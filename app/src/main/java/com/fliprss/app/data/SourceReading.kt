package com.fliprss.app.data

/** Sources remain independently loaded; a combined snapshot routes mutations by Article.local/demo. */
internal fun combinedSaved(local: LibraryState, fresh: LibraryState): LibraryState {
    val server = fresh.takeUnless { it.demo }
    val localArticles = local.articles.filter { it.id in local.savedIds }
    val serverArticles = server?.articles.orEmpty().filter { it.id in fresh.savedIds }
    return local.copy(local = false, demo = false, account = server?.account,
        scope = LibraryScope(savedOnly = true), articles = localArticles + serverArticles,
        savedIds = local.savedIds + server?.savedIds.orEmpty(), readIds = local.readIds + server?.readIds.orEmpty(),
        pending = local.pending + server?.pending.orEmpty(),
        loading = local.loading || server?.loading == true, loadingMore = server?.loadingMore == true,
        continuation = server?.continuation, error = local.error ?: server?.error,
        sections = emptyList(), markingAll = local.markingAll || server?.markingAll == true)
}

/** Only observed cards subsequently passed above the viewport qualify; never mark unseen jumps. */
internal class ScrollReadTracker {
    private val seen = mutableMapOf<String, Int>()
    private val marked = mutableSetOf<String>()
    private var previousFirst = -1
    private var previousOffset = 0
    fun observe(visible: Map<String, Int>, firstIndex: Int, offset: Int, scrolling: Boolean): Set<String> {
        val forward = firstIndex > previousFirst || (firstIndex == previousFirst && offset > previousOffset)
        val passed = if (scrolling && previousFirst >= 0 && forward) seen.filter { (key, index) ->
            key !in visible && index < firstIndex && key !in marked
        }.keys.toSet() else emptySet()
        seen.putAll(visible); marked.addAll(passed)
        previousFirst = firstIndex; previousOffset = offset
        return passed
    }
}
