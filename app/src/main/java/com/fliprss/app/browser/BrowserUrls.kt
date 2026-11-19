package com.fliprss.app.browser

import java.net.URI
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object BrowserUrls {
    fun web(value: String): String? {
        val text = value.trim()
        val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(text)
        val hostWithPort = Regex("^[^/?#]+:[0-9]+([/?#]|$)").containsMatchIn(text)
        val address = if (!hasScheme || hostWithPort) "https://$text" else text
        val url = address.toHttpUrlOrNull() ?: return null
        return url.takeIf { it.username.isEmpty() && it.password.isEmpty() }?.toString()
    }
    fun allowed(value: String, optionsUrl: String?): Boolean {
        if (value == "about:blank") return true
        if ((value.startsWith("https://", true) || value.startsWith("http://", true)) && web(value) != null) return true
        return runCatching {
            val uri = URI(value); val addon = URI(optionsUrl ?: return false)
            uri.scheme == "moz-extension" && addon.scheme == "moz-extension" &&
                !uri.host.isNullOrBlank() && uri.host == addon.host && uri.userInfo == null && uri.port == -1
        }.getOrDefault(false)
    }
}
