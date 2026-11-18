package com.fliprss.app.data

import android.content.Context
import androidx.core.content.edit

class AndroidSettingsStore(context: Context) : SettingsStore {
    private val preferences = context.applicationContext.getSharedPreferences("app-settings", 0)
    override fun load() = AppSettings(
        theme = runCatching { AppTheme.valueOf(preferences.getString("theme", "SYSTEM")!!) }.getOrDefault(AppTheme.SYSTEM),
        readerTextSize = preferences.getInt("reader-text-size", 19),
        autoMarkRead = preferences.getBoolean("auto-mark-read", true),
        showImages = preferences.getBoolean("show-images", true),
        hideRead = preferences.getBoolean("hide-read", false),
        hideEmptyCategories = preferences.getBoolean("hide-empty-categories", false),
        useLocalFeeds = preferences.getBoolean("use-local-feeds", false),
        showFeedSearch = preferences.getBoolean("show-feed-search", true),
    ).normalized()
    override fun save(settings: AppSettings) {
        val value = settings.normalized()
        preferences.edit {
            putString("theme", value.theme.name); putInt("reader-text-size", value.readerTextSize)
            putBoolean("auto-mark-read", value.autoMarkRead); putBoolean("show-images", value.showImages)
            putBoolean("hide-read", value.hideRead)
            putBoolean("hide-empty-categories", value.hideEmptyCategories)
            putBoolean("use-local-feeds", value.useLocalFeeds)
            putBoolean("show-feed-search", value.showFeedSearch)
        }
    }
}
