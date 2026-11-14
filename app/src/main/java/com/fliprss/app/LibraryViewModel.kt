package com.fliprss.app

import android.app.Application
import androidx.core.content.edit
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("demo-reading-state", 0)
    val savedIds = mutableStateListOf<String>().apply {
        addAll(preferences.getStringSet("saved", emptySet()).orEmpty())
    }
    val readIds = mutableStateListOf<String>().apply {
        addAll(preferences.getStringSet("read", emptySet()).orEmpty())
    }

    fun toggleSaved(id: String) {
        if (id in savedIds) savedIds.remove(id) else savedIds.add(id)
        preferences.edit { putStringSet("saved", savedIds.toSet()) }
    }

    fun markRead(id: String) {
        if (id !in readIds) {
            readIds.add(id)
            preferences.edit { putStringSet("read", readIds.toSet()) }
        }
    }
}
