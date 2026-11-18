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
        val readWrites = mutableListOf<Boolean>()
        override suspend fun catalogue() = Catalogue(listOf(Section("user/-/label/News", "News", unread = 2),
            Section("feed/1", "Feed", feed = true, unread = 2)), 2)
        override suspend fun page(stream: String, continuation: String?): ArticlePage {
            cursors += continuation
            pageFailure?.let { throw it }
            return pageCall?.invoke(continuation) ?: ArticlePage(listOf(article), null)
        }
        override suspend fun setRead(id: String, read: Boolean) { readGate?.await(); readFailure?.let { throw it }; readWrites += read }
        override suspend fun setStarred(id: String, starred: Boolean) { starFailure?.let { throw it } }
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

}
