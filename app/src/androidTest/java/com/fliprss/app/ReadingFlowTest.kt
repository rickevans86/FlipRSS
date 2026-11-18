package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import androidx.lifecycle.ViewModelProvider
import com.fliprss.app.data.AppSettings

class ReadingFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun resetSettings() { compose.runOnIdle {
        compose.activity.getSharedPreferences("demo-reading-state", 0).edit().clear().commit()
        val library = ViewModelProvider(compose.activity)[LibraryViewModel::class.java].library
        library.useDemo()
        library.updateSettings(AppSettings())
    } }

    @Test fun openStoryTurnPageAndReturnToLibrary() {
        val first = DemoLibrary.articles.first()
        compose.onNodeWithText(first.title)
            .performScrollTo().performClick()
        compose.onNodeWithText("1 / 6").assertIsDisplayed()
        compose.onNodeWithContentDescription("Next story").performClick()
        compose.onNodeWithText("2 / 6").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back to stories").performClick()
        compose.onNodeWithText("FlipRSS").performScrollTo().assertIsDisplayed()
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
    @Test fun accountFormCanBeOpenedAndDismissedWithoutReplacingDemo() {
        compose.onNodeWithContentDescription("App settings").performScrollTo().performClick()
        compose.onNodeWithText("Connect FreshRSS (optional)").performScrollTo().performClick()
        compose.onNodeWithText("Sign in").assertIsNotEnabled()
        compose.onNodeWithText("FreshRSS URL").performTextReplacement("https://rss.example.org")
        compose.onNodeWithText("Username").performTextInput("demo-user")
        compose.onNodeWithText("API password").performTextInput("not-a-real-password")
        compose.onNodeWithText("Sign in").assertIsEnabled()
        compose.onNodeWithText("Continue demo").performClick()
        compose.onNodeWithText("FlipRSS").performScrollTo().assertIsDisplayed()
    }

    @Test fun settingsThemeCanBeChangedAndDefaultsRestored() {
        compose.onNodeWithContentDescription("App settings").performScrollTo().performClick()
        compose.onNodeWithText("Dark").performClick()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithContentDescription("App settings").performClick()
        compose.onNodeWithText("Dark").assertIsSelected()
        compose.onNodeWithText("Reset defaults").performScrollTo().performClick()
        compose.onNodeWithText("Done").performClick()
    }

    @Test fun hideReadAndConfirmedMarkAllEmptyDemoWithoutMovingReaderQueue() {
        compose.onNodeWithText("Hide read").performScrollTo().performClick()
        compose.onNodeWithText("Mark all read").performClick()
        compose.onNodeWithText("Mark all read?").assertIsDisplayed()
        compose.onNodeWithText("Mark read").performClick()
        compose.onNodeWithText("No unread stories here.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Hide read").performScrollTo().performClick()
        compose.onNodeWithText(DemoLibrary.articles.first().title).performScrollTo().assertIsDisplayed()
    }

    @Test fun manualReadingModeDoesNotAutomaticallyMarkStoryRead() {
        compose.runOnIdle {
            val library = ViewModelProvider(compose.activity)[LibraryViewModel::class.java].library
            library.updateSettings(AppSettings(autoMarkRead = false))
            library.setRead(DemoLibrary.articles.first().id, false)
        }
        compose.onNodeWithText(DemoLibrary.articles.first().title).performScrollTo().performClick()
        compose.onNodeWithText("Mark read").assertIsDisplayed().performClick()
        compose.onNodeWithText("Mark unread").assertIsDisplayed()
    }

    @Test fun savedAlwaysIncludesReadStoriesAndHasNoHideReadToggle() {
        val story = DemoLibrary.articles.first()
        compose.runOnIdle {
            val library = ViewModelProvider(compose.activity)[LibraryViewModel::class.java].library
            library.toggleSaved(story.id)
            library.setRead(story.id, true)
            library.updateSettings(AppSettings(hideRead = true))
        }
        compose.onNodeWithText("Saved", useUnmergedTree = true).performClick()
        compose.onNodeWithText(story.title).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Hide read").assertDoesNotExist()
        compose.onNodeWithText("Your daily perspective.").assertDoesNotExist()
        compose.onNodeWithText("Account").assertDoesNotExist()
    }
}
