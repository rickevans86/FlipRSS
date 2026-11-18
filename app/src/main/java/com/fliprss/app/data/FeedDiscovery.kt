package com.fliprss.app.data

import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

internal data class FeedPage(val url: String, val text: String)
internal fun normalizedFeedUrl(value: String, base: String? = null): String? {
    val url = if (base == null) value.trim().toHttpUrlOrNull() else base.toHttpUrlOrNull()?.resolve(value.trim())
    return url?.takeIf { it.username.isEmpty() && it.password.isEmpty() }?.newBuilder()?.fragment(null)?.build()?.toString()
}
internal class FeedWeb(private val client: OkHttpClient = OkHttpClient.Builder().followRedirects(false)
    .followSslRedirects(false).connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
    .callTimeout(20, TimeUnit.SECONDS).build()) {
    suspend fun get(address: String): FeedPage = withContext(Dispatchers.IO) {
        var url = normalizedFeedUrl(address) ?: throw FreshRssProblem("Enter a valid HTTP or HTTPS URL without embedded credentials.")
        repeat(6) {
            client.newCall(Request.Builder().url(url).header("User-Agent", "FlipRSS/0.8 FeedDiscovery")
                .header("Accept", "application/rss+xml, application/atom+xml, text/html, application/xml").build()).execute().use { response ->
                if (response.code in 300..399) {
                    val next = response.header("Location")?.let { normalizedFeedUrl(it, url) }
                        ?: throw FreshRssProblem("Invalid website redirect.")
                    if (url.startsWith("https:") && !next.startsWith("https:")) throw FreshRssProblem("Website redirected to unencrypted HTTP.")
                    url = next
                } else {
                    if (!response.isSuccessful) throw FreshRssProblem("Website could not be fetched (HTTP ${response.code}).")
                    val body = response.body ?: throw FreshRssProblem("Website returned no content.")
                    if (body.contentLength() > 4_000_000 || body.source().request(4_000_001)) throw FreshRssProblem("Website response is too large.")
                    return@withContext FeedPage(response.request.url.toString(), body.string())
                }
            }
        }
        throw FreshRssProblem("Too many website redirects.")
    }
}
internal object FeedDiscovery {
    fun direct(page: FeedPage): PopularFeed? {
        val doc = Jsoup.parse(page.text, page.url, Parser.xmlParser())
        val root = doc.children().firstOrNull { it.normalName() in setOf("rss", "feed", "rdf:rdf") } ?: return null
        val title = root.getElementsByTag("title").firstOrNull()?.text().orEmpty().ifBlank { page.url.toHttpUrlOrNull()?.host ?: "RSS feed" }
        return PopularFeed(localHash(page.url), Jsoup.parse(title).text(), "Discovered", "RSS/Atom feed found on this website.", page.url)
    }
    fun candidates(page: FeedPage): List<String> {
        val doc = Jsoup.parse(page.text, page.url)
        val base = normalizedFeedUrl(doc.selectFirst("base[href]")?.attr("href").orEmpty(), page.url) ?: page.url
        val links = doc.select("link[href], a[href]").filter {
            (it.tagName() == "link" && it.attr("rel").split(' ').any { rel -> rel.equals("alternate", true) } &&
                it.attr("type").substringBefore(';').trim().lowercase() in setOf("application/rss+xml", "application/atom+xml", "application/rdf+xml")) ||
                (it.tagName() == "a" && (it.text().contains("RSS", true) || it.attr("title").contains("RSS", true) || it.attr("aria-label").contains("RSS", true) ||
                    it.select("img[alt]").any { image -> image.attr("alt").contains("RSS", true) } ||
                    Regex("(?i)(/(feeds?|rss|atom)([/?.]|$)|\\.(rss|xml|atom)([?#]|$))").containsMatchIn(it.attr("href"))))
        }.mapNotNull { normalizedFeedUrl(it.attr("href"), base) }
        val paths = listOf("/feed", "/rss", "/rss.xml", "/atom.xml", "/feed.xml", "feed", "rss")
            .mapNotNull { normalizedFeedUrl(it, page.url) }
        return (links + paths).filterNot { it == page.url }.distinct().take(16)
    }
    suspend fun find(address: String, web: FeedWeb = FeedWeb()): List<PopularFeed> = coroutineScope {
        val url = if (address.contains("://")) address else "https://" + address.trim()
        val normalized = normalizedFeedUrl(url) ?: throw FreshRssProblem("Enter a valid website or feed URL.")
        var homepageFailure: Exception? = null
        val page = try { web.get(normalized) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (failure: Exception) { homepageFailure = failure; FeedPage(normalized, "") }
        direct(page)?.let { return@coroutineScope listOf(it) }
        val semaphore = Semaphore(4)
        val found = candidates(page).map { candidate -> async {
            semaphore.withPermit {
                try { direct(web.get(candidate)) }
                catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                catch (_: Exception) { null }
            }
        } }.awaitAll().filterNotNull().distinctBy { it.url }
        if (found.isEmpty() && homepageFailure != null) throw FreshRssProblem("Could not reach this website or find an accessible feed.")
        found
    }
}
internal object DirectoryImporter {
    val sources = linkedMapOf(
        "Feeder" to "https://feeder.co/knowledge-base/rss-content/rss-lists-popular-and-useful-rss-feeds/",
        "Feedspot" to "https://rss.feedspot.com/best_rss_feeds/",
    )
    fun parse(name: String, page: FeedPage): List<PopularFeed> {
        val doc = Jsoup.parse(page.text, page.url)
        val entries = mutableListOf<PopularFeed>()
        fun add(address: String, title: String) {
            val url = normalizedFeedUrl(address, page.url) ?: return
            if (url.toHttpUrlOrNull()?.host in setOf("feeder.co", "www.feeder.co", "feedspot.com", "www.feedspot.com", "rss.feedspot.com")) return
            val label = title.replace(Regex("^\\s*\\d+[.)]\\s*"), "").replace(Regex("(?i)\\s*RSS Feed\\s*$"), "").trim()
                .ifBlank { url.toHttpUrlOrNull()?.host ?: "RSS feed" }
            entries += PopularFeed(localHash(url), label.take(180), name, "From the $name RSS list.", url)
        }
        if (name == "Feeder") {
            doc.select("tr").forEach { row ->
                row.select("code").forEach { code -> add(code.text(), row.selectFirst("td")?.text().orEmpty()) }
            }
            doc.select("a[href]").forEach { a ->
                val link = a.absUrl("href").toHttpUrlOrNull()
                link?.queryParameter("url")?.let { add(it, a.text()) }
            }
        } else {
            // Feedspot places many headings/rows directly under one common parent.
            // Never search an ancestor's descendants: that assigns the first name to every row.
            doc.select("[data-site], p.trow").forEach { row ->
                val numericId = row.attr("data").takeIf { it.matches(Regex("[0-9]+")) }
                    ?: row.id().removePrefix("row").takeIf { it.matches(Regex("[0-9]+")) }
                val heading = if (numericId != null) doc.getElementById("h$numericId")?.takeIf { it.normalName() == "h3" }
                    else row.previousElementSibling()?.takeIf { it.normalName() == "h3" }
                        ?: row.parent()?.takeIf { parent -> parent.select("h3").size == 1 && parent.select("[data-site], p.trow").size == 1 }
                            ?.selectFirst("h3")
                val title = heading?.text()?.takeIf(String::isNotBlank) ?: return@forEach
                val encoded = row.attr("data-site")
                val address = if (encoded.isNotBlank()) runCatching { URLDecoder.decode(encoded, "UTF-8") }.getOrNull()
                    else row.selectFirst("a.ext[href]:not(.extdomain)")?.attr("href")
                if (address != null) add(address, title)
            }
        }
        return entries.distinctBy { it.url }.take(1000)
    }
}
