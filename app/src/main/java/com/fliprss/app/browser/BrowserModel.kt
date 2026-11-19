package com.fliprss.app.browser

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.lang.ref.WeakReference
import kotlinx.coroutines.launch
import org.mozilla.geckoview.*

internal class BrowserTab(val session: GeckoSession) {
    var url by mutableStateOf("about:blank")
    var title by mutableStateOf("New tab")
    var back by mutableStateOf(false)
    var forward by mutableStateOf(false)
    var loading by mutableStateOf(false)
}
internal class BrowserModel(application: Application) : AndroidViewModel(application) {
    val engine = BrowserEngine.get(application)
    val tabs = mutableStateListOf<BrowserTab>()
    var current by mutableStateOf<BrowserTab?>(null); private set
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var popup by mutableStateOf<GeckoSession?>(null); private set
    private var prompts: BrowserPrompts? = null
    fun setPrompts(value: BrowserPrompts?) {
        prompts = value; tabs.forEach { it.session.promptDelegate = value }; popup?.promptDelegate = value
    }
    var resumed = false
    var permission by mutableStateOf<Pair<List<String>, GeckoResult<AllowOrDeny>>?>(null); private set
    private var initialUrl: String? = null
    private var initialized = false
    init { engine.host = WeakReference(this) }
    fun start(url: String?) {
        if (initialized) return
        initialized = true; initialUrl = url
        newTab()
        configure(engine.requestedEnabled)
    }
    fun configure(enabled: Boolean) { viewModelScope.launch {
        if (engine.configure(enabled)) {
            ready = true; error = null
            tabs.forEach { engine.attach(it.session) }
            val first = initialUrl; initialUrl = null
            if (first != null) navigate(first)
            else if (current?.url != "about:blank") current?.session?.reload()
        }
    } }
    fun navigate(value: String) {
        val url = BrowserUrls.web(value)
        if (url == null) { error = "Enter an HTTP or HTTPS address without embedded credentials."; return }
        if (!ready) { error = "Finish browser setup or retry the add-on setting first."; return }
        val session = current?.session ?: newTab()
        error = null; session.loadUri(url)
    }
    fun openOptions() {
        val url = engine.state.value.optionsUrl ?: return
        if (!ready || !engine.state.value.enabled || !BrowserUrls.allowed(url, url)) return
        newTab().loadUri(url)
    }
    fun newTab(active: Boolean = true): GeckoSession {
        val session = GeckoSession(); val tab = BrowserTab(session)
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, url: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, hasUserGesture: Boolean) {
                if (url != null) tab.url = url
            }
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) { tab.back = canGoBack }
            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) { tab.forward = canGoForward }
            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> {
                val allowed = BrowserUrls.allowed(request.uri, engine.state.value.optionsUrl)
                if (!allowed) error = "This browser opens HTTP and HTTPS pages only."
                return if (allowed && (ready || request.uri == "about:blank" || request.uri.startsWith("moz-extension://"))) GeckoResult.allow() else GeckoResult.deny()
            }
            override fun onNewSession(session: GeckoSession, uri: String): GeckoResult<GeckoSession>? =
                if (ready && BrowserUrls.allowed(uri, engine.state.value.optionsUrl)) GeckoResult.fromValue(newTab()) else null
            override fun onLoadError(session: GeckoSession, uri: String?, error: WebRequestError): GeckoResult<String>? {
                this@BrowserModel.error = "The page could not be loaded. Check the address or connection and retry."
                tab.loading = false; return null
            }
        }
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) { tab.loading = true; error = null }
            override fun onPageStop(session: GeckoSession, success: Boolean) { tab.loading = false; if (!success) error = "The page did not finish loading. Try reloading." }
        }
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) { tab.title = title ?: "Web page" }
            override fun onCloseRequest(session: GeckoSession) { closeTab(session) }
        }
        session.promptDelegate = prompts
        session.open(engine.runtime); tabs.add(tab); engine.attach(session); if (active || current == null) select(session)
        return session
    }
    fun newPopup(): GeckoSession {
        dismissPopup()
        val session = GeckoSession()
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> =
                if (BrowserUrls.allowed(request.uri, engine.state.value.optionsUrl)) GeckoResult.allow() else GeckoResult.deny()
        }
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onCloseRequest(session: GeckoSession) { dismissPopup() }
        }
        session.promptDelegate = prompts
        session.open(engine.runtime); session.setActive(resumed); popup = session
        return session
    }
    fun dismissPopup() { popup?.let { it.setActive(false); it.close() }; popup = null }
    fun select(session: GeckoSession) {
        current?.session?.let { it.setActive(false); engine.runtime.webExtensionController.setTabActive(it, false) }
        current = tabs.firstOrNull { it.session == session }
        active(resumed)
    }
    fun active(value: Boolean) {
        resumed = value
        if (!value) dismissPopup()
        if (value) engine.host = WeakReference(this)
        current?.session?.let { it.setActive(value); engine.runtime.webExtensionController.setTabActive(it, value) }
    }
    fun closeTab(session: GeckoSession): Boolean {
        val tab = tabs.firstOrNull { it.session == session } ?: return false
        session.setActive(false); engine.runtime.webExtensionController.setTabActive(session, false)
        tabs.remove(tab); engine.detach(session); session.close()
        if (current == tab) { current = null; tabs.lastOrNull()?.let { select(it.session) } }
        return true
    }
    fun permissionPrompt(details: List<String>): GeckoResult<AllowOrDeny> {
        permission?.second?.complete(AllowOrDeny.DENY)
        return GeckoResult<AllowOrDeny>().also { permission = details to it }
    }
    fun answerPermission(allow: Boolean) { permission?.second?.complete(if (allow) AllowOrDeny.ALLOW else AllowOrDeny.DENY); permission = null }
    override fun onCleared() {
        answerPermission(false); prompts?.close(); setPrompts(null); dismissPopup()
        if (engine.host.get() == this) engine.host.clear()
        tabs.toList().forEach { closeTab(it.session) }
    }
}
