package com.fliprss.app

import org.junit.Assert.*
import org.junit.Test

class DemoLibraryTest {
    @Test fun topicAndSavedFiltersIntersectWithoutReordering() {
        val nature = DemoLibrary.articles.filter { it.topic == "Nature" }
        val saved = setOf(nature.last().id, DemoLibrary.articles.first { it.topic == "Design" }.id)
        assertEquals(listOf(nature.last()), DemoLibrary.select("Nature", true, saved))
        assertEquals(DemoLibrary.articles, DemoLibrary.select(null, false, saved))
    }

    @Test fun emptyAndStaleBookmarksDoNotCreatePhantomStories() {
        assertTrue(DemoLibrary.select(null, true, emptySet()).isEmpty())
        assertTrue(DemoLibrary.select(null, true, setOf("deleted-story")).isEmpty())
    }

    @Test fun everyTopicHasStoriesAndIdsAreStableAndUnique() {
        assertEquals(DemoLibrary.articles.size, DemoLibrary.articles.map { it.id }.toSet().size)
        DemoLibrary.topics.forEach { topic ->
            assertTrue(DemoLibrary.select(topic, false, emptySet()).isNotEmpty())
        }
    }
}
