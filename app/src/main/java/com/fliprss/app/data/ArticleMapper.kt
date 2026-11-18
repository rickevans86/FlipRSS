package com.fliprss.app.data

import com.fliprss.app.Article
import com.google.gson.JsonElement
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup
import kotlin.math.ceil

internal object ArticleMapper {
    fun map(item: Item, subscriptions: Map<String, Subscription>, endpoint: String): Article? {
        val id = item.id?.takeIf(String::isNotBlank) ?: return null
        val subscription = subscriptions[item.origin?.streamId]
        val base = safeUrl(item.origin?.htmlUrl, endpoint) ?: safeUrl(subscription?.htmlUrl, endpoint) ?: endpoint
        val original = (item.canonical.orEmpty() + item.alternate.orEmpty()).firstNotNullOfOrNull { safeUrl(it.href, base) }
        val html = item.content?.content ?: item.summary?.content.orEmpty()
        val document = Jsoup.parse(html, original ?: base)
        document.select("script,style,iframe,object,embed,form,noscript").remove()
        val text = document.body().text()
        val blocks = setOf("p", "h1", "h2", "h3", "h4", "li", "pre", "blockquote")
        val paragraphs = document.select(blocks.joinToString(",")).filter { element ->
            element.parents().none { it.tagName() in blocks }
        }.map { it.text() }.filter(String::isNotBlank).ifEmpty { listOf(text).filter(String::isNotBlank) }
        val enclosures = item.enclosure.orEmpty().filter { it.type?.startsWith("image/") == true }
            .firstNotNullOfOrNull { safeUrl(it.href, original ?: base) }
        val image = enclosures ?: mediaUrl(item.mediaContent, original ?: base)
            ?: mediaUrl(item.mediaGroup, original ?: base)
            ?: document.select("img").firstNotNullOfOrNull {
                safeUrl(it.attr("src").ifBlank { it.attr("data-src") }, original ?: base)
            }
        val categoryIds = item.categories.orEmpty()
        val topics = (categoryIds + subscription?.categories.orEmpty().mapNotNull { it.id })
            .filter { "/label/" in it }.distinct()
        return Article(id = id, topic = topics.firstOrNull()?.substringAfter("/label/") ?: "FreshRSS",
            source = plain(item.origin?.title ?: subscription?.title ?: "FreshRSS"),
            title = plain(item.title.orEmpty()).ifBlank { "Untitled article" }, summary = text.take(220),
            image = "", minutes = ceil(text.split(Regex("\\s+")).size / 200.0).toInt().coerceAtLeast(1),
            paragraphs = paragraphs, imageUrl = image, originalUrl = original,
            topicIds = topics, feedId = item.origin?.streamId,
            initiallyRead = categoryIds.any { it.endsWith("/state/com.google/read") },
            initiallyStarred = categoryIds.any { it.endsWith("/state/com.google/starred") }, demo = false)
    }

    fun safeUrl(value: String?, base: String): String? {
        if (value.isNullOrBlank()) return null
        val url = base.toHttpUrlOrNull()?.resolve(value.trim()) ?: return null
        return url.takeIf { it.username.isEmpty() && it.password.isEmpty() }?.toString()
    }

    private fun plain(value: String) = Jsoup.parse(value).text()
    private fun mediaUrl(element: JsonElement?, base: String): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonArray) return element.asJsonArray.firstNotNullOfOrNull { mediaUrl(it, base) }
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject
        for (key in listOf("url", "href")) {
            val value = obj.get(key)
            if (value?.isJsonPrimitive == true) safeUrl(value.asString, base)?.let { return it }
        }
        return obj.entrySet().filter { it.key.contains("content") }.firstNotNullOfOrNull { mediaUrl(it.value, base) }
    }
}
