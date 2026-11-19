package com.fliprss.app.browser

import com.fliprss.app.Article
import java.util.UUID

/** Tiny in-process handoff: no article body in Android Binder extras, no persistence/account writes. */
internal object BrowserTextTransfer {
    data class Entry(val original: Article, val baseline: Article, val created: Long, var result: Article? = null)
    private val entries = linkedMapOf<String, Entry>()
    private fun prune() { entries.entries.removeAll { System.currentTimeMillis() - it.value.created > 15 * 60_000 } }
    fun begin(original: Article, baseline: Article): String {
        prune(); while (entries.size >= 4) entries.remove(entries.keys.first())
        return UUID.randomUUID().toString().also { entries[it] = Entry(original, baseline, System.currentTimeMillis()) }
    }
    fun peek(token: String?) = token?.let { prune(); entries[it] }
    fun complete(token: String, result: Article): Boolean {
        val entry = peek(token) ?: return false
        if (result.id != entry.original.id || result.originalUrl != entry.original.originalUrl ||
            result.local != entry.original.local || result.demo != entry.original.demo) return false
        entry.result = result; return true
    }
    fun take(token: String) = entries.remove(token)
    fun discard(token: String?) { entries.remove(token) }
}
