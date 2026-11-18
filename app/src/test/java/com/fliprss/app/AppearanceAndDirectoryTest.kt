package com.fliprss.app

import androidx.compose.ui.graphics.Color
import com.fliprss.app.data.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.*
import org.junit.Test

class AppearanceAndDirectoryTest {
    @Test fun oledUsesPureBlackNeutralSurfacesInBothSystemModes() {
        for (systemDark in listOf(false, true)) {
            val colors = appColors(AppTheme.OLED, systemDark)
            listOf(colors.background, colors.surface, colors.surfaceVariant, colors.surfaceDim, colors.surfaceBright,
                colors.surfaceContainerLowest, colors.surfaceContainerLow, colors.surfaceContainer,
                colors.surfaceContainerHigh, colors.surfaceContainerHighest).forEach { assertEquals(Color.Black, it) }
            assertEquals(Color.Transparent, colors.surfaceTint)
            assertNotEquals(Color.Black, colors.onSurface)
        }
        assertNotEquals(Color.Black, appColors(AppTheme.DARK, false).background)
        assertEquals(appColors(AppTheme.DARK, false).background, appColors(AppTheme.SYSTEM, true).background)
        assertEquals(appColors(AppTheme.LIGHT, true).background, appColors(AppTheme.SYSTEM, false).background)
    }
    @Test fun directorySearchMatchesWordsTopicsAndPublishersWithoutCaseSensitivity() {
        assertTrue(FeedDirectory.search("").isNotEmpty())
        assertEquals(listOf("android"), FeedDirectory.search(" ANDROID ").map { it.id })
        assertTrue(FeedDirectory.search("space science").any { it.id == "nasa" })
        assertTrue(FeedDirectory.search("science", "Technology").all { it.category == "Technology" })
        assertTrue(FeedDirectory.search("no-such-publisher-123").isEmpty())
        assertTrue(FeedDirectory.search("Android", "Science").isEmpty())
    }
    @Test fun allDirectoryEntriesHaveUniqueIdsAndSafeDirectHttpsFeedUrls() {
        val entries = FeedDirectory.feeds
        assertEquals(entries.size, entries.map { it.id }.toSet().size)
        assertEquals(entries.size, entries.map { it.url }.toSet().size)
        entries.forEach { feed ->
            val url = feed.url.toHttpUrl()
            assertEquals("https", url.scheme); assertTrue(url.username.isEmpty()); assertTrue(url.password.isEmpty())
            assertTrue(feed.title.isNotBlank()); assertTrue(feed.description.isNotBlank())
            assertTrue(feed.category in FeedDirectory.categories)
        }
    }
    @Test fun disablingSearchRemovesTabAndReturnsCurrentSearchToToday() {
        assertTrue("Search" in libraryDestinations(true))
        assertFalse("Search" in libraryDestinations(false))
        assertEquals("Today", availableDestination("Search", false))
        assertEquals("Saved", availableDestination("Saved", false))
        assertEquals("Search", availableDestination("Search", true))
    }
    @Test fun oledAndSearchPreferenceSurviveSettingsRoundTripAndNormalization() {
        val store = MemorySettingsStore()
        store.save(AppSettings(theme = AppTheme.OLED, showFeedSearch = false, readerTextSize = 99))
        assertEquals(AppTheme.OLED, store.load().theme)
        assertFalse(store.load().showFeedSearch)
        assertEquals(26, store.load().readerTextSize)
        assertTrue(AppSettings().showFeedSearch)
    }
}
