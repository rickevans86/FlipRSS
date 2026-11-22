package com.fliprss.app.data

import com.fliprss.app.Article
import com.fliprss.app.DemoLibrary
import java.io.IOException
import javax.net.ssl.SSLException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface DemoPreferences {
    fun load(): DemoReadingState
    fun save(state: DemoReadingState)
}
data class DemoReadingState(val saved: Set<String> = emptySet(), val read: Set<String> = emptySet())
data class MutationKey(val id: String, val read: Boolean)
data class LibraryState(
    val local: Boolean = false, val demo: Boolean = true, val restoring: Boolean = true, val account: Account? = null,
    val accountRevision: Int = 0, val connecting: Boolean = false, val sessionExpired: Boolean = false,
    val articles: List<Article> = DemoLibrary.articles,
    val sections: List<Section> = DemoLibrary.topics.map { Section(it, it) },
    val scope: LibraryScope = LibraryScope(), val savedIds: Set<String> = emptySet(), val readIds: Set<String> = emptySet(),
    val loading: Boolean = false, val loadingMore: Boolean = false, val continuation: String? = null,
    val unread: Int? = null, val error: String? = null, val loginError: String? = null,
    val pending: Set<MutationKey> = emptySet(),
    val settings: AppSettings = AppSettings(),
    val addingFeed: Boolean = false, val feedError: String? = null, val feedAddedRevision: Int = 0,
    val markingAll: Boolean = false, val notice: String? = null,
) {
    val visibleSections: List<Section> get() = sections.filter { section ->
        !settings.hideEmptyCategories || (if (demo) DemoLibrary.articles.count { it.topic == section.id && it.id !in readIds } else section.unread) != 0
    }
    val visibleArticles: List<Article> get() = if (settings.hideRead && !scope.savedOnly) articles.filterNot { it.id in readIds } else articles
}

