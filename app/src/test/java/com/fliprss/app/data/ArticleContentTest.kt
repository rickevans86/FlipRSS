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
    @Test fun browserFallbackRunsAfterHttpFailureAndFeedsSpeech() = runTest {
        val full = story.copy(paragraphs = List(5) { "$it $paragraph" })
        val controller = ArticleContentController(this, { error("HTTP 403") }, StandardTestDispatcher(testScheduler),
            render = { full }, autoBrowser = { true })
        controller.open(story, true); advanceUntilIdle()
        assertTrue(controller.state.value.viaBrowser)
        assertEquals(full.paragraphs, controller.state.value.display(story).paragraphs)
        assertTrue(SpeechText.chunks(controller.state.value.display(story)).joinToString().contains(paragraph))
    }
    @Test fun browserCanExpandAlreadyImprovedStaticTextAndKeepsItWhileLoading() = runTest {
        val gate = CompletableDeferred<Article?>(); var baseline: Article? = null
        val controller = ArticleContentController(this, { page() }, StandardTestDispatcher(testScheduler),
            render = { baseline = it; gate.await() }, autoBrowser = { true })
        controller.open(story, true); runCurrent()
        assertTrue(controller.state.value.browserLoading); assertTrue(controller.state.value.loading)
        assertEquals(paragraph, controller.state.value.display(story).paragraphs.first())
        assertEquals(controller.state.value.display(story).paragraphs, baseline!!.paragraphs)
        gate.complete(baseline!!.copy(paragraphs = baseline!!.paragraphs + List(4) { paragraph })); advanceUntilIdle()
        assertTrue(controller.state.value.viaBrowser); assertFalse(controller.state.value.loading)
    }
    @Test fun failedBrowserKeepsSuccessfulStaticText() = runTest {
        val controller = ArticleContentController(this, { page() }, StandardTestDispatcher(testScheduler),
            render = { error("browser offline") }, autoBrowser = { true })
        controller.open(story, true); advanceUntilIdle()
        assertEquals(paragraph, controller.state.value.display(story).paragraphs.first())
        assertFalse(controller.state.value.loading); assertFalse(controller.state.value.viaBrowser)
    }
    @Test fun explicitBrowserLoadWorksWithAutoOffAndDoesNotFetchStaticAgain() = runTest {
        var fetches = 0; var renders = 0
        val full = story.copy(paragraphs = List(4) { paragraph })
        val controller = ArticleContentController(this, { fetches++; page() }, StandardTestDispatcher(testScheduler),
            render = { renders++; full }, autoBrowser = { false })
        controller.open(story, false); controller.load(browserOnly = true); advanceUntilIdle()
        assertEquals(0, fetches); assertEquals(1, renders); assertTrue(controller.state.value.viaBrowser)
    }
    @Test fun manualImportCancelsPendingRenderAndCannotBeOverwrittenByLateReply() = runTest {
        val gate = CompletableDeferred<Article?>()
        val controller = ArticleContentController(this, { error("short feed") }, StandardTestDispatcher(testScheduler),
            render = { withContext(NonCancellable) { gate.await() } }, autoBrowser = { true })
        controller.open(story, true); runCurrent()
        val approved = story.copy(paragraphs = List(8) { "$it $paragraph" })
        assertTrue(controller.importBrowser(story, approved))
        gate.complete(story.copy(paragraphs = List(3) { paragraph })); advanceUntilIdle()
        assertEquals(approved.paragraphs, controller.state.value.display(story).paragraphs)
        assertTrue(controller.state.value.viaBrowser)
    }
    @Test fun staleForeignAndShorterImportsCannotReplaceTheSelectedStory() = runTest {
        val controller = ArticleContentController(this, { page() }, StandardTestDispatcher(testScheduler))
        controller.open(story, true); advanceUntilIdle()
        val selected = controller.state.value.display(story)
        assertFalse(controller.importBrowser(story, story.copy(paragraphs = listOf("short"))))
        assertFalse(controller.importBrowser(story, selected.copy(id = "other")))
        assertEquals(selected.paragraphs, controller.state.value.display(story).paragraphs)
        val other = story.copy(id = "another"); controller.open(other, false)
        assertFalse(controller.importBrowser(story, selected.copy(paragraphs = List(10) { paragraph })))
        assertEquals(other, controller.state.value.display(other))
    }
    @Test fun wrongBrowserArticleIsNeverAcceptedAndClosingCancelsItsLoad() = runTest {
        val controller = ArticleContentController(this, { error("offline") }, StandardTestDispatcher(testScheduler),
            render = { story.copy(id = "foreign", paragraphs = List(5) { paragraph }) }, autoBrowser = { true })
        controller.open(story, true); advanceUntilIdle()
        assertEquals(story, controller.state.value.display(story)); assertNull(controller.state.value.expanded)
        controller.close(); assertNull(controller.state.value.key)
    }

    @Test fun publicBrowserFallbackWorksWithoutAddonWhenStaticHasNoText() = runTest {
        var renders = 0
        val controller = ArticleContentController(this, { error("needs JavaScript") }, StandardTestDispatcher(testScheduler),
            render = { renders++; story.copy(paragraphs = List(4) { paragraph }) }, autoBrowser = { true }, expandWithBrowser = { false })
        controller.open(story, true); advanceUntilIdle()
        assertEquals(1, renders); assertTrue(controller.state.value.viaBrowser)
    }
    @Test fun alreadyExpandedPublicArticleDoesNotStartBrowserWithoutAddon() = runTest {
        var renders = 0
        val controller = ArticleContentController(this, { page() }, StandardTestDispatcher(testScheduler),
            render = { renders++; null }, autoBrowser = { true }, expandWithBrowser = { false })
        controller.open(story, true); advanceUntilIdle()
        assertEquals(0, renders); assertEquals(paragraph, controller.state.value.display(story).paragraphs.first())
    }

}
