package com.fliprss.app.browser

import android.content.Context
import com.fliprss.app.Article
import com.fliprss.app.data.RenderedArticleText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.*

/** Reader-owned, displayed rendering surface; script/dialog/media permissions are not granted. */
internal class BrowserArticleRenderer(private val context: Context) {
    private val mutable = MutableStateFlow<GeckoSession?>(null)
    val session = mutable.asStateFlow()
    private var foreground = false
    private var engine: BrowserEngine? = null
    fun foreground(value: Boolean) { foreground = value; if (!value) mutable.value?.let { engine?.reader?.abort(it) } }
    suspend fun render(article: Article): Article? = withContext(Dispatchers.Main.immediate) {
        val url = article.originalUrl?.let(BrowserUrls::web) ?: return@withContext null
        if (!foreground) return@withContext null
        val browser = BrowserEngine.get(context); engine = browser
        if (!browser.configure(browser.requestedEnabled) || !foreground) return@withContext null
        browser.reader.prepare()
        val page = GeckoSession()
        page.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, location: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, hasUserGesture: Boolean) {
                browser.reader.location(session, location)
            }
            override fun onLoadRequest(session: GeckoSession, request: GeckoSession.NavigationDelegate.LoadRequest): GeckoResult<AllowOrDeny> =
                if (request.uri == "about:blank" || RenderedArticleText.sameOrigin(url, request.uri)) GeckoResult.allow() else {
                    browser.reader.abort(page); GeckoResult.deny()
                }
            override fun onLoadError(session: GeckoSession, uri: String?, error: WebRequestError): GeckoResult<String>? {
                browser.reader.abort(page); return null
            }
        }
        page.open(browser.runtime); browser.attach(page, readerUrl = url)
        page.setActive(true); browser.runtime.webExtensionController.setTabActive(page, true)
        mutable.value?.let { browser.reader.abort(it) }; mutable.value = page
        try {
            coroutineScope {
                val result = async(start = CoroutineStart.UNDISPATCHED) { browser.reader.capture(page, article, true) }
                page.loadUri(url)
                result.await()
            }
        } finally {
            if (mutable.value === page) mutable.value = null
            browser.detach(page)
            page.setActive(false); browser.runtime.webExtensionController.setTabActive(page, false); page.close()
        }
    }
}
