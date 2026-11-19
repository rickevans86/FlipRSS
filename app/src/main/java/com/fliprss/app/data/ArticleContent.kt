package com.fliprss.app.data

import com.fliprss.app.Article
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import kotlin.math.ceil

/** Extract only public, visible article blocks; never execute scripts or reuse account credentials. */
internal object ArticleContentExtractor {
    fun extract(page: FeedPage, article: Article): Article? {
        val doc = Jsoup.parse(page.text, page.url)
        // Publishers sometimes place restricted text in hidden HTML; do not reveal it.
        if (Regex("[\"']isAccessibleForFree[\"']\\s*:\\s*(false|[\"']false[\"'])", RegexOption.IGNORE_CASE)
                .containsMatchIn(page.text)) return null
        doc.select("script,style,noscript,iframe,object,embed,form,nav,aside,footer,header,button,svg,figure,figcaption,[hidden],[aria-hidden=true], [inert]").remove()
        doc.allElements.toList().filter { element ->
            val labels = "${element.id()} ${element.className()} ${element.attr("role")}"
            Regex("(?i)(^|[ _-])(advert(isement|ising)?|ads?|social|share|sharing|related|recommended|newsletter|subscribe|subscription|paywall|comments?|cookie|navigation|menu)([ _-]|$)").containsMatchIn(labels) ||
                Regex("(?i)(display\\s*:\\s*none|visibility\\s*:\\s*hidden)").containsMatchIn(element.attr("style"))
        }.forEach(Element::remove)
        val preferred = doc.select("[itemprop=articleBody], .entry-content, .article-content, .article-body, .story-body, .wp-block-post-content")
        val candidates = preferred.ifEmpty { doc.select("article") }.ifEmpty { doc.select("main, [role=main]") }
        val blocks = candidates.map { root ->
            root.select("p,h2,h3,h4,li,blockquote,pre").filter { block ->
                block.parents().takeWhile { it != root }.none { it.normalName() in setOf("p","li","blockquote","pre") } &&
                    (block.text().length >= 25 || block.normalName().startsWith("h")) &&
                    block.select("a").sumOf { it.text().length } < block.text().length * .6
            }.map { it.text().trim() }.filter { it.isNotBlank() && it != article.title }.take(500)
        }.maxByOrNull { paragraphs -> paragraphs.sumOf(String::length) } ?: return null
        val size = blocks.sumOf(String::length)
        val feedSize = article.paragraphs.sumOf(String::length)
        if (blocks.size < 2 || size < 300 || size > 200_000 || size < feedSize + maxOf(150, feedSize / 10)) return null
        return article.copy(paragraphs = blocks, minutes = ceil(blocks.joinToString(" ").split(Regex("\\s+")).size / 200.0).toInt().coerceAtLeast(1))
    }
}

internal fun Article.contentKey() = "${if (local) "local" else if (demo) "demo" else "fresh"}:$id:$originalUrl:${paragraphs.hashCode()}"
internal data class ArticleContentState(val key: String? = null, val expanded: Article? = null,
    val loading: Boolean = false, val attempted: Boolean = false, val browserLoading: Boolean = false, val viaBrowser: Boolean = false) {
    fun display(article: Article) = expanded?.takeIf { key == article.contentKey() } ?: article
}
/** Only the open story is fetched. Responses from canceled/replaced selections cannot enter the reader. */
internal class ArticleContentController(private val scope: CoroutineScope,
    private val fetch: suspend (String) -> FeedPage = { FeedWeb().get(it) },
    private val parseDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val render: suspend (Article) -> Article? = { null },
    private val autoBrowser: () -> Boolean = { false },
    private val expandWithBrowser: () -> Boolean = { true }) {
    private val mutable = MutableStateFlow(ArticleContentState())
    val state = mutable.asStateFlow()
    private var current: Article? = null
    private var generation = 0
    private var job: Job? = null
    private val cache = object : LinkedHashMap<String, Article>(20, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Article>?) = size > 20
    }
    fun open(article: Article, autoLoad: Boolean) {
        close()
        current = article
        val cached = cache[article.contentKey()]
        mutable.value = ArticleContentState(article.contentKey(), cached, attempted = cached != null)
        if (autoLoad && cached == null) load()
    }
    fun load(browserOnly: Boolean = false) {
        val article = current ?: return
        val address = article.originalUrl?.takeIf { normalizedFeedUrl(it) != null } ?: return
        if (article.demo || mutable.value.loading) return
        val request = ++generation
        val previous = mutable.value.expanded
        val previousViaBrowser = mutable.value.viaBrowser
        mutable.value = mutable.value.copy(loading = true, browserLoading = browserOnly)
        job = scope.launch {
            var best = previous
            var viaBrowser = previousViaBrowser
            try {
                val direct = if (browserOnly) null else try {
                    withContext(parseDispatcher) { ArticleContentExtractor.extract(fetch(address), article) }
                } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { null }
                if (direct != null && direct.paragraphs.sumOf(String::length) > (best?.paragraphs?.sumOf(String::length) ?: 0)) {
                    best = direct; viaBrowser = false
                }
                if (browserOnly || (autoBrowser() && (best == null || expandWithBrowser()))) {
                    if (request == generation) mutable.value = mutable.value.copy(expanded = best, browserLoading = true)
                    val baseline = best ?: article
                    val candidate = render(baseline)
                    if (candidate != null && candidate.id == article.id && candidate.originalUrl == article.originalUrl &&
                        candidate.local == article.local && candidate.demo == article.demo) {
                        RenderedArticleText.enrich(baseline, candidate.paragraphs)?.let { best = it; viaBrowser = true }
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { /* Keep best available text. */ }
            if (request == generation) {
                if (best != null) cache[article.contentKey()] = best!!
                mutable.value = ArticleContentState(article.contentKey(), best, attempted = true, viaBrowser = viaBrowser)
            }
        }
    }
    fun importBrowser(original: Article, candidate: Article): Boolean {
        val selected = current ?: return false
        if (selected.contentKey() != original.contentKey() || candidate.id != original.id || candidate.originalUrl != original.originalUrl ||
            candidate.local != original.local || candidate.demo != original.demo) return false
        val longer = RenderedArticleText.enrich(mutable.value.display(original), candidate.paragraphs) ?: return false
        ++generation; job?.cancel(); job = null
        cache[original.contentKey()] = longer
        mutable.value = ArticleContentState(original.contentKey(), longer, attempted = true, viaBrowser = true)
        return true
    }
    fun close() { ++generation; job?.cancel(); job = null; current = null; mutable.value = ArticleContentState() }
}
