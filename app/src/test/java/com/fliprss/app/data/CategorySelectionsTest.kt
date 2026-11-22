package com.fliprss.app.data

import org.junit.Assert.*
import org.junit.Test

class CategorySelectionsTest {
    private class Store : CategoryStore {
        val values = mutableMapOf<String, CategoryPreference>()
        var writes = 0
        override fun load(namespace: String) = values[namespace]
        override fun save(namespace: String, value: CategoryPreference) { values[namespace] = value; writes++ }
    }
    @Test fun firstUseSelectsFirstAvailableCategoryInsteadOfAllStories() {
        assertEquals("news", readingCategory(null, listOf("news", "tech")))
        assertNull(readingCategory(null, emptyList()))
    }
    @Test fun explicitAllStoriesRemainsAValidChoice() {
        assertNull(readingCategory(CategoryPreference(null), listOf("news", "tech")))
        assertNull(readingCategory(CategoryPreference(null), emptyList()))
    }
    @Test fun rememberedCategoryWinsOverCatalogueOrder() {
        assertEquals("tech", readingCategory(CategoryPreference("tech"), listOf("news", "tech")))
        assertEquals("tech", readingCategory(CategoryPreference("tech"), listOf("arts", "tech", "news")))
    }
    @Test fun removedOrHiddenCategoryFallsBackWithoutInventingAnEmptyCategory() {
        val remembered = CategoryPreference("news")
        assertEquals("tech", readingCategory(remembered, listOf("tech", "arts")))
        assertNull(readingCategory(remembered, emptyList()))
        assertEquals("news", remembered.id)
    }
    @Test fun sourceAndAccountNamespacesDoNotExposeOrMixAccountDetails() {
        val account = Account("https://example.org/api/greader.php", "rick", false)
        val fresh = categoryNamespace("FreshRSS", account)
        assertEquals(fresh, categoryNamespace("FreshRSS", account))
        assertFalse(fresh.contains("rick")); assertFalse(fresh.contains("example.org"))
        assertNotEquals(fresh, categoryNamespace("FreshRSS", account.copy(username = "other")))
        assertNotEquals(fresh, categoryNamespace("FreshRSS", account.copy(endpoint = "https://other.org/api/greader.php")))
        assertNotEquals(fresh, categoryNamespace("FlipRSS", account))
        assertNotEquals(categoryNamespace("Saved", account), categoryNamespace("FlipRSS", account))
    }
    @Test fun selectionsSurviveRecreationAndDoNotOverwriteAnotherSource() {
        val store = Store(); val first = CategorySelections(store)
        first.select("local", "news"); first.select("fresh:account", "tech")
        val restored = CategorySelections(store)
        assertEquals("news", restored.preference("local")?.id)
        assertEquals("tech", restored.preference("fresh:account")?.id)
        restored.select("fresh:account", null)
        assertEquals(CategoryPreference(null), CategorySelections(store).preference("fresh:account"))
        assertEquals("news", restored.preference("local")?.id)
        val writes = store.writes; val revision = restored.revision.value
        restored.select("fresh:account", null)
        assertEquals(writes, store.writes); assertEquals(revision, restored.revision.value)
    }
}
