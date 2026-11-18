package com.fliprss.app.data

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GReaderClientTest {
    private lateinit var server: MockWebServer
    private val edits = AtomicInteger()
    private val tokens = AtomicInteger()
    private var rejectFirstEdit = false
    @Before fun start() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl!!.encodedPath.substringAfter("/api/greader.php/")
                val body = when {
                    path == "accounts/ClientLogin" -> return MockResponse().setBody("SID=ignored\r\nAuth=test-auth\r\n")
                    path == "reader/api/0/subscription/list" -> """{"subscriptions":[{"id":"feed/1","title":"A feed","htmlUrl":"https://example.org/news/","categories":[{"id":"user/-/label/Tech / AI","label":"Tech / AI"}]}]}"""
                    path == "reader/api/0/tag/list" -> """{"tags":[{"id":"user/-/state/com.google/starred"},{"id":"user/-/label/Tech / AI"}]}"""
                    path == "reader/api/0/unread-count" -> """{"unreadcounts":[{"id":"user/-/label/Tech / AI","count":3},{"id":"user/-/state/com.google/reading-list","count":3}]}"""
                    path.startsWith("reader/api/0/stream/contents/") -> """{"items":[{"id":"tag:google.com,2005:reader/item/ffffffffffffffff","title":"Hello &amp; world","origin":{"streamId":"feed/1"},"categories":["user/-/state/com.google/read"],"canonical":[{"href":"article.html"}],"summary":{"content":"<p>Hello <b>reader</b>.</p><script>steal()</script><img src='../images/hero.jpg'>"}}],"continuation":"18446744073709551615"}"""
                    path == "reader/api/0/subscription/quickadd" -> """{"numResults":1,"streamId":"feed/2"}"""
                    path == "reader/api/0/mark-all-as-read" -> return MockResponse().setBody("OK\n")
                    path == "reader/api/0/token" -> return MockResponse().setBody("write-${tokens.incrementAndGet()}\n")
                    path == "reader/api/0/edit-tag" -> {
                        if (rejectFirstEdit && edits.incrementAndGet() == 1) return MockResponse().setResponseCode(403)
                        return MockResponse().setBody("OK\n")
                    }
                    else -> return MockResponse().setResponseCode(404)
                }
                return MockResponse().setHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
    }
    @After fun stop() { server.shutdown() }

    @Test fun loginCatalogueFolderPaginationAndContentAreMapped() = runBlocking {
        val connector = GReaderConnector()
        val session = connector.login(server.url("/news/").toString(), "rick", "dummy-api-password", true)
        val repository = connector.repository(session)
        val catalogue = repository.catalogue()
        assertEquals(2, catalogue.sections.size)
        assertEquals(3, catalogue.unread)
        val folder = catalogue.sections.first { !it.feed }
        val page = repository.page(folder.id, "9223372036854775808")
        assertEquals("18446744073709551615", page.continuation)
        val article = page.articles.single()
        assertEquals("Hello & world", article.title)
        assertEquals("A feed", article.source)
        assertEquals("https://example.org/news/article.html", article.originalUrl)
        assertEquals("https://example.org/images/hero.jpg", article.imageUrl)
        assertEquals(listOf("Hello reader."), article.paragraphs)
        assertTrue(article.initiallyRead)
        assertFalse(article.demo)
        val requests = drain()
        val login = requests.first()
        assertEquals("POST", login.method)
        assertFalse(login.path!!.contains("dummy-api-password"))
        val form = decodeForm(login)
        assertEquals("dummy-api-password", form["Passwd"])
        assertEquals("rick", form["Email"])
        requests.drop(1).forEach { assertEquals("GoogleLogin auth=test-auth", it.getHeader("Authorization")) }
        val contents = requests.last()
        assertTrue(contents.requestUrl!!.encodedPath.endsWith("user/-/label/Tech%20%2F%20AI"))
        assertEquals("9223372036854775808", contents.requestUrl!!.queryParameter("c"))
    }

    @Test fun editUsesTokenRetriesExpiredWriteTokenOnceAndPreservesItemId() = runBlocking {
        rejectFirstEdit = true
        val connector = GReaderConnector()
        val session = connector.login(server.url("/").toString(), "rick", "dummy", true)
        val repository = connector.repository(session)
        val id = "tag:google.com,2005:reader/item/ffffffffffffffff"
        repository.setRead(id, true)
        repository.setStarred(id, false)
        val changes = drain().filter { it.path!!.contains("edit-tag") }.map(::decodeForm)
        assertEquals(3, changes.size)
        assertEquals("write-1", changes[0]["T"])
        assertEquals("write-2", changes[1]["T"])
        changes.forEach { assertEquals(id, it["i"]) }
        assertEquals(READ, changes[1]["a"])
        assertEquals(STARRED, changes.last()["r"])
        assertFalse(changes.last().containsKey("a"))
    }

    @Test fun authenticationFailureIsSafeAndDoesNotExposeServerBody() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(401).setBody("private server diagnostic")
        }
        try {
            GReaderConnector().login(server.url("/").toString(), "rick", "dummy", true)
            fail("Expected sign-in failure")
        } catch (failure: FreshRssProblem) {
            assertFalse(failure.message!!.contains("private server diagnostic"))
            assertFalse(failure.message!!.contains("dummy"))
            assertFalse(failure.signInRequired)
        }
    }

    @Test fun loginDoesNotReplayPasswordToRedirectDestination() = runBlocking {
        val destination = MockWebServer().apply { start() }
        try {
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(307)
                    .setHeader("Location", destination.url("/steal"))
            }
            try { GReaderConnector().login(server.url("/").toString(), "rick", "dummy", true); fail() }
            catch (failure: FreshRssProblem) { assertTrue(failure.message!!.contains("redirected")) }
            assertEquals(0, destination.requestCount)
        } finally { destination.shutdown() }
    }

    @Test fun endpointPreservesSubdirectoryAndRequiresExplicitHttpOptIn() {
        assertEquals("https://example.org/news/api/greader.php/", FreshRssEndpoint.normalize("https://example.org/news/", false))
        assertEquals("https://example.org/news/api/greader.php/", FreshRssEndpoint.normalize("https://example.org/news/api/greader.php/", false))
        for (bad in listOf("http://example.org", "https://user:password@example.org", "https://example.org/?token=secret")) {
            try { FreshRssEndpoint.normalize(bad, false); fail("Accepted unsafe URL") } catch (_: FreshRssProblem) { }
        }
    }


    @Test fun hideReadIsSentToServerForEveryPage() = runBlocking {
        val connector = GReaderConnector()
        val repository = connector.repository(connector.login(server.url("/").toString(), "rick", "dummy", true))
        repository.page(READING_LIST, null, unreadOnly = true)
        repository.page(READING_LIST, "next", unreadOnly = true)
        val requests = drain().filter { it.path!!.contains("stream/contents") }
        assertEquals(2, requests.size)
        requests.forEach { assertEquals(READ, it.requestUrl!!.queryParameter("xt")) }
        assertEquals("next", requests.last().requestUrl!!.queryParameter("c"))
    }

    @Test fun subscribeAndBulkReadUseWriteTokensAndExactScopedCutoff() = runBlocking {
        val connector = GReaderConnector()
        val repository = connector.repository(connector.login(server.url("/").toString(), "rick", "dummy", true))
        val feed = "https://feeds.example.org/rss?key=dummy-private"
        assertEquals("feed/2", repository.addFeed(feed + "#ignored"))
        repository.markAllRead("user/-/label/Tech / AI", 1_700_000_000_123_000L)
        val requests = drain()
        val add = requests.single { it.path!!.contains("quickadd") }
        assertEquals("POST", add.method)
        assertFalse(add.path!!.contains("dummy-private"))
        assertEquals(feed, decodeForm(add)["quickadd"])
        assertEquals("write-1", decodeForm(add)["T"])
        val bulk = requests.single { it.path!!.contains("mark-all-as-read") }
        assertEquals("GoogleLogin auth=test-auth", bulk.getHeader("Authorization"))
        assertEquals("user/-/label/Tech / AI", decodeForm(bulk)["s"])
        assertEquals("1700000000123000", decodeForm(bulk)["ts"])
        assertEquals("write-1", decodeForm(bulk)["T"])
    }

    @Test fun rejectedSubscriptionNeverClaimsSuccessAndInvalidUrlSendsNothing() = runBlocking {
        val connector = GReaderConnector()
        val repository = connector.repository(connector.login(server.url("/").toString(), "rick", "dummy", true))
        val before = server.requestCount
        for (url in listOf("not-a-url", "https://user:secret@example.org/feed")) {
            try { repository.addFeed(url); fail("Accepted invalid feed URL") } catch (_: FreshRssProblem) { }
        }
        assertEquals(before, server.requestCount)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = if (request.path!!.contains("token")) MockResponse().setBody("test-write-token")
                else MockResponse().setBody("""{"numResults":0,"error":"private server diagnostic"}""")
        }
        try { repository.addFeed("https://feeds.example.org/rss"); fail("Claimed rejected feed success") }
        catch (failure: FreshRssProblem) { assertFalse(failure.message!!.contains("private server diagnostic")) }
    }

    private fun drain() = List(server.requestCount) { server.takeRequest(1, TimeUnit.SECONDS)!! }
    private fun decodeForm(request: RecordedRequest): Map<String, String> {
        val encoded = request.body.clone().readUtf8()
        return encoded.split('&').associate {
            val parts = it.split('=', limit = 2)
            java.net.URLDecoder.decode(parts[0], "UTF-8") to java.net.URLDecoder.decode(parts.getOrElse(1) { "" }, "UTF-8")
        }
    }
}
