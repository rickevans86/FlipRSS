package com.fliprss.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ReadingToolbarTest {
    @get:Rule val compose = createComposeRule()
    @Test fun allFreshRssActionsFit320dpWithAccessibleToggleAndSelection() {
        compose.setContent {
            var hidden by remember { mutableStateOf(false) }
            var all by remember { mutableStateOf(false) }
            MaterialTheme { Box(Modifier.width(320.dp)) {
                ReadingToolbar(true, hidden, all, true, true, true, true, true,
                    { hidden = it }, { all = true }, {}, {}, {}, {})
            } }
        }
        for (label in listOf("Add feed", "Hide read stories", "All stories", "Mark all read", "Refresh feeds", "App settings")) {
            compose.onNodeWithContentDescription(label).assertIsDisplayed().assertWidthIsEqualTo(48.dp)
        }
        compose.onNodeWithContentDescription("Hide read stories").assertIsOff().performClick().assertIsOn()
        compose.onNodeWithContentDescription("All stories").assertIsNotSelected().performClick().assertIsSelected()
    }
    @Test fun savedAndSearchOmitSourceFiltersAndMarkAllRead() {
        compose.setContent { MaterialTheme {
            ReadingToolbar(false, true, true, false, false, false, false, true, {}, {}, {}, {}, {}, {})
        } }
        listOf("Add feed", "Hide read stories", "All stories", "Mark all read").forEach {
            compose.onNodeWithContentDescription(it).assertDoesNotExist()
        }
        compose.onNodeWithContentDescription("App settings").assertIsDisplayed()
        compose.onNodeWithContentDescription("Refresh feeds").assertIsDisplayed()
    }
}
