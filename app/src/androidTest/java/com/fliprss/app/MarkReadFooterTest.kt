package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.fliprss.app.data.AppSettings
import org.junit.Rule
import org.junit.Test

class MarkReadFooterTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun setup(confirm: Boolean) { compose.runOnIdle {
        compose.activity.getSharedPreferences("demo-reading-state", 0).edit().clear().commit()
        val model = ViewModelProvider(compose.activity)[LibraryViewModel::class.java]
        model.localLibrary.useDemo()
        model.updateSettings(AppSettings(hideRead = true, confirmMarkAllRead = confirm))
    } }
    @Test fun footerShowsConfirmationByDefault() {
        setup(true)
        compose.onNodeWithTag("story-list").performScrollToNode(hasTestTag("mark-all-read-footer"))
        compose.onNodeWithTag("mark-all-read-footer").performClick()
        compose.onNodeWithText("Mark all read?").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
    }
    @Test fun footerAppliesImmediatelyWhenConfirmationOff() {
        setup(false)
        compose.onNodeWithTag("story-list").performScrollToNode(hasTestTag("mark-all-read-footer"))
        compose.onNodeWithTag("mark-all-read-footer").performClick()
        compose.onNodeWithText("Mark all read?").assertDoesNotExist()
        compose.onNodeWithTag("story-list").performScrollToNode(hasText("No unread stories here."))
        compose.onNodeWithText("No unread stories here.").assertIsDisplayed()
    }
}
