package com.fliprss.app

import androidx.test.platform.app.InstrumentationRegistry
import com.fliprss.app.data.EncryptedSessionStore
import com.fliprss.app.data.FreshRssSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SessionStoreTest {
    @Test fun encryptedSessionRoundTripsWithoutPlaintextAndCanBeForgotten() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = EncryptedSessionStore(context)
        try {
            store.save(FreshRssSession("https://rss.example.org/api/greader.php/", "dummy-account", "dummy-test-token", false))
            val raw = context.getSharedPreferences("freshrss-session", 0).all.values.joinToString()
            assertFalse(raw.contains("dummy-test-token"))
            assertFalse(raw.contains("dummy-account"))
            val restored = store.read()!!
            assertEquals("dummy-account", restored.username)
            assertEquals("dummy-test-token", restored.token)
            store.clear()
            assertNull(store.read())
        } finally { store.clear() }
    }
}
