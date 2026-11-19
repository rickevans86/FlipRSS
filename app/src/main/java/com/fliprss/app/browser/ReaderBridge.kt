package com.fliprss.app.browser

import com.fliprss.app.Article
import com.fliprss.app.data.RenderedArticleText
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import org.mozilla.geckoview.*

/** Only the owned extension, registered session, top-level content frame and outstanding nonce may reply. */
internal class ReaderBridge(private val runtime: GeckoRuntime) {
    private class Pending(val nonce: String, val article: Article, val result: CompletableDeferred<Article?>) { val ready = CompletableDeferred<WebExtension.Port?>(); var sent: WebExtension.Port? = null }
    private class Slot { var documentUrl: String? = null; val port = MutableStateFlow<WebExtension.Port?>(null); var pending: Pending? = null }
    private val slots = mutableMapOf<GeckoSession, Slot>()
    private val mutex = Mutex()
    private var extension: WebExtension? = null
    suspend fun prepare() = mutex.withLock {
        if (extension == null) extension = runtime.webExtensionController.ensureBuiltIn("resource://android/assets/reader-bridge/", ID).awaitResult()
    }
    fun attach(session: GeckoSession) {
        val addon = extension ?: return
        if (slots.containsKey(session)) return
        val slot = slots.getOrPut(session) { Slot() }
        session.webExtensionController.setMessageDelegate(addon, object : WebExtension.MessageDelegate {
            override fun onConnect(port: WebExtension.Port) {
                val sender = port.sender
                if (slots[session] !== slot || sender.webExtension.id != ID || sender.session !== session || !sender.isTopLevel() ||
                    sender.environmentType != WebExtension.MessageSender.ENV_TYPE_CONTENT_SCRIPT || BrowserUrls.web(sender.url) == null) {
                    port.disconnect(); return
                }
                slot.port.value?.let { if (it !== port) it.disconnect() }
                slot.port.value = port
                slot.pending?.ready?.complete(port)
                port.setDelegate(object : WebExtension.PortDelegate {
                    override fun onPortMessage(message: Any, port: WebExtension.Port) {
                        val pending = slot.pending ?: return
                        if (slots[session] !== slot || slot.port.value !== port || pending.sent !== port || message !is JSONObject ||
                            message.optString("type") != "articleText" || message.optString("nonce") != pending.nonce) return
                        val documentUrl = slot.documentUrl ?: sender.url
                        if (!RenderedArticleText.sameOrigin(sender.url, documentUrl)) return
                        pending.result.complete(RenderedArticleText.parse(message.toString(), pending.article, pending.nonce, documentUrl))
                    }
                    override fun onDisconnect(port: WebExtension.Port) {
                        if (slot.port.value === port) slot.port.value = null
                        if (slot.pending?.sent === port) slot.pending?.result?.complete(null)
                    }
                })
            }
        }, APP)
    }
    suspend fun capture(session: GeckoSession, article: Article, automatic: Boolean): Article? {
        prepare(); attach(session)
        val slot = slots.getValue(session)
        abort(session)
        val pending = Pending(UUID.randomUUID().toString(), article, CompletableDeferred())
        slot.pending = pending
        slot.port.value?.let { pending.ready.complete(it) }
        return try {
            withTimeoutOrNull(if (automatic) 26_000L else 16_000L) {
                val port = pending.ready.await() ?: return@withTimeoutOrNull null
                if (slot.pending !== pending || slot.port.value !== port) return@withTimeoutOrNull null
                pending.sent = port
                val chars = article.paragraphs.sumOf(String::length)
                port.postMessage(JSONObject().put("type", "extract").put("nonce", pending.nonce)
                    .put("minimum", chars + maxOf(150, chars / 10)).put("deadline", if (automatic) 18000 else 12000)
                    .put("minWait", if (automatic) 6000 else 0).put("settle", if (automatic) 1500 else 500))
                pending.result.await()
            }
        } finally {
            if (slot.pending === pending) slot.pending = null
            runCatching { pending.sent?.postMessage(JSONObject().put("type", "cancel").put("nonce", pending.nonce)) }
        }
    }
    fun location(session: GeckoSession, url: String?) { slots[session]?.documentUrl = url?.let(BrowserUrls::web) }
    fun abort(session: GeckoSession) {
        val slot = slots[session] ?: return
        val pending = slot.pending ?: return
        slot.pending = null; pending.ready.complete(null); pending.result.complete(null)
        runCatching { pending.sent?.postMessage(JSONObject().put("type", "cancel").put("nonce", pending.nonce)) }
    }
    fun detach(session: GeckoSession) {
        abort(session)
        slots.remove(session)?.port?.value?.let { runCatching { it.disconnect() } }
        extension?.let { session.webExtensionController.setMessageDelegate(it, null, APP) }
    }
    companion object { const val ID = "reader-bridge@fliprss.app"; const val APP = "fliprss_reader" }
}
