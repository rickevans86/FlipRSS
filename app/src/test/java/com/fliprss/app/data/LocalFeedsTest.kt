package com.fliprss.app.data

import com.fliprss.app.DemoLibrary
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class LocalFeedsTest {
    private class Store : LocalFeedStore {
        var value: String? = null
        var fail = false
        override fun read() = value
        override fun write(json: String) { if (fail) throw java.io.IOException("test storage failure"); value = json }
    }
    private fun rss(vararg ids: String) = """<rss version="2.0"><channel><title>News &amp; views</title>${ids.joinToString("") {
        "<item><guid>$it</guid><title>Story $it</title><link>https://example.org/$it</link><description><![CDATA[<p>Hello <b>reader</b></p><script>bad()</script>]]></description></item>"
    }}</channel></rss>"""

    @Test fun rssAndAtomMapContentAndStableScopedIds() {
        val (feed, stories) = LocalFeedParser.parse(rss("one"), "https://example.org/rss", 100)
        assertEquals("News & views", feed.title)
        assertEquals(listOf("Hello reader"), stories.single().article.paragraphs)
        assertEquals(listOf(feed.category), stories.single().article.topicIds)
        assertFalse(stories.single().article.demo)
        val atom = """<feed xmlns="http://www.w3.org/2005/Atom"><title>Atom</title><entry><id>one</id><title>A story</title><link href="/a"/><content type="html">&lt;p&gt;Atom body&lt;/p&gt;</content></entry></feed>"""
        val a = LocalFeedParser.parse(atom, "https://example.org/atom", 100).second.single().article
        assertEquals("https://example.org/a", a.originalUrl)
        assertEquals(listOf("Atom body"), a.paragraphs)
        assertNotEquals(stories.single().article.id, a.id)
        assertEquals(a.id, LocalFeedParser.parse(atom, "https://example.org/atom", 200).second.single().article.id)
    }
    @Test fun rejectsHtmlWithoutFetchingExternalEntities() {
        try { LocalFeedParser.parse("<html><body>Not a feed</body></html>", "https://example.org", 0); fail() }
        catch (_: FreshRssProblem) { }
        val xml = """<!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><rss><channel><title>&xxe;</title></channel></rss>"""
        assertFalse(LocalFeedParser.parse(xml, "https://example.org/rss", 0).first.title.contains("root:"))
    }
    @Test fun persistsSubscriptionsReadSavedAndOfflineCache() = runBlocking {
        MockWebServer().use { server ->
            val store = Store(); val repo = LocalFeedRepository(store)
            server.enqueue(MockResponse().setBody(rss("one", "two")))
            val url = server.url("/rss").toString()
            repo.addFeed(url)
            val all = repo.page(READING_LIST, null).articles
            assertEquals(2, all.size)
            repo.setRead(all[0].id, true); repo.setStarred(all[1].id, true)
            val restored = LocalFeedRepository(store)
            assertEquals(1, restored.catalogue().unread)
            assertEquals(listOf(all[1].id), restored.page(STARRED, null).articles.map { it.id })
            assertEquals(listOf(all[1].id), restored.page(READING_LIST, null, true).articles.map { it.id })
            server.enqueue(MockResponse().setResponseCode(503))
            assertNotNull(restored.refreshFeeds())
            assertEquals(2, restored.page(READING_LIST, null).articles.size)
            try { restored.addFeed(url); fail() } catch (_: FreshRssProblem) { }
            assertNull(server.takeRequest().getHeader("Authorization"))
        }
    }
    @Test fun refreshKeepsReadStateAndBulkRespectsDiscoveryCutoff() = runBlocking {
        MockWebServer().use { server ->
            val store = Store(); var now = 100L
            val repo = LocalFeedRepository(store, nowMillis = { now })
            server.enqueue(MockResponse().setBody(rss("one"))); repo.addFeed(server.url("/rss").toString())
            val id = repo.page(READING_LIST, null).articles.single().id
            repo.setRead(id, true); now = 200
            server.enqueue(MockResponse().setBody(rss("one", "two"))); assertNull(repo.refreshFeeds())
            repo.markAllRead(READING_LIST, 150_000)
            assertEquals(1, repo.page(READING_LIST, null, true).articles.size)
            repo.markAllRead(READING_LIST, 200_000)
            assertTrue(repo.catalogue().sections.all { it.unread == 0 })
        }
    }
    @Test fun rejectsUnsafeUrlsAndFailedWritesDoNotClaimSuccess() = runBlocking {
        MockWebServer().use { server ->
            val store = Store(); val repo = LocalFeedRepository(store)
            for (url in listOf("file:///etc/passwd", "https://user:password@example.org/rss")) {
                try { repo.addFeed(url); fail() } catch (_: FreshRssProblem) { }
            }
            server.enqueue(MockResponse().setBody(rss("one"))); store.fail = true
            try { repo.addFeed(server.url("/rss").toString()); fail() } catch (_: java.io.IOException) { }
            assertEquals(0, repo.catalogue().unread)
        }
    }
    @Test fun hidesOnlyKnownEmptyCategoriesAndRestoresThemWhenDisabled() {
        val sections = listOf(Section("empty", "Empty", unread = 0), Section("unknown", "Unknown"), Section("news", "News", unread = 2))
        val state = LibraryState(demo = false, sections = sections, settings = AppSettings(hideEmptyCategories = true))
        assertEquals(listOf("unknown", "news"), state.visibleSections.map { it.id })
        assertEquals(sections, state.copy(settings = AppSettings()).visibleSections)
        val demo = LibraryState(settings = AppSettings(hideEmptyCategories = true), readIds = DemoLibrary.articles.map { it.id }.toSet())
        assertTrue(demo.visibleSections.isEmpty())
    }
    @Test fun atomicFileStoreRoundTrips() {
        val dir = java.nio.file.Files.createTempDirectory("fliprss-local-test").toFile()
        try {
            val store = FileLocalFeedStore(java.io.File(dir, "feeds.json"))
            assertNull(store.read()); store.write("first"); store.write("second"); assertEquals("second", store.read())
        } finally { dir.deleteRecursively() }
    }
    @Test fun redirectsResolveRelativeLinksWithoutChangingSubscriptionIdentity() = runBlocking {
        MockWebServer().use { original -> MockWebServer().use { destination ->
            val store = Store(); val repo = LocalFeedRepository(store)
            original.enqueue(MockResponse().setResponseCode(302).setHeader("Location", destination.url("/folder/feed")))
            destination.enqueue(MockResponse().setBody("<rss><channel><title>Redirect</title><item><guid>one</guid><link>story</link><description>Body</description></item></channel></rss>"))
            val url = original.url("/feed").toString(); val id = repo.addFeed(url)
            assertEquals("feed/local/" + localHash(url), id)
            assertEquals(destination.url("/folder/story").toString(), repo.page(READING_LIST, null).articles.single().originalUrl)
            assertNull(destination.takeRequest().getHeader("Authorization"))
        } }
    }
    @Test fun oversizedFeedsAreRejectedBeforeParsingOrSaving() = runBlocking {
        MockWebServer().use { server ->
            val store = Store(); val repo = LocalFeedRepository(store)
            server.enqueue(MockResponse().setBody("x".repeat(5_000_001)))
            try { repo.addFeed(server.url("/large").toString()); fail() } catch (_: FreshRssProblem) { }
            assertNull(store.value)
        }
    }
    @Test fun corruptStorageIsKeptAndNotOverwritten() = runBlocking {
        val store = Store().apply { value = "null" }
        val repo = LocalFeedRepository(store)
        try { repo.catalogue(); fail() } catch (_: FreshRssProblem) { }
        assertEquals("null", store.value)
    }
}
