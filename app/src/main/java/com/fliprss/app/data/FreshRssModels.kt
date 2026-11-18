package com.fliprss.app.data

import com.fliprss.app.Article

const val READING_LIST = "user/-/state/com.google/reading-list"
const val STARRED = "user/-/state/com.google/starred"
const val READ = "user/-/state/com.google/read"
const val LABEL_PREFIX = "user/-/label/"

// Deliberately not a data class: a generated toString must never expose the token.
class FreshRssSession(val endpoint: String, val username: String, val token: String, val allowHttp: Boolean)
data class Account(val endpoint: String, val username: String, val allowHttp: Boolean)
data class Section(val id: String, val title: String, val feed: Boolean = false, val unread: Int? = null)
data class Catalogue(val sections: List<Section>, val unread: Int?)
data class ArticlePage(val articles: List<Article>, val continuation: String?)
data class LibraryScope(val sectionId: String? = null, val savedOnly: Boolean = false) {
    val stream: String get() = if (savedOnly) STARRED else sectionId ?: READING_LIST
}

class FreshRssProblem(message: String, val signInRequired: Boolean = false) : Exception(message)
interface FreshRssConnector {
    suspend fun login(server: String, username: String, password: String, allowHttp: Boolean): FreshRssSession
    fun repository(session: FreshRssSession): FreshRssRepository
}
interface FreshRssRepository {
    suspend fun catalogue(): Catalogue
    suspend fun page(stream: String, continuation: String?): ArticlePage
    suspend fun setRead(id: String, read: Boolean)
    suspend fun setStarred(id: String, starred: Boolean)
}
interface SessionStore {
    suspend fun read(): FreshRssSession?
    suspend fun save(session: FreshRssSession)
    suspend fun clear()
}
