package com.fliprss.app.data

import com.fliprss.app.Article
import com.fliprss.app.SpeechText
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class RenderedArticleTextTest {
    private val url = "https://www.theage.com.au/world/asia/trump-putin-and-xi-talk-nuclear-weapons-and-ukraine.html"
    private val nonce = "12345678-1234-1234-1234-123456789abc"
    private val article = Article("age/1", "News", "The Age - World", "Trump, Putin and Xi talk nuclear weapons and Ukraine",
        "Short feed excerpt", "", 1, listOf("The feed includes only a short summary."), originalUrl = url,
        feedId = "feed/age", initiallyStarred = true, demo = false)
    private val paragraphs = List(4) { "Paragraph ${it + 1}: This is the visible expanded article body, with reporting and background that the original feed excerpt did not contain. Readers should receive every one of these paragraphs." }
    private fun payload(actual: String = url, canonical: String = actual, title: String = "APEC: ${article.title}", token: String = nonce,
        body: Any = paragraphs) = Gson().toJson(mapOf("type" to "articleText", "nonce" to token, "url" to actual, "canonical" to canonical, "title" to title, "paragraphs" to body))
    @Test fun acceptsVisibleBrowserParagraphsAndPreservesArticleIdentityForSpeech() {
        val expanded = RenderedArticleText.parse(payload(), article, nonce, url)!!
        assertEquals(article, expanded.copy(paragraphs = article.paragraphs, minutes = article.minutes))
        assertEquals(paragraphs, expanded.paragraphs)
        assertTrue(SpeechText.chunks(expanded).joinToString().contains(paragraphs.last()))
    }
    @Test fun ignoresTrackingAndFragmentButPreservesQueryArticleIdentityAndHttps() {
        assertTrue(RenderedArticleText.samePage(url, url + "?utm_source=rss#paragraph3"))
        assertTrue(RenderedArticleText.samePage(url, url.replace("www.", "")))
        assertFalse(RenderedArticleText.samePage("https://example.com/story?id=1", "https://example.com/story?id=2"))
        assertFalse(RenderedArticleText.sameOrigin(url, url.replace("https:", "http:")))
        assertFalse(RenderedArticleText.sameOrigin("https://example.com:80/story", "https://example.com/story"))
        assertTrue(RenderedArticleText.sameOrigin("http://example.com/story", "https://example.com/story"))
    }
    @Test fun rejectsWrongNonceSenderPageForeignCanonicalAndHeadline() {
        assertNull(RenderedArticleText.parse(payload(token = "stale"), article, nonce, url))
        assertNull(RenderedArticleText.parse(payload(), article, nonce, "https://evil.example/story"))
        val another = url.replace("ukraine.html", "another-story.html")
        assertNull(RenderedArticleText.parse(payload(actual = another), article, nonce, another))
        val foreign = "https://evil.example/story"
        assertNull(RenderedArticleText.parse(payload(actual = foreign, canonical = url), article, nonce, foreign))
        assertNull(RenderedArticleText.parse(payload(title = "An unrelated sports report"), article, nonce, url))
    }
    @Test fun acceptsSamePublisherCanonicalAliasButNeverForeignOrigin() {
        val alias = "https://www.theage.com.au/mobile/amp-story.html"
        assertNotNull(RenderedArticleText.parse(payload(actual = alias, canonical = url), article, nonce, alias))
        assertFalse(RenderedArticleText.sameOrigin(url, "https://user:secret@www.theage.com.au/world/"))
    }
    @Test fun rejectsMalformedOversizedNonStringOrInsufficientText() {
        for (value in listOf("not-json", " ".repeat(1_000_001), payload(body = List(501) { "entry" }), payload(body = listOf(mapOf("hidden" to "object"))), payload(body = listOf("Only the feed summary")), payload(body = listOf("x".repeat(200_001), "another paragraph")))) {
            assertNull(RenderedArticleText.parse(value, article, nonce, url))
        }
    }
    @Test fun doesNotReplaceAlreadyLongerReaderText() {
        assertNull(RenderedArticleText.parse(payload(), article.copy(paragraphs = paragraphs + paragraphs), nonce, url))
    }
}
