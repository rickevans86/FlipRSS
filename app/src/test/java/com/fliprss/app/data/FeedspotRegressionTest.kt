package com.fliprss.app.data

import org.junit.Assert.*
import org.junit.Test

class FeedspotRegressionTest {
    @Test fun siblingEntriesUnderSharedContainerKeepTheirOwnNamesAndUrls() {
        val html="""<main><h3 id="h1">1. NBC News RSS Feed</h3><p class="trow" id="row1" data="1" data-site="https%3A%2F%2Ffeeds.nbcnews.com%2Fnews"><a class="ext" href="https://feeds.nbcnews.com/news">RSS</a></p>
            <h3 id="h2">2. WordPress RSS Feed</h3><p class="trow" id="row2" data="2" data-site="https%3A%2F%2Fwordpress.com%2Fblog%2Ffeed%2F"><a class="ext" href="https://wordpress.com/blog/feed/">RSS</a></p>
            <h3 id="h3">3. RoboHub RSS Feed</h3><p class="trow" id="row3" data="3" data-site="https%3A%2F%2Frobohub.org%2Ffeed%2F"><a class="ext" href="https://robohub.org/feed/">RSS</a></p>
            <footer><a class="ext" href="https://unrelated.org/feed">Unrelated link without entry heading</a></footer></main>"""
        val entries=DirectoryImporter.parse("Feedspot",FeedPage(DirectoryImporter.sources.getValue("Feedspot"),html))
        assertEquals(listOf("NBC News","WordPress","RoboHub"),entries.map { it.title })
        assertEquals(3,entries.size)
        assertEquals("https://wordpress.com/blog/feed/",entries[1].url)
        assertEquals(3,entries.map { it.url }.distinct().size)
    }
    @Test fun unmatchedRowsDoNotBorrowAnEarlierPublisherHeading() {
        val html="""<div><h3 id='h1'>NBC News RSS Feed</h3><p class='trow' id='row1' data-site='https%3A%2F%2Fnbc.example%2Frss'></p>
            <h3 id='h222'>Another heading RSS Feed</h3><p class='trow' id='row777' data-site='https%3A%2F%2Funknown.example%2Frss'></p></div>"""
        val entries=DirectoryImporter.parse("Feedspot",FeedPage(DirectoryImporter.sources.getValue("Feedspot"),html))
        assertEquals(1,entries.size);assertEquals("NBC News",entries.single().title)
    }
    @Test fun fallbackRssLinkIsScopedToMatchingRowAndIgnoresHomepage() {
        val html="""<main><h3 id='h123'>WordPress RSS Feed</h3><p class='trow' id='row123'><a class='extdomain ext' href='https://wordpress.com'>Website</a><a class='ext' href='https://wordpress.com/blog/feed/'>RSS</a></p></main>"""
        val entry=DirectoryImporter.parse("Feedspot",FeedPage(DirectoryImporter.sources.getValue("Feedspot"),html)).single()
        assertEquals("WordPress",entry.title);assertEquals("https://wordpress.com/blog/feed/",entry.url)
    }
}
