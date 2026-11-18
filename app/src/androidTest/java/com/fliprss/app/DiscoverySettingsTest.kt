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
            library.useDemo(); library.updateSettings(AppSettings())
        }
        compose.onNodeWithText("Search", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Search popular feeds").performTextInput("NASA")
        compose.onNodeWithText("Add NASA").performScrollTo().performClick()
        compose.onNodeWithText("Use local feeds without FreshRSS").performScrollTo().performClick()
        compose.onNodeWithText("https://www.nasa.gov/feed/").assertExists()
        compose.onNodeWithText("Add to app").assertIsEnabled()
        compose.onNodeWithText("Cancel").performClick()
    }
    @Test fun oledAndSearchVisibilityPersistInAndroidSettings() {
        val context = compose.activity
        val original = AndroidSettingsStore(context).load()
        try {
            val settings = original.copy(theme = AppTheme.OLED, showFeedSearch = false)
            compose.runOnIdle {
                ViewModelProvider(context)[LibraryViewModel::class.java].library.updateSettings(settings)
            }
            compose.onNodeWithText("Search", useUnmergedTree = true).assertDoesNotExist()
            val restored = AndroidSettingsStore(context).load()
            org.junit.Assert.assertEquals(AppTheme.OLED, restored.theme)
            org.junit.Assert.assertFalse(restored.showFeedSearch)
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
        compose.onNodeWithText("Today").assertIsSelected()
        compose.onNodeWithText("Find your next feed").assertDoesNotExist()
    }
}
