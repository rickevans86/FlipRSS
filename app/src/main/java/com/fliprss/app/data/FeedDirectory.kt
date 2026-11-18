package com.fliprss.app.data

import java.util.Locale

internal data class PopularFeed(val id: String, val title: String, val category: String,
    val description: String, val url: String)

/** Curated, bundled directory. Searching never sends queries to a third-party service. */
internal object FeedDirectory {
    val feeds = listOf(
        PopularFeed("bbc", "BBC News", "News", "Headlines and reporting from around the world.", "https://feeds.bbci.co.uk/news/rss.xml"),
        PopularFeed("guardian", "The Guardian", "News", "World news, analysis and opinion.", "https://www.theguardian.com/world/rss"),
        PopularFeed("npr", "NPR News", "News", "US and international news from public radio.", "https://feeds.npr.org/1001/rss.xml"),
        PopularFeed("ars", "Ars Technica", "Technology", "Technology, science and digital culture.", "https://feeds.arstechnica.com/arstechnica/index"),
        PopularFeed("verge", "The Verge", "Technology", "Technology, gadgets and the future.", "https://www.theverge.com/rss/index.xml"),
        PopularFeed("wired", "WIRED", "Technology", "Ideas, science, technology and culture.", "https://www.wired.com/feed/rss"),
        PopularFeed("techcrunch", "TechCrunch", "Technology", "Startups, business and technology news.", "https://techcrunch.com/feed/"),
        PopularFeed("hackernews", "Hacker News", "Technology", "Popular stories from the programming community.", "https://news.ycombinator.com/rss"),
        PopularFeed("android", "Android Authority", "Technology", "Android phones, apps and mobile technology.", "https://www.androidauthority.com/feed/"),
        PopularFeed("nasa", "NASA", "Science", "Space exploration and science from NASA.", "https://www.nasa.gov/feed/"),
        PopularFeed("nature", "Nature", "Science", "Scientific research and discoveries.", "https://www.nature.com/nature.rss"),
        PopularFeed("sciencedaily", "ScienceDaily", "Science", "Research news across science and health.", "https://www.sciencedaily.com/rss/all.xml"),
        PopularFeed("smithsonian", "Smithsonian Magazine", "Culture", "History, science, arts and culture.", "https://www.smithsonianmag.com/rss/latest_articles/"),
        PopularFeed("xkcd", "xkcd", "Culture", "Comics about science, technology and life.", "https://xkcd.com/atom.xml"),
        PopularFeed("pitchfork", "Pitchfork", "Culture", "Music news and artists.", "https://pitchfork.com/feed/feed-news/rss"),
    )
    val categories = listOf("All topics") + feeds.map { it.category }.distinct()
    fun search(query: String, category: String = "All topics"): List<PopularFeed> {
        val words = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter(String::isNotEmpty)
        return feeds.filter { feed ->
            (category == "All topics" || feed.category == category) && words.all { word ->
                "${feed.title} ${feed.category} ${feed.description}".lowercase(Locale.ROOT).contains(word)
            }
        }
    }
}

internal fun libraryDestinations(showFeedSearch: Boolean) =
    if (showFeedSearch) listOf("FreshRSS", "FlipRSS", "Search", "Saved") else listOf("FreshRSS", "FlipRSS", "Saved")
internal fun availableDestination(current: String, showFeedSearch: Boolean) =
    current.takeIf { it in libraryDestinations(showFeedSearch) } ?: "FlipRSS"
