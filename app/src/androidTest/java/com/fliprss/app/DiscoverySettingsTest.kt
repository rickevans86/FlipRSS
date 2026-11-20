package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.fliprss.app.data.*
import org.junit.Rule
import org.junit.Test

class DiscoverySettingsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun searchFiltersPopularFeedsAndPrefillsLocalSubscription() {
        compose.runOnIdle {
            val library = ViewModelProvider(compose.activity)[LibraryViewModel::class.java].library
            library.useLocalFeeds(); library.updateSettings(AppSettings())
        }
        compose.onNodeWithText("Search", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Search popular feeds").performTextInput("NASA")
        compose.onAllNodesWithText("Add NASA").onFirst().performScrollTo().performClick()
        compose.onNodeWithText("https://www.nasa.gov/feed/").assertExists()
        compose.onNodeWithText("Add to app").assertIsEnabled()
        compose.onNodeWithText("Cancel").performClick()
    }
    @Test fun oledAndSearchVisibilityPersistInAndroidSettings() {
        val context = compose.activity
        val original = AndroidSettingsStore(context).load()
        try {
            val settings = original.copy(theme = AppTheme.OLED, showFeedSearch = false, showFreshRss = false, showFlipRss = false)
            compose.runOnIdle {
                ViewModelProvider(context)[LibraryViewModel::class.java].library.updateSettings(settings)
            }
            compose.onNodeWithText("Search", useUnmergedTree = true).assertDoesNotExist()
            val restored = AndroidSettingsStore(context).load()
            org.junit.Assert.assertEquals(AppTheme.OLED, restored.theme)
            org.junit.Assert.assertFalse(restored.showFeedSearch)
            org.junit.Assert.assertFalse(restored.showFreshRss)
            org.junit.Assert.assertFalse(restored.showFlipRss)
            compose.onNodeWithText("Saved", useUnmergedTree = true).assertIsSelected()
        } finally {
            compose.runOnIdle { ViewModelProvider(context)[LibraryViewModel::class.java].library.updateSettings(original) }
        }
    }
    @Test fun turningSearchOffWhileOnSearchReturnsToToday() {
        compose.runOnIdle { ViewModelProvider(compose.activity)[LibraryViewModel::class.java].library.updateSettings(AppSettings()) }
        compose.onNodeWithText("Search", useUnmergedTree = true).performClick()
        compose.runOnIdle {
            val library = ViewModelProvider(compose.activity)[LibraryViewModel::class.java].library
            library.updateSettings(library.state.value.settings.copy(showFeedSearch = false))
        }
        compose.onNodeWithText("Search", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("FlipRSS", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("Find your next feed").assertDoesNotExist()
    }
}
