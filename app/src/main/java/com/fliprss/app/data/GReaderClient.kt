package com.fliprss.app.data

import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FreshRssEndpoint {
    fun normalize(server: String, allowHttp: Boolean): String {
        val url = server.trim().toHttpUrlOrNull()
            ?: throw FreshRssProblem("Enter a valid FreshRSS instance URL, including https://.")
        if (url.scheme != "https" && !allowHttp) throw FreshRssProblem("Use HTTPS or explicitly allow HTTP for your local server.")
        if (url.username.isNotEmpty() || url.password.isNotEmpty() || url.query != null || url.fragment != null) {
            throw FreshRssProblem("Use the instance URL without embedded credentials, query or fragment.")
        }
        var path = url.encodedPath.trimEnd('/')
        if (path.endsWith("/reader/api/0")) path = path.removeSuffix("/reader/api/0")
        if (!path.endsWith("/api/greader.php")) path += "/api/greader.php"
        return url.newBuilder().encodedPath("$path/").build().toString()
    }

    internal fun streamPath(stream: String): String {
        val prefix = when {
            stream.startsWith(LABEL_PREFIX) -> LABEL_PREFIX
            stream.startsWith("feed/") -> "feed/"
            stream == READING_LIST || stream == STARRED -> return stream
            else -> throw FreshRssProblem("This server returned an unsupported stream identifier.")
        }
        return prefix + URLEncoder.encode(stream.removePrefix(prefix), "UTF-8").replace("+", "%20")
    }
}

class GReaderConnector(private val client: OkHttpClient = OkHttpClient.Builder()
    .followRedirects(false).followSslRedirects(false)
    .connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
    .callTimeout(45, TimeUnit.SECONDS).build()) : FreshRssConnector {
    private fun api(endpoint: String): GReaderApi = Retrofit.Builder().baseUrl(endpoint).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(GReaderApi::class.java)

    override suspend fun login(server: String, username: String, password: String, allowHttp: Boolean): FreshRssSession {
        if (username.isBlank() || password.isEmpty()) throw FreshRssProblem("Enter your username and FreshRSS API password.")
        val endpoint = FreshRssEndpoint.normalize(server, allowHttp)
        val text = api(endpoint).login(username.trim(), password).text(login = true)
        val token = text.lineSequence().map(String::trim).firstOrNull { it.startsWith("Auth=") }
            ?.removePrefix("Auth=")?.takeIf { it.isNotBlank() && it.none(Char::isISOControl) }
            ?: throw FreshRssProblem("The server did not return a valid GReader login. Check the API URL and that API access is enabled.")
        return FreshRssSession(endpoint, username.trim(), token, allowHttp)
    }

    override fun repository(session: FreshRssSession): FreshRssRepository =
        GReaderRepository(api(session.endpoint), session)
}

internal class GReaderRepository(private val api: GReaderApi, private val session: FreshRssSession) : FreshRssRepository {
    private val auth get() = "GoogleLogin auth=${session.token}"
    private var subscriptions = emptyMap<String, Subscription>()
    private var writeToken: String? = null
    private val tokenMutex = Mutex()

    override suspend fun catalogue(): Catalogue = coroutineScope {
        val feedsCall = async { api.subscriptions(auth).data() }
        val tagsCall = async { api.tags(auth).data() }
        val countsCall = async { api.counts(auth).data() }
        val feeds = feedsCall.await().subscriptions.orEmpty().filter { !it.id.isNullOrBlank() }
        subscriptions = feeds.associateBy { it.id!! }
        val counts = countsCall.await().unreadcounts.orEmpty().mapNotNull { count ->
            count.id?.let { it to count.count?.coerceAtLeast(0) }
        }.toMap()
        val folders = (tagsCall.await().tags.orEmpty() + feeds.flatMap { it.categories.orEmpty() })
            .filter { it.id?.startsWith(LABEL_PREFIX) == true }.distinctBy { it.id }
            .map { Section(it.id!!, it.label?.takeIf(String::isNotBlank) ?: it.id.removePrefix(LABEL_PREFIX), unread = counts[it.id]) }
        Catalogue(folders + feeds.map { Section(it.id!!, it.title.orEmpty().ifBlank { "Untitled feed" },
            feed = true, unread = counts[it.id]) }, counts[READING_LIST])
    }

    override suspend fun page(stream: String, continuation: String?): ArticlePage {
        val response = api.contents(auth, FreshRssEndpoint.streamPath(stream), continuation).data()
        return ArticlePage(response.items.orEmpty().mapNotNull { ArticleMapper.map(it, subscriptions, session.endpoint) }
            .distinctBy { it.id }, response.continuation?.takeIf(String::isNotBlank))
    }

    override suspend fun setRead(id: String, read: Boolean) = edit(id, READ, read)
    override suspend fun setStarred(id: String, starred: Boolean) = edit(id, STARRED, starred)

    private suspend fun token(force: Boolean = false): String = tokenMutex.withLock {
        if (force) writeToken = null
        writeToken ?: api.token(auth).text().trim().also {
            if (it.isEmpty() || it.any(Char::isISOControl) || '<' in it) throw FreshRssProblem("The server did not return a valid write token.")
            writeToken = it
        }
    }

    private suspend fun edit(id: String, tag: String, enabled: Boolean) {
        suspend fun send(value: String) = api.edit(auth, value, id, tag.takeIf { enabled }, tag.takeUnless { enabled })
        var response = send(token())
        if (response.code() == 403) {
            response.errorBody()?.close()
            response = send(token(force = true))
        }
        if (response.text().trim() != "OK") throw FreshRssProblem("The server did not confirm the change. Refresh to check its state.")
    }
}

private fun Response<*>.check(login: Boolean = false) {
    if (isSuccessful) return
    errorBody()?.close()
    when (code()) {
        401, 403 -> throw FreshRssProblem(if (login) "Sign-in failed. Check your username, API password and API access setting."
            else "Your session has expired. Sign in again.", signInRequired = !login)
        in 300..399 -> throw FreshRssProblem("The server redirected the request. Enter its final instance URL.")
        404 -> throw FreshRssProblem("FreshRSS API was not found. Check the instance URL and enable API access.")
        else -> throw FreshRssProblem("FreshRSS returned HTTP ${code()}. Try again shortly.")
    }
}
private fun <T> Response<T>.data(): T { check(); return body() ?: throw FreshRssProblem("FreshRSS returned an empty response.") }
private fun Response<ResponseBody>.text(login: Boolean = false): String {
    check(login); return body()?.use { it.string() } ?: throw FreshRssProblem("FreshRSS returned an empty response.")
}
