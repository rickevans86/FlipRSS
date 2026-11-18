package com.fliprss.app.data

import com.fliprss.app.FeedSearchModel
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class DirectoryCacheTest {
    @Test fun cachesListsRefreshesDailyAndKeepsFailedSources() = runBlocking {
        val jobs=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        val dir=java.nio.file.Files.createTempDirectory("fliprss-directory-test").toFile()
        val requests=AtomicInteger();var now=100_000_000L;var failed=false
        val client=OkHttpClient.Builder().addInterceptor { chain ->
            requests.incrementAndGet();assertNull(chain.request().header("Authorization"))
            val feeder=chain.request().url.host=="feeder.co"
            val body=if(feeder) "<table><tr><td>Imported feeder</td><td><code>https://example.org/feeder.xml</code></td></tr></table>"
                else "<li><h3>Imported spot</h3><p data-site='https%3A%2F%2Fexample.org%2Fspot.xml'></p></li>"
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(if(failed&&feeder)503 else 200).message("fixture").body(body.toResponseBody()).build()
        }.build()
        try {
            val file=java.io.File(dir,"lists.json")
            val one=FeedSearchModel(jobs,file,FeedWeb(client), now = {now})
            withTimeout(5000){one.state.first { !it.updating&&it.updated>0 }}
            assertTrue(one.state.value.feeds.any { it.url=="https://example.org/feeder.xml" })
            assertEquals(2,requests.get())
            val two=FeedSearchModel(jobs,file,FeedWeb(client), now = {100_000_000L})
            withTimeout(5000){two.state.first { it.updated>0 }}
            assertEquals(2,requests.get())
            now+=86_400_001;failed=true
            val three=FeedSearchModel(jobs,file,FeedWeb(client), now = {now})
            withTimeout(5000){three.state.first { !it.updating&&it.notice?.contains("Feeder")==true }}
            assertEquals(4,requests.get())
            assertTrue(three.state.value.feeds.any { it.url=="https://example.org/feeder.xml" })
            assertTrue(three.state.value.feeds.any { it.url=="https://example.org/spot.xml" })
        } finally {jobs.cancel();dir.deleteRecursively()}
    }
    @Test fun rejectsDowngradeCredentialsAndOversizeDiscoveryResponses() = runBlocking {
        for (mode in listOf("redirect","large")) {
            val client=OkHttpClient.Builder().addInterceptor { chain ->
                val b=Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).message("fixture")
                if(mode=="redirect") b.code(302).header("Location","http://example.org/feed").body("".toResponseBody()).build()
                else b.code(200).body("x".repeat(4_000_001).toResponseBody()).build()
            }.build()
            try { FeedWeb(client).get("https://example.org");fail() } catch (_: FreshRssProblem) { }
        }
        try {FeedWeb().get("https://user:secret@example.org");fail()}catch (_: FreshRssProblem) { }
    }
    @Test fun oldCachedDuplicateNamesAreDiscardedAndRefreshedImmediately() = runBlocking {
        val jobs=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        val dir=java.nio.file.Files.createTempDirectory("fliprss-bad-cache").toFile()
        val requests=AtomicInteger()
        try {
            val file=java.io.File(dir,"lists.json")
            file.writeText("""{"sources":{"Feedspot":[{"id":"bad","title":"NBC News","category":"Feedspot","description":"wrong old cache","url":"https://wordpress.com/blog/feed/"}]},"updated":100000000,"attempted":100000000}""")
            val client=OkHttpClient.Builder().addInterceptor { chain ->
                requests.incrementAndGet()
                val body=if(chain.request().url.host=="feeder.co") "<table><tr><td>Example</td><td><code>https://example.org/rss</code></td></tr></table>"
                    else "<h3 id='h123'>WordPress RSS Feed</h3><p class='trow' id='row123' data-site='https%3A%2F%2Fwordpress.com%2Fblog%2Ffeed%2F'></p>"
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("fixture").body(body.toResponseBody()).build()
            }.build()
            val model=FeedSearchModel(jobs,file,FeedWeb(client),now={100_000_000L})
            withTimeout(5000){model.state.first { !it.updating&&it.notice!=null }}
            assertEquals(2,requests.get())
            assertEquals("WordPress",model.state.value.feeds.single { it.url=="https://wordpress.com/blog/feed/" }.title)
            assertTrue(file.readText().contains("\"schemaVersion\":2"))
        } finally {jobs.cancel();dir.deleteRecursively()}
    }
}
