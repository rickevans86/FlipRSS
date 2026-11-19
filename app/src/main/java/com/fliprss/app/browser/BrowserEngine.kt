package com.fliprss.app.browser

import android.content.Context
import androidx.core.content.edit
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.*

internal data class AddonState(val enabled: Boolean = false, val busy: Boolean = false,
    val error: String? = null, val optionsUrl: String? = null)
internal suspend fun <T : Any> GeckoResult<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    accept({ value -> if (continuation.isActive) {
        if (value != null) continuation.resume(value)
        else continuation.resumeWithException(IllegalStateException("Browser returned no result"))
    } }, { error -> if (continuation.isActive) continuation.resumeWithException(error ?: IllegalStateException("Browser operation failed")) })
}
/** One engine/profile per process; no FreshRSS token/cookie sharing. Extension is app-pinned. */
internal class BrowserEngine private constructor(context: Context) {
    val runtime = GeckoRuntime.create(context.applicationContext, GeckoRuntimeSettings.Builder()
        .javaScriptEnabled(true).remoteDebuggingEnabled(false).consoleOutput(false).build())
    val reader = ReaderBridge(runtime)
    private val preferences = context.applicationContext.getSharedPreferences("browser-addon", 0)
    private val mutex = Mutex()
    private val mutable = MutableStateFlow(AddonState())
    val state = mutable.asStateFlow()
    private var extension: WebExtension? = null
    private var defaultAction: WebExtension.Action? = null
    private val actions = mutableMapOf<GeckoSession, WebExtension.Action>()
    private val actionDelegate = object : WebExtension.ActionDelegate {
        override fun onBrowserAction(extension: WebExtension, session: GeckoSession?, action: WebExtension.Action) {
            if (session == null) defaultAction = action else actions[session] = action
        }
        override fun onTogglePopup(extension: WebExtension, action: WebExtension.Action): GeckoResult<GeckoSession>? {
            val model = host.get() ?: return null
            if (model.popup != null) { model.dismissPopup(); return null }
            return GeckoResult.fromValue(model.newPopup())
        }
        override fun onOpenPopup(extension: WebExtension, action: WebExtension.Action): GeckoResult<GeckoSession>? =
            host.get()?.let { GeckoResult.fromValue(it.newPopup()) }
    }
    fun clickAction(session: GeckoSession?): Boolean {
        val defaults = defaultAction
        val action = session?.let { actions[it] }?.let { if (defaults != null) it.withDefault(defaults) else it } ?: defaults
        if (!state.value.enabled || action == null) return false
        action.click(); return true
    }
    fun detach(session: GeckoSession) { reader.detach(session); actions.remove(session) }
    var host = WeakReference<BrowserModel>(null)
    val requestedEnabled get() = preferences.getBoolean("enabled", false)
    init {
        runtime.webExtensionController.setPromptDelegate(object : WebExtensionController.PromptDelegate {
            override fun onOptionalPrompt(extension: WebExtension, permissions: Array<String>, origins: Array<String>,
                dataCollectionPermissions: Array<String>): GeckoResult<AllowOrDeny>? =
                if (extension.id == ID) host.get()?.permissionPrompt((permissions + origins + dataCollectionPermissions).toList()) else GeckoResult.deny()
        })
    }
    suspend fun configure(enabled: Boolean): Boolean = mutex.withLock { kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
        mutable.value = mutable.value.copy(busy = true, error = null)
        try {
            try { reader.prepare() } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled } catch (_: Exception) { /* Browser still works; capture can retry. */ }
            var installed = runtime.webExtensionController.list().awaitResult().firstOrNull { it.id == ID }
            if (enabled) installed = runtime.webExtensionController.ensureBuiltIn("resource://android/assets/bpc/", ID).awaitResult()
            if (installed != null) {
                installed = if (enabled) runtime.webExtensionController.enable(installed, WebExtensionController.EnableSource.USER).awaitResult()
                    else runtime.webExtensionController.disable(installed, WebExtensionController.EnableSource.USER).awaitResult()
                bind(installed)
            }
            extension = installed
            val actual = installed?.metaData?.enabled == true
            check(actual == enabled)
            preferences.edit { putBoolean("enabled", actual) }
            mutable.value = AddonState(enabled = actual, optionsUrl = installed?.metaData?.optionsPageUrl)
            true
        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (_: Exception) {
            mutable.value = mutable.value.copy(busy = false, error = "Could not apply the add-on setting. Retry or use your external browser.")
            false
        }
    }
    }
    private fun bind(addon: WebExtension) {
        addon.setActionDelegate(actionDelegate)
        addon.setTabDelegate(object : WebExtension.TabDelegate {
            override fun onNewTab(source: WebExtension, createDetails: WebExtension.CreateTabDetails): GeckoResult<GeckoSession>? {
                val model = host.get() ?: return null
                val url = createDetails.url ?: "about:blank"
                if (!BrowserUrls.allowed(url, addon.metaData.optionsPageUrl)) return null
                model.dismissPopup()
                return GeckoResult.fromValue(model.newTab(active = createDetails.active != false))
            }
            override fun onOpenOptionsPage(source: WebExtension) { host.get()?.openOptions() }
        })
    }
    fun attach(session: GeckoSession, readerUrl: String? = null) {
        reader.attach(session)
        extension?.let { addon ->
            session.webExtensionController.setActionDelegate(addon, actionDelegate)
            session.webExtensionController.setTabDelegate(addon, object : WebExtension.SessionTabDelegate {
            override fun onUpdateTab(extension: WebExtension, session: GeckoSession, details: WebExtension.UpdateTabDetails): GeckoResult<AllowOrDeny> {
                if (readerUrl != null) return if (details.url == null || com.fliprss.app.data.RenderedArticleText.sameOrigin(readerUrl, details.url!!)) GeckoResult.allow() else GeckoResult.deny()
                val model = host.get() ?: return GeckoResult.deny()
                if (details.url != null && !BrowserUrls.allowed(details.url!!, state.value.optionsUrl)) return GeckoResult.deny()
                if (details.active == true) model.select(session)
                return GeckoResult.allow()
            }
            override fun onCloseTab(source: WebExtension?, session: GeckoSession): GeckoResult<AllowOrDeny> {
                val model = host.get() ?: return GeckoResult.deny()
                return if (model.closeTab(session)) GeckoResult.allow() else GeckoResult.deny()
            }
        }) }
    }
    companion object {
        const val ID = "magnolia@12.34"
        fun addonRequested(context: Context) = context.applicationContext.getSharedPreferences("browser-addon", 0).getBoolean("enabled", false)
        private var instance: BrowserEngine? = null
        fun get(context: Context): BrowserEngine = instance ?: BrowserEngine(context).also { instance = it }
    }
}
