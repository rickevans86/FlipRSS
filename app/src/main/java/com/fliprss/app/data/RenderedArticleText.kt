package com.fliprss.app.data

import com.fliprss.app.Article
import com.google.gson.JsonParser
import java.util.Locale
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlin.math.ceil

/** Native-side validation for the owned visible-DOM bridge. Never trusts a page's claimed identity alone. */
internal object RenderedArticleText {
    private fun address(value: String) = value.takeIf { it.length <= 8192 }?.toHttpUrlOrNull()
        ?.takeIf { it.username.isEmpty() && it.password.isEmpty() }
    fun sameOrigin(expected: String, actual: String): Boolean {
        val a = address(expected) ?: return false; val b = address(actual) ?: return false
        return a.host.removePrefix("www.") == b.host.removePrefix("www.") &&
            (a.port == b.port || (a.port == (if (a.isHttps) 443 else 80) && b.port == (if (b.isHttps) 443 else 80))) && !(a.isHttps && !b.isHttps)
    }
    fun samePage(expected: String, actual: String): Boolean {
        val a = address(expected) ?: return false; val b = address(actual) ?: return false
        fun query(url: okhttp3.HttpUrl) = url.queryParameterNames.filterNot {
            it.startsWith("utm_", true) || it in setOf("fbclid", "gclid", "mc_cid", "mc_eid")
        }.associateWith { url.queryParameterValues(it).sortedBy { value -> value.orEmpty() } }
        return sameOrigin(expected, actual) && a.encodedPath.trimEnd('/') == b.encodedPath.trimEnd('/') && query(a) == query(b)
    }
    private fun headlineMatches(expected: String, actual: String): Boolean {
        val stop = setOf("the", "and", "a", "an", "of", "to", "in", "on", "for", "with", "is")
        fun words(value: String) = Regex("[\\p{L}\\p{N}]+").findAll(value.lowercase(Locale.ROOT)).map { it.value }.filterNot { it in stop }.toSet()
        val a = words(expected); val b = words(actual)
        return a.isNotEmpty() && a.intersect(b).size >= ceil(a.size * .6).toInt()
    }
    fun enrich(article: Article, paragraphs: List<String>): Article? {
        val count = paragraphs.sumOf(String::length); val previous = article.paragraphs.sumOf(String::length)
        if (paragraphs.size !in 2..500 || paragraphs.any { it.isBlank() } || count !in 300..200_000 ||
            count < previous + maxOf(150, previous / 10)) return null
        return article.copy(paragraphs = paragraphs, minutes = ceil(paragraphs.joinToString(" ").split(Regex("\\s+")).size / 200.0).toInt().coerceAtLeast(1))
    }
    fun parse(json: String, article: Article, nonce: String, trustedDocumentUrl: String): Article? = runCatching {
        if (json.length > 1_000_000) return null
        val obj = JsonParser.parseString(json).asJsonObject
        if (obj["type"]?.asString != "articleText" || obj["nonce"]?.asString != nonce) return null
        val expected = article.originalUrl ?: return null
        val actual = obj["url"]?.asString ?: return null
        val canonical = obj["canonical"]?.asString.orEmpty()
        if (!samePage(trustedDocumentUrl, actual) || !sameOrigin(expected, actual) ||
            !(samePage(expected, actual) || samePage(expected, canonical)) ||
            !headlineMatches(article.title, obj["title"]?.asString.orEmpty())) return null
        val array = obj["paragraphs"]?.asJsonArray ?: return null
        if (array.size() !in 2..500) return null
        val paragraphs = array.map { value ->
            if (!value.isJsonPrimitive || !value.asJsonPrimitive.isString) return null
            value.asString.replace(Regex("[\\p{Cc}&&[^\\t\\n\\r]]"), "").replace(Regex("\\s+"), " ").trim()
        }
        enrich(article, paragraphs)
    }.getOrNull()
}
