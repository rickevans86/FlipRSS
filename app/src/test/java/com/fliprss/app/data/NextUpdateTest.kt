package com.fliprss.app.data

import com.fliprss.app.DemoLibrary
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class NextUpdateTest {
    @Test fun directoryImportsActualFeederAndFeedspotFormatsNotHomeOrFollowLinks() {
        val feeder = FeedPage(DirectoryImporter.sources.getValue("Feeder"), """<table><tr><td>Example news</td><td><code>https://example.org/rss.xml</code></td></tr></table><a href="https://feeder.co/add-feed?url=https%3A%2F%2Fexample.org%2Frss.xml">Example</a>""")
        val a = DirectoryImporter.parse("Feeder", feeder)
        assertEquals(1, a.size);assertEquals("Example news",a.single().title);assertEquals("https://example.org/rss.xml",a.single().url)
        val feedspot = FeedPage(DirectoryImporter.sources.getValue("Feedspot"), """<li><h3>1. Publisher RSS Feed</h3><p data-site="https%3A%2F%2Fexample.org%2Frss"><strong>RSS Feed</strong><a class="ext" href="https://example.org/rss">RSS</a><a class="extdomain ext" href="https://example.org/">Website</a></p></li>""")
        val b=DirectoryImporter.parse("Feedspot",feedspot)
        assertEquals(1,b.size);assertEquals("Publisher",b.single().title);assertEquals("https://example.org/rss",b.single().url)
    }
    @Test fun relativeFeedLinksBaseElementsAndUsualPathsAreFoundSafely() {
        val page=FeedPage("https://example.org/blog/", """<html><head><base href="/news/"><link rel="alternate" type="application/rss+xml" href="rss.xml"></head><body><a href="/atom.xml">RSS</a><a href="javascript:bad()">RSS</a></body></html>""")
        val urls=FeedDiscovery.candidates(page)
        assertTrue("https://example.org/news/rss.xml" in urls)
        assertTrue("https://example.org/atom.xml" in urls)
        assertTrue("https://example.org/feed" in urls)
        assertFalse(urls.any { it.startsWith("javascript:") })
        assertNull(normalizedFeedUrl("https://user:secret@example.org/feed"))
    }
    @Test fun directRssAndAtomFeedDetectionAndNoFeedAreHandled() {
        assertEquals("Test",FeedDiscovery.direct(FeedPage("https://example.org/rss", "<rss><channel><title>Test</title></channel></rss>"))!!.title)
        assertNotNull(FeedDiscovery.direct(FeedPage("https://example.org/atom.xml","<feed xmlns='http://www.w3.org/2005/Atom'><title>Atom</title></feed>")))
        assertNull(FeedDiscovery.direct(FeedPage("https://example.org","<html><title>Website</title></html>")))
    }
    @Test fun websiteDiscoveryValidatesCandidateFeedsAndNeverSendsAuthorization() = runBlocking {
        val client=OkHttpClient.Builder().addInterceptor { chain ->
            assertNull(chain.request().header("Authorization"))
            val url=chain.request().url
            val body=when(url.encodedPath) {
                "/" -> "<html><link rel='alternate' type='application/rss+xml' href='/news.xml'></html>"
                "/news.xml" -> "<rss><channel><title>News</title></channel></rss>"
                else -> "<html>Not a feed</html>"
            }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK").body(body.toResponseBody()).build()
        }.build()
        assertEquals(listOf("https://example.org/news.xml"),FeedDiscovery.find("https://example.org/",FeedWeb(client)).map { it.url })
    }
    @Test fun combinedSavedRetainsReadBookmarksAndOmitsUnconnectedServerDemo() {
        val a=DemoLibrary.articles[0].copy(local=true,demo=false)
        val b=DemoLibrary.articles[1].copy(local=false,demo=false)
        val local=LibraryState(local=true,demo=false,articles=listOf(a),savedIds=setOf(a.id),readIds=setOf(a.id),settings=AppSettings(hideRead=true))
        val fresh=LibraryState(demo=false,articles=listOf(b),savedIds=setOf(b.id),readIds=setOf(b.id))
        val combined=combinedSaved(local,fresh)
        assertEquals(listOf(a.id,b.id),combined.visibleArticles.map { it.id })
        assertTrue(combined.scope.savedOnly);assertEquals(setOf(a.id,b.id),combined.savedIds)
        assertEquals(listOf(a.id),combinedSaved(local,fresh.copy(demo=true)).visibleArticles.map { it.id })
        assertTrue(combined.articles[0].local);assertFalse(combined.articles[1].local)
    }
    @Test fun scrollingMarksOnlyPreviouslySeenCardsThatPassedAboveTop() {
        val t=ScrollReadTracker()
        assertTrue(t.observe(mapOf("a" to 3,"b" to 4),3,0,false).isEmpty())
        assertTrue(t.observe(mapOf("a" to 3,"b" to 4),3,40,true).isEmpty())
        assertEquals(setOf("a"),t.observe(mapOf("b" to 4,"c" to 5),4,0,true))
        assertTrue(t.observe(mapOf("a" to 3,"b" to 4),3,0,true).isEmpty())
        assertTrue(t.observe(mapOf("b" to 4),4,0,true).isEmpty())
        assertEquals(setOf("b","c"),t.observe(mapOf("z" to 30),30,0,true))
        assertFalse("unseen" in t.observe(emptyMap(),40,0,true))
    }
    @Test fun scrollReadIsOffByDefaultAndTabsAreSourceSpecific() {
        assertFalse(AppSettings().markReadOnScroll)
        val store=MemorySettingsStore();store.save(AppSettings(markReadOnScroll=true));assertTrue(store.load().markReadOnScroll)
        assertEquals(listOf("FreshRSS","FlipRSS","Search","Saved"),libraryDestinations(true))
        assertFalse("Topics" in libraryDestinations(true));assertFalse("Search" in libraryDestinations(false))
    }
    @Test fun commonFeedPathsWorkEvenWhenHomepageIsBlocked() = runBlocking {
        val client=OkHttpClient.Builder().addInterceptor { chain ->
            val feed=chain.request().url.encodedPath=="/feed"
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(if(feed)200 else 403).message("fixture")
                .body((if(feed) "<rss><channel><title>Accessible feed</title></channel></rss>" else "Blocked homepage").toResponseBody()).build()
        }.build()
        assertEquals(listOf("https://example.org/feed"),FeedDiscovery.find("example.org",FeedWeb(client)).map { it.url })
    }
}
