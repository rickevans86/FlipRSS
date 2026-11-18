package com.fliprss.app

import org.junit.Assert.*
import org.junit.Test

class SpeechTextTest {
    @Test fun readsTitleAndBodyWithoutRepeatingSummary() {
        val article = DemoLibrary.articles.first().copy(title = "Headline", summary = "Duplicate summary", paragraphs = listOf("First paragraph", "Second paragraph"))
        assertEquals(listOf("Headline", "First paragraph", "Second paragraph"), SpeechText.chunks(article))
        assertEquals(listOf("Headline", "Duplicate summary"), SpeechText.chunks(article.copy(paragraphs = emptyList())))
    }
    @Test fun longUnicodeTextIsNotLostOrSplitInsideSurrogatePairs() {
        val text = "abc😀".repeat(3000)
        val chunks = SpeechText.chunks(DemoLibrary.articles.first().copy(title = "", paragraphs = listOf(text)), 99)
        assertEquals(text, chunks.joinToString(""))
        chunks.forEach { assertTrue(it.length <= 99); assertFalse(Character.isHighSurrogate(it.last())); assertFalse(Character.isLowSurrogate(it.first())) }
    }
    @Test fun whitespaceAndEmptyArticlesAreHandled() {
        val article = DemoLibrary.articles.first().copy(title = " ", summary = "", paragraphs = listOf("  "))
        assertTrue(SpeechText.chunks(article).isEmpty())
        val words = "Read these words in order. ".repeat(1000)
        val chunks = SpeechText.chunks(article.copy(paragraphs = listOf(words)), 100)
        assertEquals(words.trim().split(Regex("\\s+")), chunks.flatMap { it.split(Regex("\\s+")) })
    }
}
