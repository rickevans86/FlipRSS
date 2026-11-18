package com.fliprss.app.data

import com.fliprss.app.Article
import com.fliprss.app.DemoLibrary
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryControllerTest {
    private val story = Article("live-1", "News", "Feed", "A real article", "Summary", "", 1,
        listOf("Body"), topicIds = listOf("user/-/label/News"), feedId = "feed/1", demo = false)
    private class SavedSession : SessionStore {
        var session: FreshRssSession? = null
        override suspend fun read() = session
        override suspend fun save(session: FreshRssSession) { this.session = session }
        override suspend fun clear() { session = null }
    }
    private class DemoPrefs : DemoPreferences {
        var value = DemoReadingState()
        override fun load() = value
        override fun save(state: DemoReadingState) { value = state }
    }
    private class FakeRepository(val article: Article) : FreshRssRepository {
        var readFailure: Exception? = null
        var readGate: CompletableDeferred<Unit>? = null
        var starFailure: Exception? = null
        var pageFailure: Exception? = null
        var pageCall: (suspend (String?) -> ArticlePage)? = null
        val cursors = mutableListOf<String?>()
        val unreadFilters = mutableListOf<Boolean>()
        val added = mutableListOf<String>()
        val bulkRequests = mutableListOf<Pair<String, Long>>()
        var addFailure: Exception? = null
        var addGate: CompletableDeferred<Unit>? = null
        var bulkFailure: Exception? = null
        var bulkGate: CompletableDeferred<Unit>? = null
        var allRead = false
        var catalogueCalls = 0
        val readWrites = mutableListOf<Boolean>()
        override suspend fun catalogue(): Catalogue {
            catalogueCalls++
            val count = if (allRead) 0 else 2
            return Catalogue(listOf(Section("user/-/label/News", "News", unread = count),
                Section("feed/1", "Feed", feed = true, unread = count)), count)
        }
        override suspend fun page(stream: String, continuation: String?, unreadOnly: Boolean): ArticlePage {
            cursors += continuation; unreadFilters += unreadOnly
            pageFailure?.let { throw it }
            return pageCall?.invoke(continuation) ?: ArticlePage(
                if (unreadOnly && allRead) emptyList() else listOf(article.copy(initiallyRead = allRead || article.initiallyRead)), null)
        }
        override suspend fun setRead(id: String, read: Boolean) { readGate?.await(); readFailure?.let { throw it }; readWrites += read }
        override suspend fun setStarred(id: String, starred: Boolean) { starFailure?.let { throw it } }
        override suspend fun addFeed(url: String): String {
            addGate?.await(); addFailure?.let { throw it }; added += url; return "feed/2"
        }
        override suspend fun markAllRead(stream: String, cutoffMicros: Long) {
            bulkRequests += stream to cutoffMicros
            bulkGate?.await(); bulkFailure?.let { throw it }; allRead = true
        }
    }
    private class Connector(private val clients: Map<String, FreshRssRepository>) : FreshRssConnector {
        var logins = 0
        override suspend fun login(server: String, username: String, password: String, allowHttp: Boolean): FreshRssSession {
            logins++
            return FreshRssSession("https://example.org/api/greader.php/", username, "test-token", false)
        }
        override fun repository(session: FreshRssSession) = clients.getValue(session.username)
    }

    @Test fun writesRollbackOnFailureAndDemoStateStaysSeparate() = runTest {
        val repository = FakeRepository(story)
        val preferences = DemoPrefs()
        val sessions = SavedSession()
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), sessions, preferences)
        advanceUntilIdle()
        controller.toggleSaved(DemoLibrary.articles.first().id)
        controller.connect("https://example.org", "rick", "dummy", false)
        advanceUntilIdle()
        assertFalse(controller.state.value.demo)
        assertTrue(controller.state.value.savedIds.isEmpty())
        repository.starFailure = FreshRssProblem("Write failed")
        controller.toggleSaved(story.id)
        assertTrue(story.id in controller.state.value.savedIds)
        advanceUntilIdle()
        assertFalse(story.id in controller.state.value.savedIds)
        assertNotNull(controller.state.value.error)
        assertTrue(controller.state.value.pending.isEmpty())
        repository.readFailure = FreshRssProblem("Read write failed")
        controller.setRead(story.id, true)
        assertEquals(1, controller.state.value.unread)
        advanceUntilIdle()
        assertFalse(story.id in controller.state.value.readIds)
        assertEquals(2, controller.state.value.unread)
        controller.signOut(); advanceUntilIdle()
        assertTrue(controller.state.value.demo)
        assertEquals(setOf(DemoLibrary.articles.first().id), controller.state.value.savedIds)
        assertNull(sessions.session)
    }

    @Test fun paginationDeduplicatesAndRepeatedCursorStopsLoading() = runTest {
        val repository = FakeRepository(story)
        repository.pageCall = { cursor ->
            if (cursor == null) ArticlePage(listOf(story), "next")
            else ArticlePage(listOf(story, story.copy(id = "second")), "next")
        }
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        assertEquals("next", controller.state.value.continuation)
        controller.loadMore(); advanceUntilIdle()
        assertEquals(listOf("live-1", "second"), controller.state.value.articles.map { it.id })
        assertNull(controller.state.value.continuation)
        assertEquals(listOf(null, "next"), repository.cursors)
        controller.loadMore(); advanceUntilIdle()
        assertEquals(2, repository.cursors.size)
    }

    @Test fun expiredSessionKeepsLiveContentAndNeverFallsBackToDemo() = runTest {
        val repository = FakeRepository(story)
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        repository.pageFailure = FreshRssProblem("Sign in again", signInRequired = true)
        controller.refresh(); advanceUntilIdle()
        assertFalse(controller.state.value.demo)
        assertTrue(controller.state.value.sessionExpired)
        assertEquals(listOf(story), controller.state.value.articles)
        assertFalse(controller.state.value.loading)
    }

    @Test fun delayedOldAccountCannotReplaceNewAccountArticles() = runTest {
        val first = FakeRepository(story)
        val second = FakeRepository(story.copy(id = "other-account"))
        val delayed = CompletableDeferred<Unit>()
        first.pageCall = { withContext(NonCancellable) { delayed.await(); ArticlePage(listOf(story), null) } }
        val controller = LibraryController(this, Connector(mapOf("first" to first, "second" to second)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "first", "dummy", false); runCurrent()
        controller.connect("https://example.org", "second", "dummy", false); runCurrent()
        assertEquals("second", controller.state.value.account?.username)
        delayed.complete(Unit); advanceUntilIdle()
        assertEquals(listOf("other-account"), controller.state.value.articles.map { it.id })
    }

    @Test fun pageStartedBeforeReadWriteCannotUndoSuccessfulLocalChange() = runTest {
        val repository = FakeRepository(story)
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        val gate = CompletableDeferred<Unit>()
        repository.pageCall = { gate.await(); ArticlePage(listOf(story), null) }
        controller.refresh(); runCurrent()
        controller.setRead(story.id, true); runCurrent()
        assertEquals(listOf(true), repository.readWrites)
        gate.complete(Unit); advanceUntilIdle()
        assertTrue(story.id in controller.state.value.readIds)
    }
    @Test fun storedTokenRestoresWithoutRequestingOrRetainingPassword() = runTest {
        val repository = FakeRepository(story)
        val connector = Connector(mapOf("rick" to repository))
        val sessions = SavedSession().apply { session = FreshRssSession("https://example.org/api/greader.php/", "rick", "test-token", false) }
        val controller = LibraryController(this, connector, sessions, DemoPrefs())
        advanceUntilIdle()
        assertFalse(controller.state.value.demo)
        assertEquals(listOf(story), controller.state.value.articles)
        assertEquals(0, connector.logins)
    }

    @Test fun failedSecureStorageNeverPretendsSignInSucceeded() = runTest {
        val repository = FakeRepository(story)
        val sessions = object : SessionStore {
            override suspend fun read(): FreshRssSession? = null
            override suspend fun save(session: FreshRssSession) { throw java.io.IOException("storage unavailable") }
            override suspend fun clear() { }
        }
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), sessions, DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        assertTrue(controller.state.value.demo)
        assertNull(controller.state.value.account)
        assertNotNull(controller.state.value.loginError)
        assertFalse(controller.state.value.connecting)
    }

    @Test fun failedReplacementLoginDoesNotAbandonExistingAccountWrite() = runTest {
        val repository = FakeRepository(story)
        val base = Connector(mapOf("rick" to repository))
        val connector = object : FreshRssConnector by base {
            override suspend fun login(server: String, username: String, password: String, allowHttp: Boolean): FreshRssSession {
                if (username == "bad") throw FreshRssProblem("Bad credentials")
                return base.login(server, username, password, allowHttp)
            }
        }
        val controller = LibraryController(this, connector, SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        val gate = CompletableDeferred<Unit>()
        repository.readGate = gate
        controller.setRead(story.id, true); runCurrent()
        controller.connect("https://example.org", "bad", "dummy", false); runCurrent()
        gate.complete(Unit); advanceUntilIdle()
        assertEquals("rick", controller.state.value.account?.username)
        assertTrue(story.id in controller.state.value.readIds)
        assertTrue(controller.state.value.pending.isEmpty())
        assertNotNull(controller.state.value.loginError)
    }

    @Test fun hideReadUsesServerFilterAndHidesOptimisticReadsUntilRollback() = runTest {
        val repository = FakeRepository(story)
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        controller.updateSettings(controller.state.value.settings.copy(hideRead = true)); advanceUntilIdle()
        assertTrue(repository.unreadFilters.last())
        repository.readFailure = FreshRssProblem("Write failed")
        controller.setRead(story.id, true)
        assertTrue(controller.state.value.visibleArticles.isEmpty())
        advanceUntilIdle()
        assertEquals(listOf(story), controller.state.value.visibleArticles)
        controller.updateSettings(controller.state.value.settings.copy(hideRead = false)); advanceUntilIdle()
        assertFalse(repository.unreadFilters.last())
    }

    @Test fun settingsPersistAndSurviveAccountChangesWithoutUnnecessaryRefresh() = runTest {
        val repository = FakeRepository(story)
        val settings = MemorySettingsStore()
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs(), settings)
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        val calls = repository.cursors.size
        controller.updateSettings(AppSettings(theme = AppTheme.DARK, readerTextSize = 40, autoMarkRead = false, showImages = false))
        assertEquals(calls, repository.cursors.size)
        assertEquals(26, controller.state.value.settings.readerTextSize)
        controller.signOut(); advanceUntilIdle()
        assertEquals(AppTheme.DARK, controller.state.value.settings.theme)
        assertFalse(controller.state.value.settings.autoMarkRead)
        val restored = LibraryController(this, Connector(emptyMap()), SavedSession(), DemoPrefs(), settings)
        advanceUntilIdle()
        assertEquals(controller.state.value.settings, restored.state.value.settings)
    }

    @Test fun markAllReadCallsServerForCapturedScopeAndUnloadedItems() = runTest {
        val repository = FakeRepository(story)
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs(),
            nowMillis = { 1_700_000_000_123L })
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        controller.selectScope(LibraryScope("feed/1")); advanceUntilIdle()
        val target = controller.markReadTarget()
        controller.selectScope(LibraryScope()); advanceUntilIdle()
        controller.markAllRead(target); advanceUntilIdle()
        assertEquals(listOf("feed/1" to 1_700_000_000_123_000L), repository.bulkRequests)
        assertTrue(repository.readWrites.isEmpty())
        assertTrue(story.id in controller.state.value.readIds)
        assertEquals(0, controller.state.value.unread)
        assertNotNull(controller.state.value.notice)
    }

    @Test fun bulkFailureDoesNotClaimReadAndStaleAccountConfirmationIsRejected() = runTest {
        val repository = FakeRepository(story).apply { bulkFailure = FreshRssProblem("Bulk failed") }
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        val target = controller.markReadTarget()
        controller.markAllRead(target); advanceUntilIdle()
        assertFalse(story.id in controller.state.value.readIds)
        assertFalse(controller.state.value.markingAll)
        assertNull(controller.state.value.notice)
        controller.signOut(); advanceUntilIdle()
        controller.markAllRead(target); advanceUntilIdle()
        assertEquals(1, repository.bulkRequests.size)
        assertNotNull(controller.state.value.error)
    }

    @Test fun addFeedRefreshesCatalogueOnlyAfterServerSuccessAndReportsFailure() = runTest {
        val repository = FakeRepository(story)
        val controller = LibraryController(this, Connector(mapOf("rick" to repository)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "rick", "dummy", false); advanceUntilIdle()
        val calls = repository.catalogueCalls
        controller.addFeed("https://feeds.example.org/rss"); advanceUntilIdle()
        assertEquals(listOf("https://feeds.example.org/rss"), repository.added)
        assertEquals(calls + 1, repository.catalogueCalls)
        assertEquals(1, controller.state.value.feedAddedRevision)
        repository.addFailure = FreshRssProblem("Not a feed")
        controller.addFeed("https://feeds.example.org/not-feed"); advanceUntilIdle()
        assertEquals(1, controller.state.value.feedAddedRevision)
        assertNotNull(controller.state.value.feedError)
        assertFalse(controller.state.value.addingFeed)
    }

    @Test fun demoBulkUsesConfirmedSnapshotAndDoesNotContactServer() = runTest {
        val controller = LibraryController(this, Connector(emptyMap()), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.selectScope(LibraryScope("Nature"))
        val target = controller.markReadTarget()
        controller.selectScope(LibraryScope("Design"))
        controller.markAllRead(target); advanceUntilIdle()
        assertEquals(DemoLibrary.articles.filter { it.topic == "Nature" }.map { it.id }.toSet(), controller.state.value.readIds)
    }

    @Test fun savedBulkRequestStaysStarredAndKeepsNewAccountUntouched() = runTest {
        val first = FakeRepository(story.copy(initiallyStarred = true))
        val second = FakeRepository(story.copy(id = "other-account"))
        val gate = CompletableDeferred<Unit>()
        first.bulkGate = gate
        val controller = LibraryController(this, Connector(mapOf("first" to first, "second" to second)), SavedSession(), DemoPrefs())
        advanceUntilIdle(); controller.connect("https://example.org", "first", "dummy", false); advanceUntilIdle()
        controller.selectScope(LibraryScope(savedOnly = true)); advanceUntilIdle()
        controller.markAllRead(controller.markReadTarget()); runCurrent()
        assertEquals(STARRED, first.bulkRequests.single().first)
        controller.connect("https://example.org", "second", "dummy", false); runCurrent()
        gate.complete(Unit); advanceUntilIdle()
        assertEquals("second", controller.state.value.account?.username)
        assertFalse(controller.state.value.markingAll)
        assertFalse("other-account" in controller.state.value.readIds)
        assertNull(controller.state.value.notice)
    }

}
