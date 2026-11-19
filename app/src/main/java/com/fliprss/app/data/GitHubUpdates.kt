package com.fliprss.app.data

import com.google.gson.JsonParser
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal const val RELEASES_PAGE = "https://github.com/rickevans86/FlipRSS/releases"
internal const val LATEST_RELEASE = "https://api.github.com/repos/rickevans86/FlipRSS/releases/latest"
internal const val MAX_UPDATE_BYTES = 512_000_000L
internal data class AppUpdate(val version: String, val code: Long, val url: String, val sha256: String, val size: Long)
internal class UpdateProblem(message: String) : Exception(message)

/** Stable releases only. An update is offered only after both APK and matching build metadata exist. */
internal object UpdateManifest {
    fun parse(releaseJson: String, buildJson: String, installedCode: Long): AppUpdate? {
        val release = JsonParser.parseString(releaseJson).asJsonObject
        if (release.get("draft")?.asBoolean == true || release.get("prerelease")?.asBoolean == true)
            throw UpdateProblem("No regular release is available yet.")
        val tag = release.get("tag_name")?.asString ?: throw UpdateProblem("Release version is missing.")
        val info = JsonParser.parseString(buildJson).asJsonObject
        val version = info.get("version")?.asString ?: throw UpdateProblem("Build version is missing.")
        val code = info.get("version_code")?.asLong ?: throw UpdateProblem("Build number is missing.")
        if (tag != "v$version") throw UpdateProblem("Release and build versions do not match.")
        if (code <= installedCode) return null
        val apk = release.getAsJsonArray("assets")?.firstOrNull { it.asJsonObject.get("name")?.asString == "FlipRSS.apk" }
            ?.asJsonObject ?: throw UpdateProblem("The release APK is still being published. Try again later.")
        val hash = info.get("sha256")?.asString.orEmpty()
        val size = info.get("size_bytes")?.asLong ?: 0
        if (!hash.matches(Regex("[a-f0-9]{64}")) || size !in 1..MAX_UPDATE_BYTES || apk.get("size")?.asLong != size)
            throw UpdateProblem("Release size or checksum metadata is invalid.")
        val url = apk.get("browser_download_url")?.asString.orEmpty()
        if (url != "$RELEASES_PAGE/download/$tag/FlipRSS.apk") throw UpdateProblem("Release download URL is invalid.")
        apk.get("digest")?.takeUnless { it.isJsonNull }?.asString?.let {
            if (it != "sha256:$hash") throw UpdateProblem("Release checksum does not match GitHub.")
        }
        return AppUpdate(version, code, url, hash, size)
    }
}

internal class GitHubUpdates(private val client: OkHttpClient = OkHttpClient.Builder()
    .followRedirects(false).followSslRedirects(false).connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS).callTimeout(600, TimeUnit.SECONDS).build()) {
    private fun open(url: String): Response {
        var address = url
        repeat(6) {
            val parsed = address.toHttpUrlOrNull() ?: throw UpdateProblem("Invalid update URL.")
            if (!parsed.isHttps || parsed.username.isNotEmpty() || parsed.password.isNotEmpty() ||
                parsed.host !in setOf("api.github.com", "github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com"))
                throw UpdateProblem("Update download left GitHub's secure download servers.")
            val response = client.newCall(Request.Builder().url(parsed).header("Accept", "application/vnd.github+json")
                .header("User-Agent", "FlipRSS-Android").build()).execute()
            if (response.code in 300..399) {
                val next = response.header("Location")?.let { parsed.resolve(it) }; response.close()
                address = next?.toString() ?: throw UpdateProblem("Update redirect was invalid.")
            } else {
                if (!response.isSuccessful) { val status = response.code; response.close()
                    throw UpdateProblem(if (status == 403 || status == 429) "GitHub is limiting update requests. Try again later."
                    else "GitHub update request failed (HTTP $status). Try again later.") }
                return response
            }
        }
        throw UpdateProblem("Too many update redirects.")
    }
    private fun text(url: String): String = open(url).use { response ->
        val body = response.body ?: throw UpdateProblem("Empty update response.")
        if (body.contentLength() > 1_000_000 || body.source().request(1_000_001)) throw UpdateProblem("Update metadata is too large.")
        body.string()
    }
    suspend fun check(installedCode: Long): AppUpdate? = withContext(Dispatchers.IO) {
        val json = text(LATEST_RELEASE)
        val release = JsonParser.parseString(json).asJsonObject
        val tag = release.get("tag_name")?.asString.orEmpty()
        if (!tag.matches(Regex("v[0-9]+\\.[0-9]+\\.[0-9]+"))) throw UpdateProblem("No regular release with update metadata is available yet.")
        val expected = "$RELEASES_PAGE/download/$tag/build-info.json"
        val metadata = release.getAsJsonArray("assets")?.firstOrNull { it.asJsonObject.get("name")?.asString == "build-info.json" }?.asJsonObject
            ?: throw UpdateProblem("Release files are still being published. Open GitHub Releases or try again later.")
        if (metadata.get("browser_download_url")?.asString != expected) throw UpdateProblem("Invalid build metadata URL.")
        UpdateManifest.parse(json, text(expected), installedCode)
    }
    suspend fun download(update: AppUpdate, directory: File): File = withContext(Dispatchers.IO) {
        directory.mkdirs()
        val partial = File(directory, "FlipRSS.apk.part")
        val target = File(directory, "FlipRSS.apk")
        target.delete()
        try {
            val hash = MessageDigest.getInstance("SHA-256")
            var count = 0L
            val coroutine = currentCoroutineContext()
            open(update.url).use { response ->
                val body = response.body ?: throw UpdateProblem("Empty APK response.")
                if (body.contentLength() > MAX_UPDATE_BYTES) throw UpdateProblem("Update APK is too large.")
                body.byteStream().use { input -> partial.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        coroutine.ensureActive()
                        val amount = input.read(buffer); if (amount < 0) break
                        count += amount
                        if (count > update.size || count > MAX_UPDATE_BYTES) throw UpdateProblem("APK size does not match this release.")
                        hash.update(buffer, 0, amount); output.write(buffer, 0, amount)
                    }
                    output.fd.sync()
                } }
            }
            val digest = hash.digest().joinToString("") { "%02x".format(it) }
            if (count != update.size || digest != update.sha256) throw UpdateProblem("APK verification failed. Download it again.")
            if (!partial.renameTo(target)) throw IOException("Could not keep update APK")
            target
        } finally { partial.delete() }
    }
}
