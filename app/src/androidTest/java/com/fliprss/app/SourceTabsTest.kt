package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.fliprss.app.data.AppSettings
import org.junit.Rule
import org.junit.Test

class SourceTabsTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun plusIsOnlyOnFreshRssAndTopicsIsRemoved() {
        compose.runOnIdle { ViewModelProvider(compose.activity)[LibraryViewModel::class.java].updateSettings(AppSettings()) }
        compose.onNodeWithContentDescription("Add feed").assertDoesNotExist()
        compose.onNodeWithText("Topics").assertDoesNotExist()
        compose.onNodeWithText("FreshRSS",useUnmergedTree=true).performClick()
        compose.onNodeWithContentDescription("Add feed").assertIsDisplayed()
        compose.onNodeWithText("Search",useUnmergedTree=true).performClick()
        compose.onNodeWithContentDescription("Add feed").assertDoesNotExist()
        compose.onNodeWithText("Website or feed URL").assertIsDisplayed()
        compose.onNodeWithText("Saved",useUnmergedTree=true).performClick()
        compose.onNodeWithContentDescription("Add feed").assertDoesNotExist()
        compose.onNodeWithContentDescription("Hide read stories").assertDoesNotExist()
    }
}
