package com.fliprss.app

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.fliprss.app.data.PopularFeed
import org.junit.Rule
import org.junit.Test

class DirectoryPagingTest {
    @get:Rule val compose=createComposeRule()
    @Test fun catalogueRequiresExplicitLoadMoreAfterTwentyResults() {
        val feeds=(1..45).map { PopularFeed("$it","Publisher $it","News","Description","https://example.org/$it/rss") }
        compose.setContent {
            MaterialTheme {
                var count by remember { mutableIntStateOf(20) }
                LazyColumn(Modifier.testTag("results")) {
                    feedSearchPage(state=FeedSearchState(feeds=feeds),query="",category="All topics",onQuery={},onCategory={},
                        website="",onWebsite={},onFind={},onRefresh={},canAdd=true,onAdd={},pageSize=count,onLoadMore={count+=20})
                }
            }
        }
        compose.onNodeWithTag("results").performScrollToNode(hasText("Showing 20 of 45 feeds · Adds to FlipRSS"))
        compose.onNodeWithText("Showing 20 of 45 feeds · Adds to FlipRSS").assertExists()
        compose.onNodeWithTag("results").performScrollToNode(hasText("Load more feeds"))
        compose.onNodeWithText("Load more feeds").performClick()
        compose.onNodeWithTag("results").performScrollToNode(hasText("Showing 40 of 45 feeds · Adds to FlipRSS"))
        compose.onNodeWithText("Showing 40 of 45 feeds · Adds to FlipRSS").assertExists()
    }
}
