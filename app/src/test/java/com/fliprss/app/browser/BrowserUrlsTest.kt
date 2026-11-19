package com.fliprss.app.browser

import org.junit.Assert.*
import org.junit.Test

class BrowserUrlsTest {
    @Test fun acceptsAndNormalizesWebAddresses() {
        assertEquals("https://example.com/story", BrowserUrls.web("example.com/story"))
        assertEquals("http://example.com/story", BrowserUrls.web("http://example.com/story"))
        assertEquals("https://localhost:8080/story", BrowserUrls.web("localhost:8080/story"))
        assertEquals("https://example.com/a%20b", BrowserUrls.web("https://example.com/a b"))
    }
    @Test fun rejectsCredentialsAndNonWebSchemes() {
        listOf("https://user:secret@example.com/", "javascript:alert(1)", "data:text/html,test", "file:///tmp/file", "intent://example.com", "about:config", "moz-extension://uuid/options.html", "ftp://example.com").forEach {
            assertNull(it, BrowserUrls.web(it))
        }
    }
    @Test fun onlyAllowsSameAddonOriginForTrustedExtensionPages() {
        val options = "moz-extension://uuid/options/options.html"
        assertTrue(BrowserUrls.allowed(options, options))
        assertTrue(BrowserUrls.allowed("moz-extension://uuid/options/popup.html", options))
        assertTrue(BrowserUrls.allowed("https://example.com/article", options))
        assertTrue(BrowserUrls.allowed("about:blank", options))
        for (url in listOf("moz-extension://uuid.evil/options/options.html", "moz-extension://other/options/options.html", "moz-extension://user@uuid/options.html", "moz-extension://uuid:99/options.html", "file:///tmp/file", "javascript:alert(1)", "about:config")) {
            assertFalse(url, BrowserUrls.allowed(url, options))
        }
        assertFalse(BrowserUrls.allowed(options, null))
    }
}
