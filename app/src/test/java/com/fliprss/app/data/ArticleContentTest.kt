package com.fliprss.app.data

import com.fliprss.app.Article
import com.fliprss.app.SpeechText
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleContentTest {
    private val story = Article("id", "Tech", "Publisher", "Story title", "Short excerpt.", "", 1,
        listOf("Short excerpt."), originalUrl = "https://example.org/story", demo = false, local = true, feedId = "local/1")
    private val paragraph = "Readers can now explore the complete public story within the application. This paragraph explains the announcement and gives the context that was missing from the short feed excerpt."
    private val html = "<main><article><div class='entry-content'><p>$paragraph</p><h2>More context</h2><p>The second part. $paragraph</p></div></article></main>"
    private fun page(text: String = html) = FeedPage(story.originalUrl!!, text)
    @Test fun extractsLongerBodyAndPreservesOriginAndState() {
        val expanded = ArticleContentExtractor.extract(page(), story)!!
        assertEquals(listOf(paragraph, "More context", "The second part. $paragraph"), expanded.paragraphs)
        assertEquals(story, expanded.copy(paragraphs = story.paragraphs, minutes = story.minutes))
    }
    @Test fun removesAdsNavigationHiddenTextAndScripts() {
        val polluted = html.replace("<h2>", "<aside><p>BUY AD</p></aside><div class='newsletter'><p>SUBSCRIBE</p></div><p hidden>HIDDEN</p><p style='display: none'>SECRET</p><script>TRACK</script><h2>")
        val text = ArticleContentExtractor.extract(page(polluted), story)!!.paragraphs.joinToString()
        for (bad in listOf("BUY AD","SUBSCRIBE","HIDDEN","SECRET","TRACK")) assertFalse(text.contains(bad))
    }
    @Test fun doesNotReplaceLongFeedWithShorterWebsite() {
        assertNull(ArticleContentExtractor.extract(page(), story.copy(paragraphs = List(10) { paragraph })))
    }
    @Test fun refusesRestrictedArticleAndGenericLandingPage() {
        assertNull(ArticleContentExtractor.extract(page("<script type='application/ld+json'>{\"isAccessibleForFree\": false}</script>" + html), story))
        assertNull(ArticleContentExtractor.extract(page("<body><nav><p>$paragraph</p></nav><p>$paragraph</p><p>$paragraph</p></body>"), story))
    }
    @Test fun preservesSeparateParagraphsWithoutNestedDuplicates() {
        val blocks = ArticleContentExtractor.extract(page(html.replace("<h2>More context</h2>", "<blockquote><p>A quoted statement. $paragraph</p></blockquote>")), story)!!.paragraphs
        assertEquals(3, blocks.size); assertEquals(1, blocks.count { "quoted statement" in it })
    }
    @Test fun manualLoadingCachesSuccessAndUsesExpandedTextForSpeech() = runTest {
        var calls = 0
        val controller = ArticleContentController(this, { calls++; page() }, StandardTestDispatcher(testScheduler))
        controller.open(story, false); advanceUntilIdle(); assertEquals(0, calls)
        controller.load(); advanceUntilIdle()
        val expanded = controller.state.value.display(story)
        assertEquals(paragraph, expanded.paragraphs.first()); assertTrue(SpeechText.chunks(expanded, 4000).joinToString().contains(paragraph))
        controller.close(); controller.open(story, true); advanceUntilIdle(); assertEquals(1, calls)
        assertEquals(expanded, controller.state.value.display(story))
    }
    @Test fun failureRetainsFeedAndCanRetry() = runTest {
        var fails = true
        val controller = ArticleContentController(this, { if (fails) error("offline") else page() }, StandardTestDispatcher(testScheduler))
        controller.open(story, true); advanceUntilIdle()
        assertTrue(controller.state.value.attempted); assertFalse(controller.state.value.loading)
        assertEquals(story, controller.state.value.display(story))
        fails = false; controller.load(); advanceUntilIdle(); assertNotEquals(story.paragraphs, controller.state.value.display(story).paragraphs)
    }
    @Test fun switchingArticlesDiscardsLateResponse() = runTest {
        val gate = CompletableDeferred<FeedPage>()
        val controller = ArticleContentController(this, { withContext(NonCancellable) { gate.await() } }, StandardTestDispatcher(testScheduler))
        controller.open(story, true); runCurrent()
        val other = story.copy(id = "another", originalUrl = "https://example.org/other")
        controller.open(other, false); gate.complete(page()); advanceUntilIdle()
        assertEquals(other.contentKey(), controller.state.value.key)
        assertNull(controller.state.value.expanded); assertEquals(other, controller.state.value.display(other))
    }
    @Test fun demoDoesNotFetchAndClosingResetsState() = runTest {
        var calls = 0
        val controller = ArticleContentController(this, { calls++; page() }, StandardTestDispatcher(testScheduler))
        controller.open(story.copy(demo = true), true); advanceUntilIdle(); assertEquals(0, calls)
        controller.close(); assertNull(controller.state.value.key)
    }
}
