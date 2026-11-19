package com.fliprss.app.browser

import com.fliprss.app.DemoLibrary
import org.junit.Assert.*
import org.junit.Test

class BrowserTextTransferTest {
    @Test fun resultsAreBoundedConsumedOnceAndRejectForeignArticle() {
        val original = DemoLibrary.articles.first().copy(originalUrl = "https://example.com/story", demo = false)
        val token = BrowserTextTransfer.begin(original, original)
        assertFalse(BrowserTextTransfer.complete(token, original.copy(id = "other")))
        assertFalse(BrowserTextTransfer.complete(token, original.copy(originalUrl = "https://example.com/other")))
        assertTrue(BrowserTextTransfer.complete(token, original.copy(paragraphs = listOf("Expanded body"))))
        assertNotNull(BrowserTextTransfer.take(token)?.result)
        assertNull(BrowserTextTransfer.take(token)); assertNull(BrowserTextTransfer.peek(token))
        val tokens = (1..5).map { BrowserTextTransfer.begin(original.copy(id = "id$it"), original) }
        assertNull(BrowserTextTransfer.peek(tokens.first()))
        tokens.forEach(BrowserTextTransfer::discard)
    }
    @Test fun bridgeManifestOnlyInjectsTopLevelWebDocumentsAndEnablesContentMessaging() {
        val module = java.io.File(".").canonicalFile.let { if (java.io.File(it, "src/main/assets").exists()) it else java.io.File(it, "app") }
        val manifest = com.google.gson.JsonParser.parseString(java.io.File(module, "src/main/assets/reader-bridge/manifest.json").readText()).asJsonObject
        val permissions = manifest["permissions"].asJsonArray.map { it.asString }
        assertTrue("nativeMessagingFromContent" in permissions)
        assertTrue("geckoViewAddons" in permissions)
        val script = manifest["content_scripts"].asJsonArray.single().asJsonObject
        assertFalse(script["all_frames"].asBoolean)
        assertEquals(setOf("https://*/*", "http://*/*"), script["matches"].asJsonArray.map { it.asString }.toSet())
        assertFalse(manifest.has("externally_connectable"))
    }

}
