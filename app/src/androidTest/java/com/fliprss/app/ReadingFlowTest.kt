package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class ReadingFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun openStoryTurnPageAndReturnToLibrary() {
        val first = DemoLibrary.articles.first()
        compose.onNodeWithText(first.title)
            .performScrollTo().performClick()
        compose.onNodeWithText("1 / 6").assertIsDisplayed()
        compose.onNodeWithContentDescription("Next story").performClick()
        compose.onNodeWithText("2 / 6").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to stories").performClick()
        compose.onNodeWithText("Your daily perspective.").performScrollTo().assertIsDisplayed()
    }
    @Test fun bookmarkAppearsInSavedAndCanBeRemoved() {
        val title = DemoLibrary.articles.first().title
        val unsave = "Unsave $title"
        if (compose.onAllNodesWithContentDescription(unsave).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithContentDescription(unsave).performScrollTo().performClick()
        }
        compose.onNodeWithContentDescription("Save $title").performScrollTo().performClick()
        compose.onNodeWithText("Saved", useUnmergedTree = true).performClick()
        compose.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription(unsave).performScrollTo().performClick()
        compose.onNodeWithText(title).assertDoesNotExist()
    }

    @Test fun topicTileOpensOnlyThatSection() {
        compose.onNodeWithText("Topics", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Nature").performScrollTo().performClick()
        compose.onNodeWithText("2 stories").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(DemoLibrary.articles.first().title).performScrollTo().assertIsDisplayed()
    }
}
