package com.fliprss.app.data

import com.fliprss.app.Article
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

internal fun localHash(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
internal data class LocalFeed(val url: String, val title: String, val id: String) {
    val category get() = LABEL_PREFIX + id.removePrefix("feed/local/")
}
internal data class LocalEntry(val article: Article, val discoveredMicros: Long)
internal data class LocalFeedData(val feeds: List<LocalFeed> = emptyList(), val entries: List<LocalEntry> = emptyList(),
    val read: Set<String> = emptySet(), val saved: Set<String> = emptySet())
interface LocalFeedStore { fun read(): String?; fun write(json: String) }
class FileLocalFeedStore(private val file: File) : LocalFeedStore {
    override fun read(): String? = if (file.exists()) file.readText() else null
    override fun write(json: String) {
        file.parentFile?.mkdirs()
        val temporary = File(file.path + ".tmp")
        temporary.outputStream().use { it.write(json.toByteArray(Charsets.UTF_8)); it.fd.sync() }
        Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }
}

/** XML parser does not resolve external entities. Feed HTML is converted by the existing safe mapper. */
internal object LocalFeedParser {
    fun parse(xml: String, url: String, nowMicros: Long, baseUrl: String = url): Pair<LocalFeed, List<LocalEntry>> {
        val document = Jsoup.parse(xml, baseUrl, Parser.xmlParser())
        val root = document.children().firstOrNull { it.normalName() in setOf("rss", "feed", "rdf:rdf") }
            ?: throw FreshRssProblem("This URL is not an RSS or Atom feed. Enter its XML feed URL.")
        val atom = root.normalName() == "feed"
        val container = if (atom) root else root.children().firstOrNull { it.normalName() == "channel" } ?: root
        val title = container.children().firstOrNull { it.normalName() == "title" }?.text().orEmpty()
            .ifBlank { url.toHttpUrlOrNull()?.host ?: "RSS feed" }
        val feed = LocalFeed(url, Jsoup.parse(title).text(), "feed/local/" + localHash(url))
        val subscription = Subscription(feed.id, feed.title, baseUrl, listOf(Tag(feed.category, feed.title)))
        val entries = root.getElementsByTag(if (atom) "entry" else "item").take(500).mapNotNull { node ->
            fun child(vararg names: String) = node.children().firstOrNull { it.normalName() in names }
            val link = if (atom) node.children().firstOrNull { it.normalName() == "link" && it.attr("rel") in listOf("", "alternate") }
                ?.attr("href") else child("link")?.text()
            val original = ArticleMapper.safeUrl(link, baseUrl)
            val headline = child("title")?.text().orEmpty()
            val contentNode = child("content:encoded", "content") ?: child("description", "summary")
            val html = if (atom && contentNode?.attr("type") == "xhtml") contentNode.html() else contentNode?.text().orEmpty()
            val identity = child("guid", "id")?.text()?.takeIf(String::isNotBlank) ?: original
                ?: (headline + "\n" + html).takeIf(String::isNotBlank) ?: return@mapNotNull null
            val id = "local/" + localHash(feed.id + "\n" + identity)
            val enclosure = node.children().firstOrNull { it.normalName() == "enclosure" && it.attr("type").startsWith("image/") }
            val mapped = ArticleMapper.map(Item(id = id, title = headline, content = Content(html),
                alternate = original?.let { listOf(Link(it)) },
                enclosure = enclosure?.let { listOf(Link(it.attr("url"), it.attr("type"))) },
                origin = Origin(feed.id, feed.title, baseUrl)), mapOf(feed.id to subscription), baseUrl)
            mapped?.copy(topic = feed.title, local = true)?.let { LocalEntry(it, nowMicros) }
        }.distinctBy { it.article.id }
        return feed to entries
    }
}

/** On-device subscriptions and reading state; never shares auth or data with FreshRSS. */
class LocalFeedRepository(private val store: LocalFeedStore,
    private val http: OkHttpClient = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build(),
    private val nowMillis: () -> Long = System::currentTimeMillis) : FreshRssRepository {
    private val mutex = Mutex()
    private val gson = Gson()
    private var data: LocalFeedData? = null
    private suspend fun <T> access(block: () -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (data == null) {
                try {
                    val json = store.read()
                    data = if (json == null) LocalFeedData() else gson.fromJson(json, LocalFeedData::class.java)
                        ?: throw IOException("Invalid local data")
                }
                catch (_: Exception) { throw FreshRssProblem("Could not read local feeds. Your saved data has been kept.") }
            }
            block()
        }
    }
    private fun commit(value: LocalFeedData) { store.write(gson.toJson(value)); data = value }
    private fun validated(url: String): String {
        val parsed = url.trim().toHttpUrlOrNull() ?: throw FreshRssProblem("Enter a valid HTTP or HTTPS feed URL.")
        if (parsed.username.isNotEmpty() || parsed.password.isNotEmpty()) throw FreshRssProblem("Feed URLs must not contain a username or password.")
        return parsed.newBuilder().fragment(null).build().toString()
    }
    private fun fetch(url: String): Pair<LocalFeed, List<LocalEntry>> {
        var current = validated(url)
        repeat(6) {
            http.newCall(Request.Builder().url(current).header("Accept", "application/atom+xml, application/rss+xml, application/xml, text/xml").build())
                .execute().use { response ->
                    if (response.code in 300..399) {
                        val next = response.header("Location")?.let { response.request.url.resolve(it) }
                            ?: throw FreshRssProblem("Feed redirect has no valid destination.")
                        if (response.request.url.isHttps && !next.isHttps) throw FreshRssProblem("Feed redirected from HTTPS to unencrypted HTTP.")
                        current = validated(next.toString())
                    } else {
                        if (!response.isSuccessful) throw FreshRssProblem("Feed could not be downloaded (HTTP ${response.code}).")
                        val body = response.body ?: throw FreshRssProblem("Feed response was empty.")
                        if (body.contentLength() > 5_000_000) throw FreshRssProblem("Feed is too large (maximum 5 MB).")
                        val source = body.source()
                        if (source.request(5_000_001)) throw FreshRssProblem("Feed is too large (maximum 5 MB).")
                        // Keep the subscription's original URL and stable IDs across redirects.
                        return LocalFeedParser.parse(body.string(), url, nowMillis() * 1000, response.request.url.toString())
                    }
                }
        }
        throw FreshRssProblem("Feed has too many redirects.")
    }
    private fun merged(old: LocalFeedData, feed: LocalFeed, incoming: List<LocalEntry>): LocalFeedData {
        val previous = old.entries.associateBy { it.article.id }
        val updated = incoming.map { entry -> entry.copy(discoveredMicros = previous[entry.article.id]?.discoveredMicros ?: entry.discoveredMicros) }
        val entries = (updated + old.entries).distinctBy { it.article.id }
        // Bound each feed's cache, retaining saved stories outside the latest 500.
        val keep = entries.filter { it.article.feedId == feed.id }.take(500).map { it.article.id }.toSet() + old.saved
        return old.copy(feeds = old.feeds.filterNot { it.id == feed.id } + feed,
            entries = entries.filter { it.article.feedId != feed.id || it.article.id in keep })
    }
    suspend fun refreshFeeds(): String? = access {
        var failed = 0
        data!!.feeds.toList().forEach { feed ->
            try { val (updated, entries) = fetch(feed.url); commit(merged(data!!, updated, entries)) }
            catch (_: IOException) { failed++ }
            catch (_: FreshRssProblem) { failed++ }
        }
        if (failed > 0) "$failed feed(s) could not refresh. Showing saved articles; try again later." else null
    }
    override suspend fun addFeed(url: String): String = access {
        val normalized = validated(url)
        if (data!!.feeds.any { it.url == normalized }) throw FreshRssProblem("This feed is already in your local library.")
        val (feed, entries) = fetch(normalized)
        commit(merged(data!!, feed, entries)); feed.id
    }
    internal suspend fun subscriptions(): List<LocalFeed> = access { data!!.feeds.toList() }
    override suspend fun catalogue(): Catalogue = access {
        val value = data!!
        val sections = value.feeds.flatMap { feed ->
            val count = value.entries.count { it.article.feedId == feed.id && it.article.id !in value.read }
            listOf(Section(feed.category, feed.title, unread = count), Section(feed.id, feed.title, feed = true, unread = count))
        }
        Catalogue(sections, value.entries.count { it.article.id !in value.read })
    }
    private fun matching(entry: LocalEntry, stream: String, value: LocalFeedData): Boolean = when (stream) {
        READING_LIST -> true
        STARRED -> entry.article.id in value.saved
        else -> entry.article.feedId == stream || stream in entry.article.topicIds
    }
    override suspend fun page(stream: String, continuation: String?, unreadOnly: Boolean): ArticlePage = access {
        val value = data!!
        val selected = value.entries.filter { matching(it, stream, value) && (!unreadOnly || it.article.id !in value.read) }
        // Local cache is bounded and delivered as a snapshot, avoiding offset skips after read changes.
        ArticlePage(selected.map { it.article.copy(local = true, initiallyRead = it.article.id in value.read, initiallyStarred = it.article.id in value.saved) }, null)
    }
    override suspend fun setRead(id: String, read: Boolean) { access { val old = data!!; commit(old.copy(read = if (read) old.read + id else old.read - id)) } }
    override suspend fun setStarred(id: String, starred: Boolean) { access { val old = data!!; commit(old.copy(saved = if (starred) old.saved + id else old.saved - id)) } }
    override suspend fun markAllRead(stream: String, cutoffMicros: Long) { access {
        val old = data!!
        commit(old.copy(read = old.read + old.entries.filter { matching(it, stream, old) && it.discoveredMicros <= cutoffMicros }.map { it.article.id }))
    } }
}
