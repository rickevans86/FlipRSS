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
        markReadOnScroll = preferences.getBoolean("mark-read-on-scroll", false),
        confirmMarkAllRead = preferences.getBoolean("confirm-mark-all-read", true),
        autoLoadFullArticles = preferences.getBoolean("auto-load-full-articles", true),
        speech = SpeechOptions(preferences.getString("speech-engine", "").orEmpty(), preferences.getString("speech-voice", "").orEmpty(),
            preferences.getBoolean("speech-online", false), preferences.getInt("speech-rate", 95), preferences.getInt("speech-pitch", 100)),
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
            putBoolean("mark-read-on-scroll", value.markReadOnScroll)
            putBoolean("confirm-mark-all-read", value.confirmMarkAllRead)
            putBoolean("auto-load-full-articles", value.autoLoadFullArticles)
            putString("speech-engine", value.speech.engine); putString("speech-voice", value.speech.voice)
            putBoolean("speech-online", value.speech.allowOnline); putInt("speech-rate", value.speech.ratePercent)
            putInt("speech-pitch", value.speech.pitchPercent)
        }
    }
}
