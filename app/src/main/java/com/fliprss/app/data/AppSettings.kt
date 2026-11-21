package com.fliprss.app.data

enum class AppTheme { SYSTEM, LIGHT, DARK, OLED }
enum class FeedLayout { TILE, LIST }
data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val feedLayout: FeedLayout = FeedLayout.TILE,
    val readerTextSize: Int = 19,
    val autoMarkRead: Boolean = true,
    val showImages: Boolean = true,
    val hideRead: Boolean = false,
    val hideEmptyCategories: Boolean = false,
    val useLocalFeeds: Boolean = false,
    val showFeedSearch: Boolean = true,
    val showFreshRss: Boolean = true,
    val showFlipRss: Boolean = true,
    val markReadOnScroll: Boolean = false,
    val confirmMarkAllRead: Boolean = true,
    val autoLoadFullArticles: Boolean = true,
    val useBuiltInBrowser: Boolean = true,
    val speech: SpeechOptions = SpeechOptions(),
) {
    fun normalized() = copy(readerTextSize = readerTextSize.coerceIn(16, 26), speech = speech.normalized())
}
interface SettingsStore {
    fun load(): AppSettings
    fun save(settings: AppSettings)
}
class MemorySettingsStore : SettingsStore {
    private var value = AppSettings()
    override fun load() = value
    override fun save(settings: AppSettings) { value = settings.normalized() }
}
/** Captured when confirmation opens: later navigation/account changes cannot widen its scope. */
class MarkReadTarget internal constructor(
    val stream: String, val title: String, val cutoffMicros: Long,
    internal val accountGeneration: Int, internal val knownIds: Set<String>,
)