/** Owns async/account boundaries independently of Android, allowing race and rollback tests. */
class LibraryController(private val scope: CoroutineScope, private val connector: FreshRssConnector,
    private val sessions: SessionStore, private val demoPreferences: DemoPreferences,
    private val settingsStore: SettingsStore = MemorySettingsStore(),
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val localRepository: LocalFeedRepository? = null) {
    private val mutable = MutableStateFlow(LibraryState(settings = settingsStore.load().normalized()).let {
        val demo = demoPreferences.load()
        it.copy(savedIds = demo.saved, readIds = demo.read, scope = LibraryScope(unreadOnly = it.settings.hideRead))
    })
    val state: StateFlow<LibraryState> = mutable.asStateFlow()
    private var repository: FreshRssRepository? = null
    private var generation = 0
    private var accountAttempt = 0
    private var requestNumber = 0
    private var loadJob: Job? = null
    private var accountJob: Job? = null
    private var addJob: Job? = null
    private var bulkJob: Job? = null
    private val sessionMutex = Mutex()
    private val writes = mutableMapOf<MutationKey, Job>()
    private val cache = mutableMapOf<String, Article>()
    private var revision = 0L
    private val readRevision = mutableMapOf<String, Long>()
    private val starRevision = mutableMapOf<String, Long>()

    init {
        accountJob = scope.launch {
            try {
                val session = sessionMutex.withLock { sessions.read() }
                if (accountAttempt == 0) {
                    if (localRepository != null && (session == null || mutable.value.settings.useLocalFeeds)) installLocal()
                    else if (session != null) install(session) else mutable.update { it.copy(restoring = false) }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (accountAttempt == 0) mutable.update { it.copy(restoring = false,
                    loginError = "Saved sign-in could not be restored. Forget it and sign in again.",
                    error = "Saved sign-in could not be restored. Open Account to reconnect.") }
            }
        }
    }

    fun connect(server: String, username: String, password: String, allowHttp: Boolean) {
        val attempt = ++accountAttempt
        accountJob?.cancel()
        mutable.update { it.copy(connecting = true, restoring = false, loginError = null) }
        accountJob = scope.launch {
            try {
                val session = connector.login(server, username, password, allowHttp)
                sessionMutex.withLock { if (attempt == accountAttempt) sessions.save(session) }
                if (attempt == accountAttempt) install(session)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (attempt == accountAttempt) mutable.update { it.copy(connecting = false, loginError = message(failure)) }
            }
        }
    }

    private fun install(session: FreshRssSession) {
        generation++
        loadJob?.cancel(); addJob?.cancel(); bulkJob?.cancel()
        writes.values.toList().forEach(Job::cancel); writes.clear()
        repository = connector.repository(session)
        cache.clear(); readRevision.clear(); starRevision.clear(); revision = 0
        val settings = mutable.value.settings.copy(useLocalFeeds = false)
        settingsStore.save(settings)
        mutable.value = LibraryState(demo = false, restoring = false, settings = settings,
            scope = LibraryScope(unreadOnly = settings.hideRead), account = Account(session.endpoint, session.username, session.allowHttp),
            accountRevision = mutable.value.accountRevision + 1, articles = emptyList(), sections = emptyList())
        refresh()
    }

    /** Switching sources preserves the saved FreshRSS sign-in and local library. */
    fun useLocalFeeds() {
        if (localRepository == null) return
        accountAttempt++; accountJob?.cancel()
        installLocal()
    }
    fun useSavedFreshRss() {
        val attempt = ++accountAttempt
        accountJob?.cancel()
        mutable.update { it.copy(connecting = true, loginError = null) }
        accountJob = scope.launch {
            try {
                val session = sessionMutex.withLock { sessions.read() }
                    ?: throw FreshRssProblem("No saved FreshRSS account. Sign in below.")
                if (attempt == accountAttempt) install(session)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (attempt == accountAttempt) mutable.update { it.copy(connecting = false, loginError = message(failure)) }
            }
        }
    }
    fun useDemo() {
        accountAttempt++; accountJob?.cancel(); generation++
        loadJob?.cancel(); addJob?.cancel(); bulkJob?.cancel()
        writes.values.toList().forEach(Job::cancel); writes.clear()
        repository = null; cache.clear(); readRevision.clear(); starRevision.clear()
        val demo = demoPreferences.load()
        mutable.value = LibraryState(restoring = false, settings = mutable.value.settings,
            scope = LibraryScope(unreadOnly = mutable.value.settings.hideRead), savedIds = demo.saved, readIds = demo.read,
            accountRevision = mutable.value.accountRevision + 1)
    }
    private fun installLocal() {
        repository = localRepository ?: return
        generation++; loadJob?.cancel(); addJob?.cancel(); bulkJob?.cancel()
        writes.values.toList().forEach(Job::cancel); writes.clear()
        cache.clear(); readRevision.clear(); starRevision.clear(); revision = 0
        val settings = mutable.value.settings.copy(useLocalFeeds = true)
        settingsStore.save(settings)
        mutable.value = LibraryState(local = true, demo = false, restoring = false, settings = settings,
            articles = emptyList(), sections = emptyList(), scope = LibraryScope(unreadOnly = settings.hideRead),
            accountRevision = mutable.value.accountRevision + 1)
        load(reset = true, catalogue = true, refreshLocal = false)
    }

    /** Clear encrypted storage before leaving the account, so sign-out cannot silently fail. */
    fun signOut() {
        val attempt = ++accountAttempt
        accountJob?.cancel()
        mutable.update { it.copy(connecting = true) }
        accountJob = scope.launch {
            try {
                sessionMutex.withLock { if (attempt == accountAttempt) sessions.clear() }
                if (attempt == accountAttempt) {
                    generation++
                    loadJob?.cancel(); addJob?.cancel(); bulkJob?.cancel()
                    writes.values.toList().forEach(Job::cancel); writes.clear()
                    repository = null; cache.clear(); readRevision.clear(); starRevision.clear()
                    val demo = demoPreferences.load()
                    val settings = mutable.value.settings
                    mutable.value = LibraryState(restoring = false, settings = settings,
                        scope = LibraryScope(unreadOnly = settings.hideRead), savedIds = demo.saved, readIds = demo.read,
                        accountRevision = mutable.value.accountRevision + 1)
                    if (localRepository != null) installLocal()
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { if (attempt == accountAttempt) mutable.update { it.copy(connecting = false,
                loginError = "Could not forget the saved sign-in. Please try again.") } }
        }
    }

    fun selectScope(value: LibraryScope) {
        val section = value.sectionId?.takeIf { id -> mutable.value.sections.any { it.id == id } }
        val selection = value.copy(sectionId = if (!mutable.value.demo && value.savedOnly) null else section,
            unreadOnly = mutable.value.settings.hideRead && !value.savedOnly)
        if (selection == mutable.value.scope) return
        loadJob?.cancel()
        mutable.update { it.copy(scope = selection, articles = emptyList(), continuation = null, loadingMore = false, error = null) }
        if (mutable.value.demo) updateDemo() else load(reset = true, catalogue = false)
    }

    fun refresh() { if (!mutable.value.demo) load(reset = true, catalogue = true) else updateDemo() }
    fun loadMore() { if (!mutable.value.demo && mutable.value.continuation != null && !mutable.value.loading && !mutable.value.loadingMore) load(false, false) }
    fun dismissError() { mutable.update { it.copy(error = null) } }
    fun dismissNotice() { mutable.update { it.copy(notice = null) } }
    fun clearFeedError() { mutable.update { it.copy(feedError = null) } }
    fun updateSettings(value: AppSettings) {
        val settings = value.normalized()
        val changedFilter = settings.hideRead != mutable.value.settings.hideRead
        settingsStore.save(settings)
        mutable.update { it.copy(settings = settings) }
        if (changedFilter) selectScope(mutable.value.scope.copy(unreadOnly = settings.hideRead))
    }
    fun findArticle(id: String): Article? = if (mutable.value.demo) DemoLibrary.articles.find { it.id == id } else cache[id]
    fun bookmarkedArticles(): List<Article> = if (mutable.value.demo) DemoLibrary.articles.filter { it.id in mutable.value.savedIds } else cache.values.filter { it.id in mutable.value.savedIds }
    fun cover(section: Section): Article? = if (mutable.value.demo) DemoLibrary.articles.find { it.topic == section.id }
        else cache.values.firstOrNull { it.feedId == section.id || section.id in it.topicIds }

    private fun load(reset: Boolean, catalogue: Boolean, refreshLocal: Boolean = true) {
        val client = repository ?: return
        loadJob?.cancel()
        val current = generation
        val request = ++requestNumber
        val selection = mutable.value.scope
        val cursor = if (reset) null else mutable.value.continuation
        val startedAt = revision
        mutable.update { it.copy(loading = reset, loadingMore = !reset, error = null) }
        loadJob = scope.launch {
            try {
                val warning = if (catalogue && refreshLocal && client is LocalFeedRepository) client.refreshFeeds() else null
                val categories = if (catalogue) client.catalogue() else null
                val page = client.page(selection.stream, cursor, selection.unreadOnly)
                if (current != generation || request != requestNumber) return@launch
                page.articles.forEach { cache[it.id] = it }
                mutable.update { old ->
                    val read = old.readIds.toMutableSet(); val saved = old.savedIds.toMutableSet()
                    page.articles.forEach { article ->
                        if ((readRevision[article.id] ?: 0) <= startedAt && MutationKey(article.id, true) !in old.pending) {
                            if (article.initiallyRead) read.add(article.id) else read.remove(article.id)
                        }
                        if ((starRevision[article.id] ?: 0) <= startedAt && MutationKey(article.id, false) !in old.pending) {
                            if (article.initiallyStarred) saved.add(article.id) else saved.remove(article.id)
                        }
                    }
                    old.copy(articles = ((if (reset) emptyList() else old.articles) + page.articles).distinctBy { it.id },
                        sections = categories?.sections?.map { section ->
                            if (revision == startedAt) section else section.copy(unread = old.sections.find { it.id == section.id }?.unread ?: section.unread)
                        } ?: old.sections,
                        unread = if (categories != null && revision == startedAt) categories.unread else old.unread,
                        savedIds = saved, readIds = read, loading = false, loadingMore = false,
                        continuation = page.continuation?.takeUnless { it == cursor }, error = warning)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (current == generation && request == requestNumber) mutable.update { it.copy(loading = false,
                    loadingMore = false, sessionExpired = it.sessionExpired || (failure is FreshRssProblem && failure.signInRequired),
                    error = message(failure)) }
            }
        }
    }

    fun toggleSaved(id: String) = mutate(id, false, id !in mutable.value.savedIds)
    fun setRead(id: String, read: Boolean) = mutate(id, true, read)

    private fun mutate(id: String, read: Boolean, enabled: Boolean) {
        val old = mutable.value
        if (old.markingAll) return
        val previous = id in if (read) old.readIds else old.savedIds
        if (previous == enabled || findArticle(id) == null) return
        if (old.demo) {
            updateFlag(id, read, enabled)
            demoPreferences.save(DemoReadingState(mutable.value.savedIds, mutable.value.readIds))
            updateDemo(); return
        }
        val client = repository ?: return
        val key = MutationKey(id, read)
        if (key in old.pending) return
        if (old.sessionExpired) { mutable.update { it.copy(error = "Your session has expired. Sign in again.") }; return }
        val current = generation
        if (read) readRevision[id] = ++revision else starRevision[id] = ++revision
        mutable.update { it.copy(pending = it.pending + key) }
        updateFlag(id, read, enabled)
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                if (read) client.setRead(id, enabled) else client.setStarred(id, enabled)
                if (current == generation && !read && !enabled && mutable.value.scope.savedOnly) {
                    mutable.update { it.copy(articles = it.articles.filterNot { article -> article.id == id }) }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (current == generation) {
                    updateFlag(id, read, previous)
                    mutable.update { it.copy(sessionExpired = it.sessionExpired || (failure is FreshRssProblem && failure.signInRequired),
                        error = message(failure) + " Refresh to check the server state.") }
                }
            } finally {
                if (current == generation) { writes.remove(key); mutable.update { it.copy(pending = it.pending - key) } }
            }
        }
        writes[key] = job; job.start()
    }

    fun addFeed(url: String) {
        val old = mutable.value
        val client = repository
        if (old.demo || client == null || old.sessionExpired) {
            mutable.update { it.copy(feedError = "Sign in to FreshRSS before adding a feed.") }; return
        }
        if (old.addingFeed || old.markingAll) return
        val current = generation
        mutable.update { it.copy(addingFeed = true, feedError = null, notice = null) }
        addJob = scope.launch {
            try {
                client.addFeed(url)
                if (current == generation) {
                    mutable.update { it.copy(addingFeed = false, feedAddedRevision = it.feedAddedRevision + 1,
                        notice = if (it.local) "Feed added to this device." else "Feed added to FreshRSS.") }
                    load(reset = true, catalogue = true, refreshLocal = false)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (current == generation) mutable.update { it.copy(addingFeed = false,
                    sessionExpired = it.sessionExpired || (failure is FreshRssProblem && failure.signInRequired),
                    feedError = message(failure) + if (failure is IOException) " Refresh feeds before retrying to check whether it was added." else "") }
            }
        }
    }

    /** Both page shortcuts use this policy; capture the same scope/cutoff before any action. */
    fun requestMarkAllRead(): MarkReadTarget? {
        val target = markReadTarget()
        if (mutable.value.settings.confirmMarkAllRead) return target
        markAllRead(target)
        return null
    }

    fun markReadTarget(): MarkReadTarget {
        val value = mutable.value
        val title = if (value.scope.savedOnly) "Saved stories" else
            value.sections.find { it.id == value.scope.sectionId }?.title ?: "All stories"
        return MarkReadTarget(value.scope.stream, title, nowMillis() * 1_000L, generation,
            value.articles.map { it.id }.toSet())
    }

    fun markAllRead(target: MarkReadTarget) {
        val old = mutable.value
        if (target.accountGeneration != generation) {
            mutable.update { it.copy(error = "The account changed. Open mark all read again.") }; return
        }
        if (old.markingAll || old.addingFeed || old.loading || old.loadingMore || old.connecting || old.pending.isNotEmpty()) {
            mutable.update { it.copy(error = "Wait for current changes to finish, then try again.") }; return
        }
        if (old.demo) {
            val ids = DemoLibrary.articles.map { it.id }.toSet()
            // Use the confirmed snapshot, not the subsequently selected scope.
            mutable.update { it.copy(readIds = it.readIds + target.knownIds.intersect(ids), notice = null) }
            demoPreferences.save(DemoReadingState(mutable.value.savedIds, mutable.value.readIds))
            return
        }
        val client = repository ?: return
        if (old.sessionExpired) { mutable.update { it.copy(error = "Your session has expired. Sign in again.") }; return }
        val current = generation
        mutable.update { it.copy(markingAll = true, error = null, notice = null) }
        bulkJob = scope.launch {
            try {
                client.markAllRead(target.stream, target.cutoffMicros)
                if (current == generation) {
                    target.knownIds.forEach { readRevision[it] = ++revision }
                    mutable.update { it.copy(markingAll = false, readIds = it.readIds + target.knownIds,
                        notice = null) }
                    refresh()
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                if (current == generation) mutable.update { it.copy(markingAll = false,
                    sessionExpired = it.sessionExpired || (failure is FreshRssProblem && failure.signInRequired),
                    error = message(failure) + " Refresh to check the server state.") }
            }
        }
    }

    private fun updateFlag(id: String, read: Boolean, enabled: Boolean) {
        val article = findArticle(id)
        mutable.update { old ->
            val existing = if (read) old.readIds else old.savedIds
            val updated = if (enabled) existing + id else existing - id
            val countDelta = if (!read || (id in existing) == enabled) 0 else if (enabled) -1 else 1
            old.copy(readIds = if (read) updated else old.readIds, savedIds = if (!read) updated else old.savedIds,
                unread = old.unread?.let { (it + countDelta).coerceAtLeast(0) },
                sections = old.sections.map { section ->
                    if (section.unread != null && (section.id == article?.feedId || section.id in article?.topicIds.orEmpty()))
                        section.copy(unread = (section.unread + countDelta).coerceAtLeast(0)) else section
                })
        }
    }
    private fun updateDemo() { mutable.update { it.copy(articles = DemoLibrary.select(it.scope.sectionId, it.scope.savedOnly, it.savedIds)) } }
    private fun message(failure: Exception): String = if (mutable.value.local && failure !is FreshRssProblem)
        "Could not update local feeds. Check the connection and device storage; saved articles are kept."
        else when (failure) {
        is FreshRssProblem -> failure.message ?: "Could not complete the FreshRSS request."
        is SSLException -> "Could not verify the server’s secure connection. Check its HTTPS certificate."
        is IOException -> "Could not reach FreshRSS. Check the connection and instance URL."
        else -> "Could not read the FreshRSS response or secure session. Check API access and try again."
    }
}
