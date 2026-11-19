package com.fliprss.app.browser

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.mozilla.geckoview.GeckoView

@Composable internal fun ReaderBrowserLifecycle(renderer: BrowserArticleRenderer) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, renderer) {
        fun update() { renderer.foreground(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
        val observer = LifecycleEventObserver { _, _ -> update() }
        owner.lifecycle.addObserver(observer); update()
        onDispose { owner.lifecycle.removeObserver(observer); renderer.foreground(false) }
    }
}
private class RenderView(context: Context) : GeckoView(context) {
    override fun dispatchTouchEvent(event: MotionEvent): Boolean = false
    override fun onGenericMotionEvent(event: MotionEvent): Boolean = false
}
/** Real viewport behind reader UI; TextureView alpha works on supported Android versions. No input/accessibility. */
@Composable internal fun ReaderBrowserSurface(renderer: BrowserArticleRenderer) {
    val session by renderer.session.collectAsStateWithLifecycle()
    session?.let { page -> key(page) {
        AndroidView(factory = { context -> RenderView(context).apply {
            setViewBackend(GeckoView.BACKEND_TEXTURE_VIEW)
            alpha = 0f; isFocusable = false; isFocusableInTouchMode = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            setSession(page)
        } }, modifier = Modifier.fillMaxSize().clearAndSetSemantics { }, onRelease = { it.releaseSession() })
    } }
}
