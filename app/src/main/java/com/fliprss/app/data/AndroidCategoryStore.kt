package com.fliprss.app.data

import android.content.Context
import androidx.core.content.edit

internal class AndroidCategoryStore(context: Context) : CategoryStore {
    private val preferences = context.applicationContext.getSharedPreferences("reading-categories", 0)
    override fun load(namespace: String): CategoryPreference? =
        if (!preferences.contains(namespace)) null else CategoryPreference(preferences.getString(namespace, "").orEmpty().ifEmpty { null })
    override fun save(namespace: String, value: CategoryPreference) {
        preferences.edit { putString(namespace, value.id.orEmpty()) }
    }
}
