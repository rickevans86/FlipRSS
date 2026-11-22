package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.fliprss.app.data.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PinnedToolbarFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun headerRemainsVisibleAfterScrollingToReadFooter() {
        compose.runOnIdle {
            val model = ViewModelProvider(compose.activity)[LibraryViewModel::class.java]
            model.localLibrary.useDemo(); model.updateSettings(AppSettings())
            model.categories.select("local", null)
        }
        compose.onNodeWithTag("story-list").performScrollToNode(hasTestTag("mark-all-read-footer"))
        compose.onNodeWithTag("reading-toolbar").assertIsDisplayed()
        compose.onNodeWithContentDescription("App settings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Mark all read").assertIsDisplayed()
    }
    @Test fun categoryAndExplicitAllStoriesPersistInAndroidStore() {
        val context = compose.activity
        val key = "test:category-persistence"
        val preferences = context.getSharedPreferences("reading-categories", 0)
        try {
            AndroidCategoryStore(context).save(key, CategoryPreference("news"))
            assertEquals(CategoryPreference("news"), AndroidCategoryStore(context).load(key))
            AndroidCategoryStore(context).save(key, CategoryPreference(null))
            assertEquals(CategoryPreference(null), AndroidCategoryStore(context).load(key))
            preferences.edit().remove(key).commit()
            assertNull(AndroidCategoryStore(context).load(key))
        } finally { preferences.edit().remove(key).commit() }
    }
}
