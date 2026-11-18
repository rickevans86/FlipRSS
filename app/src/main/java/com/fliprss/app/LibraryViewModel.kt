package com.fliprss.app

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fliprss.app.data.*

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("demo-reading-state", 0)
    val library = LibraryController(viewModelScope, GReaderConnector(), EncryptedSessionStore(application),
        object : DemoPreferences {
            override fun load() = DemoReadingState(
                preferences.getStringSet("saved", emptySet()).orEmpty().toSet(),
                preferences.getStringSet("read", emptySet()).orEmpty().toSet())
            override fun save(state: DemoReadingState) {
                preferences.edit { putStringSet("saved", state.saved); putStringSet("read", state.read) }
            }
        }, AndroidSettingsStore(application))
}
