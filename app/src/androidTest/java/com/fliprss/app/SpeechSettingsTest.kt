package com.fliprss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.fliprss.app.data.*
import org.junit.Rule
import org.junit.Test

class SpeechSettingsTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun speechDialogHasExplicitOnlineOptInAndPreviewControls() {
        compose.onNodeWithContentDescription("App settings").performClick()
        compose.onNodeWithText("Speech and voices").performScrollTo().performClick()
        compose.onNodeWithText("Allow online voices").assertExists()
        compose.onNodeWithText("Preview voice").performScrollTo().assertExists()
        compose.onNodeWithText("Reset speech defaults").performScrollTo().assertExists()
        compose.onNodeWithText("Done").performClick()
    }
    @Test fun androidPreferencesPersistSpeechChoices() {
        val store=AndroidSettingsStore(compose.activity);val original=store.load()
        try {
            store.save(original.copy(speech=SpeechOptions("example.engine","example.voice",true,110,95)))
            org.junit.Assert.assertEquals(SpeechOptions("example.engine","example.voice",true,110,95), AndroidSettingsStore(compose.activity).load().speech)
        } finally {store.save(original)}
    }
}
