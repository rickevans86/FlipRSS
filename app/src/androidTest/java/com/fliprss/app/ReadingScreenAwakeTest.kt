package com.fliprss.app

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ReadingScreenAwakeTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    @Test fun activePlaybackPauseBackgroundResumeAndExitReleaseRequest() {
        val playing = mutableStateOf(false)
        val reader = mutableStateOf(true)
        lateinit var view: View
        lateinit var owner: Owner
        compose.runOnUiThread { owner = Owner().apply { registry.currentState = Lifecycle.State.RESUMED } }
        compose.setContent {
            view = LocalView.current
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                if (reader.value) ReadingScreenAwake(playing.value)
            }
        }
        compose.runOnIdle { assertFalse(view.keepScreenOn); playing.value = true }
        compose.runOnIdle { assertTrue(view.keepScreenOn); playing.value = false }
        compose.runOnIdle { assertFalse(view.keepScreenOn); playing.value = true }
        compose.runOnIdle { assertTrue(view.keepScreenOn); owner.registry.currentState = Lifecycle.State.STARTED; assertFalse(view.keepScreenOn) }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED; assertTrue(view.keepScreenOn); reader.value = false }
        compose.runOnIdle { assertFalse(view.keepScreenOn) }
    }
    @Test fun preservesPreExistingScreenRequest() {
        val reader = mutableStateOf(false)
        val playing = mutableStateOf(true)
        lateinit var view: View
        compose.setContent { view = LocalView.current; if (reader.value) ReadingScreenAwake(playing.value) }
        compose.runOnIdle { view.keepScreenOn = true; reader.value = true }
        compose.runOnIdle { assertTrue(view.keepScreenOn); playing.value = false }
        compose.runOnIdle { assertTrue(view.keepScreenOn); reader.value = false }
        compose.runOnIdle { assertTrue(view.keepScreenOn); view.keepScreenOn = false }
    }
}
