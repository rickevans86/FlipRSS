package com.fliprss.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** A reader-only request: no wake lock, and the previous view policy is restored on exit. */
@Composable internal fun ReadingScreenAwake(playing: Boolean) {
    val view = LocalView.current
    val owner = LocalLifecycleOwner.current
    DisposableEffect(view, owner, playing) {
        val previous = view.keepScreenOn
        fun update() {
            view.keepScreenOn = previous || (playing && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        }
        val observer = LifecycleEventObserver { _, _ -> update() }
        owner.lifecycle.addObserver(observer)
        update()
        onDispose {
            owner.lifecycle.removeObserver(observer)
            view.keepScreenOn = previous
        }
    }
}
