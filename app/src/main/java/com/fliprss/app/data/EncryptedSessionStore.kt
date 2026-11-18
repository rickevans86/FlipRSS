package com.fliprss.app.data

import android.content.Context
import android.content.SharedPreferences
import java.security.KeyStore
import java.io.File
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Only the auth token is retained, never the API password. Failures never fall back to plaintext. */
@Suppress("DEPRECATION")
class EncryptedSessionStore(context: Context) : SessionStore {
    private val application = context.applicationContext
    private val mutex = Mutex()
    private var cached: SharedPreferences? = null
    private fun preferences(): SharedPreferences = cached ?: run {
        val key = MasterKey.Builder(application, "fliprss_session_key").setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(application, "freshrss-session", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM).also { cached = it }
    }
    override suspend fun read(): FreshRssSession? = withContext(Dispatchers.IO) { mutex.withLock {
        val endpoint = preferences().getString("endpoint", null) ?: return@withLock null
        val token = preferences().getString("auth", null) ?: return@withLock null
        val username = preferences().getString("username", null) ?: return@withLock null
        val allowHttp = preferences().getBoolean("allow-http", false)
        FreshRssSession(FreshRssEndpoint.normalize(endpoint, allowHttp), username, token, allowHttp)
    } }
    override suspend fun save(session: FreshRssSession) = withContext(Dispatchers.IO) { mutex.withLock {
        if (!preferences().edit().putString("endpoint", session.endpoint).putString("username", session.username)
            .putString("auth", session.token).putBoolean("allow-http", session.allowHttp).commit()) {
            throw FreshRssProblem("Could not securely save your session. Try again.")
        }
    } }
    override suspend fun clear() = withContext(Dispatchers.IO) { mutex.withLock {
        // Forget works even when the old encrypted file/key can no longer be opened.
        if (!application.deleteSharedPreferences("freshrss-session") &&
            File(application.dataDir, "shared_prefs/freshrss-session.xml").exists()) {
            throw FreshRssProblem("Could not remove the saved sign-in. Try again.")
        }
        cached = null
        val keys = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keys.containsAlias("fliprss_session_key")) keys.deleteEntry("fliprss_session_key")
        Unit
    } }
}
