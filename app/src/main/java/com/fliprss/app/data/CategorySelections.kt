package com.fliprss.app.data

import java.security.MessageDigest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** An absent preference chooses the first category; an explicit null chooses All stories. */
internal data class CategoryPreference(val id: String?)
internal interface CategoryStore {
    fun load(namespace: String): CategoryPreference?
    fun save(namespace: String, value: CategoryPreference)
}
internal fun categoryNamespace(destination: String, account: Account?): String = when (destination) {
    "FlipRSS" -> "local"
    "FreshRSS" -> "fresh:" + MessageDigest.getInstance("SHA-256")
        .digest("${account?.endpoint.orEmpty()}\u0000${account?.username.orEmpty()}".toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    else -> "page:$destination"
}
internal fun readingCategory(preference: CategoryPreference?, available: List<String>): String? =
    if (preference != null && preference.id == null) null
    else preference?.id?.takeIf { it in available } ?: available.firstOrNull()

internal class CategorySelections(private val store: CategoryStore) {
    private val mutable = MutableStateFlow(0L)
    val revision = mutable.asStateFlow()
    fun preference(namespace: String) = store.load(namespace)
    fun select(namespace: String, category: String?) {
        val value = CategoryPreference(category)
        if (store.load(namespace) == value) return
        store.save(namespace, value)
        mutable.value++
    }
}
