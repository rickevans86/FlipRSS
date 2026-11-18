package com.fliprss.app.data

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class ArticleMapperTest {
    @Test fun imagePriorityAndUnsafeContentAreHandled() {
        val item = Item(id = "one", title = "Title", origin = Origin(htmlUrl = "https://example.org/"),
            summary = Content("<style>hidden</style><iframe>hidden</iframe><p>Visible</p><img src='/fallback.jpg'>"),
            enclosure = listOf(Link("javascript:alert(1)", "image/jpeg"), Link("/enclosure.jpg", "image/jpeg")),
            mediaContent = JsonParser.parseString("""{"url":"/media.jpg"}"""))
        val mapped = ArticleMapper.map(item, emptyMap(), "https://example.org/api/greader.php/")!!
        assertEquals("https://example.org/enclosure.jpg", mapped.imageUrl)
        assertEquals(listOf("Visible"), mapped.paragraphs)
        assertEquals("https://example.org/media.jpg", ArticleMapper.map(item.copy(enclosure = null), emptyMap(), "https://example.org/")!!.imageUrl)
        assertEquals("https://example.org/fallback.jpg", ArticleMapper.map(item.copy(enclosure = null, mediaContent = null), emptyMap(), "https://example.org/")!!.imageUrl)
        val lazyImage = item.copy(enclosure = null, mediaContent = null,
            summary = Content("<p>Body</p><img src='data:image/gif;base64,AA' data-src='/actual.jpg'>"))
        assertEquals("https://example.org/actual.jpg", ArticleMapper.map(lazyImage, emptyMap(), "https://example.org/")!!.imageUrl)
        assertNull(ArticleMapper.safeUrl("javascript:alert(1)", "https://example.org/"))
        assertNull(ArticleMapper.safeUrl("https://user:password@example.org/image", "https://example.org/"))
    }
    @Test fun malformedIdsAreSkippedAndNestedParagraphsNotDuplicated() {
        assertNull(ArticleMapper.map(Item(id = ""), emptyMap(), "https://example.org/"))
        val mapped = ArticleMapper.map(Item(id = "one", summary = Content("<blockquote><p>Quote</p></blockquote><ul><li>Item</li></ul>")),
            emptyMap(), "https://example.org/")!!
        assertEquals(listOf("Quote", "Item"), mapped.paragraphs)
    }
}
