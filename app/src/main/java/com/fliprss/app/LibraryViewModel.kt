package com.fliprss.app

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fliprss.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("demo-reading-state", 0)
    private val settings = AndroidSettingsStore(application)
    internal val categories = CategorySelections(AndroidCategoryStore(application))
    private val demo = object : DemoPreferences {
        override fun load() = DemoReadingState(preferences.getStringSet("saved", emptySet()).orEmpty().toSet(),
            preferences.getStringSet("read", emptySet()).orEmpty().toSet())
        override fun save(state: DemoReadingState) { preferences.edit { putStringSet("saved", state.saved); putStringSet("read", state.read) } }
    }
    private val noSession = object : SessionStore {
        override suspend fun read(): FreshRssSession? = null
        override suspend fun save(session: FreshRssSession) = Unit
        override suspend fun clear() = Unit
    }
    internal val localRepository = LocalFeedRepository(FileLocalFeedStore(java.io.File(application.filesDir, "local-feeds.json")))
    val localLibrary = LibraryController(viewModelScope, GReaderConnector(), noSession, demo, settings, localRepository = localRepository)
    val freshLibrary = LibraryController(viewModelScope, GReaderConnector(), EncryptedSessionStore(application), demo, settings)
    // Compatibility for reader/demo fixtures; production navigation selects sources explicitly.
    internal val browserText = com.fliprss.app.browser.BrowserArticleRenderer(application)
    internal val articleContent = ArticleContentController(viewModelScope, render = browserText::render,
        autoBrowser = { settings.load().useBuiltInBrowser },
        expandWithBrowser = { com.fliprss.app.browser.BrowserEngine.addonRequested(application) })
    val library get() = localLibrary
    private val mutableFeeds = MutableStateFlow<List<LocalFeed>>(emptyList())
    internal val localFeeds = mutableFeeds.asStateFlow()
    init { viewModelScope.launch { localLibrary.state.collect { value ->
        if (!value.restoring && !value.loading && !value.addingFeed) runCatching { mutableFeeds.value = localRepository.subscriptions() }
    } } }
    fun updateSettings(value: AppSettings) { localLibrary.updateSettings(value); freshLibrary.updateSettings(value); directory.setEnabled(value.showFeedSearch) }
    internal fun owner(article: Article) = if (article.local || article.demo) localLibrary else freshLibrary
    fun findArticle(id: String) = localLibrary.findArticle(id) ?: freshLibrary.findArticle(id)
    internal val directory = FeedSearchModel(viewModelScope, java.io.File(application.filesDir, "feed-directory.json"), autoRefresh = settings.load().showFeedSearch)
}
