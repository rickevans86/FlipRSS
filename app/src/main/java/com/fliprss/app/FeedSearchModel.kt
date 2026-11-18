package com.fliprss.app

import com.fliprss.app.data.*
import com.google.gson.Gson
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class DirectoryCache(val sources: Map<String, List<PopularFeed>> = emptyMap(), val updated: Long = 0, val attempted: Long = 0)
internal data class FeedSearchState(val feeds: List<PopularFeed> = FeedDirectory.feeds, val updating: Boolean = false,
    val updated: Long = 0, val notice: String? = null, val finding: Boolean = false,
    val found: List<PopularFeed> = emptyList(), val discoveryMessage: String? = null)
internal class FeedSearchModel(private val scope: CoroutineScope, file: File,
    private val web: FeedWeb = FeedWeb(), private val now: () -> Long = System::currentTimeMillis, autoRefresh: Boolean = true) {
    private val store = FileLocalFeedStore(file)
    private val gson = Gson()
    private var cache = DirectoryCache()
    private var enabled = autoRefresh
    private var ready = false
    private val mutable = MutableStateFlow(FeedSearchState())
    val state = mutable.asStateFlow()
    init { scope.launch {
        cache = withContext(Dispatchers.IO) { runCatching { store.read()?.let { gson.fromJson(it, DirectoryCache::class.java) } }.getOrNull() ?: DirectoryCache() }
        runCatching { publish() }.onFailure { cache = DirectoryCache(); publish() }
        ready = true; refreshIfDue()
    } }
    fun setEnabled(value: Boolean) { enabled = value; if (value && ready) refreshIfDue() }
    private fun refreshIfDue() {
        val time = now()
        if (enabled && (time < cache.attempted || time - cache.attempted >= 86_400_000)) refresh()
    }
    private fun publish() {
        mutable.value = mutable.value.copy(feeds = (FeedDirectory.feeds + cache.sources.values.flatten()).distinctBy { normalizedFeedUrl(it.url) }, updated = cache.updated)
    }
    fun refresh() {
        if (mutable.value.updating) return
        mutable.value = mutable.value.copy(updating = true, notice = null)
        scope.launch {
            val sources = cache.sources.toMutableMap(); val failures = mutableListOf<String>(); var successes = 0
            DirectoryImporter.sources.forEach { (name, url) ->
                try {
                    val feeds = withContext(Dispatchers.Default) { DirectoryImporter.parse(name, web.get(url)) }
                    if (feeds.isEmpty()) throw FreshRssProblem("No public RSS links found")
                    sources[name] = feeds; successes++
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { failures += name }
            }
            cache = DirectoryCache(sources, if (successes > 0) now() else cache.updated, now())
            val kept = withContext(Dispatchers.IO) { runCatching { store.write(gson.toJson(cache)) }.isSuccess }
            publish()
            mutable.value = mutable.value.copy(updating = false, notice = when {
                !kept -> "Lists loaded, but the device could not save the cache."
                failures.isNotEmpty() -> "Could not update ${failures.joinToString()}. Cached feeds remain available."
                else -> "Feed lists updated from Feeder and Feedspot."
            })
        }
    }
    fun find(url: String) {
        if (mutable.value.finding || url.isBlank()) return
        mutable.value = mutable.value.copy(finding = true, found = emptyList(), discoveryMessage = "Looking for RSS and Atom feeds…")
        scope.launch {
            try {
                val found = FeedDiscovery.find(url.trim(), web)
                mutable.value = mutable.value.copy(finding = false, found = found,
                    discoveryMessage = if (found.isEmpty()) "No RSS or Atom feeds found. This website may not publish a feed; try its direct feed URL." else "${found.size} feed(s) found. Add one to FlipRSS.")
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutable.value = mutable.value.copy(finding = false, discoveryMessage = "Could not inspect this website. Check its address/connection or try a direct feed URL.") }
        }
    }
}
