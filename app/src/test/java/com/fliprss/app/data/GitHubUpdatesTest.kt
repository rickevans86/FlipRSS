package com.fliprss.app.data

import com.google.gson.Gson
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class GitHubUpdatesTest {
    private val apk = "verified APK fixture".toByteArray()
    private val hash = MessageDigest.getInstance("SHA-256").digest(apk).joinToString("") { "%02x".format(it) }
    private val url = "$RELEASES_PAGE/download/v0.7.0/FlipRSS.apk"
    private val gson = Gson()
    private fun release(prerelease: Boolean = false, apkUrl: String = url, digest: String = "sha256:$hash") = gson.toJson(mapOf(
        "tag_name" to "v0.7.0", "prerelease" to prerelease, "draft" to false,
        "assets" to listOf(mapOf("name" to "FlipRSS.apk", "size" to apk.size, "browser_download_url" to apkUrl, "digest" to digest),
            mapOf("name" to "build-info.json", "browser_download_url" to "$RELEASES_PAGE/download/v0.7.0/build-info.json"))))
    private fun metadata(version: String = "0.7.0", code: Int = 7) = gson.toJson(mapOf("version" to version,
        "version_code" to code, "sha256" to hash, "size_bytes" to apk.size))
    @Test fun offersNewerRegularReleaseAndIgnoresOlderOrEqualBuilds() {
        val update = UpdateManifest.parse(release(), metadata(), 6)!!
        assertEquals(7L, update.code); assertEquals(url, update.url); assertEquals(hash, update.sha256)
        assertNull(UpdateManifest.parse(release(), metadata(), 7))
        assertNull(UpdateManifest.parse(release(), metadata(), 8))
    }
    @Test fun refusesPrereleasesWrongMetadataForeignUrlsAndMismatchedDigests() {
        val invalid = listOf(release(true) to metadata(), release() to metadata("0.8.0"),
            release(apkUrl = "https://example.org/FlipRSS.apk") to metadata(), release(digest = "sha256:" + "0".repeat(64)) to metadata())
        invalid.forEach { (r, m) -> try { UpdateManifest.parse(r, m, 6); fail() } catch (_: UpdateProblem) { } }
    }
    @Test fun checksPublishedMetadataWithoutSendingAccountAuthorization() = runBlocking {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            assertNull(chain.request().header("Authorization"))
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body((if (chain.request().url.toString() == LATEST_RELEASE) release() else metadata()).toResponseBody()).build()
        }.build()
        assertEquals(7L, GitHubUpdates(client).check(6)!!.code)
    }
    @Test fun successfulDownloadIsCheckedAndFailedHashLeavesNoInstallableFile() = runBlocking {
        val directory = java.nio.file.Files.createTempDirectory("fliprss-updates-test").toFile()
        try {
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK").body(apk.toResponseBody()).build()
            }.build()
            val remote = GitHubUpdates(client); val update = UpdateManifest.parse(release(), metadata(), 6)!!
            val file = remote.download(update, directory)
            assertEquals("FlipRSS.apk", file.name); assertArrayEquals(apk, file.readBytes())
            try { remote.download(update.copy(sha256 = "0".repeat(64)), directory); fail() } catch (_: UpdateProblem) { }
            assertFalse(file.exists()); assertFalse(java.io.File(directory, "FlipRSS.apk.part").exists())
        } finally { directory.deleteRecursively() }
    }
    @Test fun refusesRedirectToHttpOrOutsideGitHub() = runBlocking {
        val directory = java.nio.file.Files.createTempDirectory("fliprss-redirect-test").toFile()
        try {
            for (destination in listOf("http://github.com/unsafe", "https://example.org/FlipRSS.apk")) {
                val client = OkHttpClient.Builder().addInterceptor { chain ->
                    Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(302).message("Redirect")
                        .header("Location", destination).body("".toResponseBody()).build()
                }.build()
                try { GitHubUpdates(client).download(UpdateManifest.parse(release(), metadata(), 6)!!, directory); fail() } catch (_: UpdateProblem) { }
                assertTrue(directory.listFiles().orEmpty().isEmpty())
            }
        } finally { directory.deleteRecursively() }
    }
}
