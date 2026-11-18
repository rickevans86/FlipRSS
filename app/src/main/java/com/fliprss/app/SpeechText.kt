package com.fliprss.app

/** Split at word boundaries where possible, never bisect a UTF-16 surrogate pair. */
object SpeechText {
    fun chunks(article: Article, limit: Int = 3500): List<String> {
        require(limit >= 2)
        val body = article.paragraphs.filter(String::isNotBlank).ifEmpty { listOf(article.summary) }
        return (listOf(article.title) + body).filter(String::isNotBlank).flatMap { paragraph ->
            val result = mutableListOf<String>()
            var start = 0
            while (start < paragraph.length) {
                var end = (start + limit).coerceAtMost(paragraph.length)
                if (end < paragraph.length) {
                    if (Character.isHighSurrogate(paragraph[end - 1]) && Character.isLowSurrogate(paragraph[end])) end--
                    val space = paragraph.lastIndexOf(' ', end - 1)
                    if (space > start + limit / 2) end = space
                }
                paragraph.substring(start, end).trim().takeIf(String::isNotEmpty)?.let(result::add)
                start = end
                while (start < paragraph.length && paragraph[start].isWhitespace()) start++
            }
            result
        }
    }
}
