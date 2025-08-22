// ThemePreferences.kt
package com.elsharif.dailyseventy.domain.data.shardprefernces

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("theme_prefs")

class ThemePreferences(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("theme_cache", Context.MODE_PRIVATE)

    companion object {
        private val COLOR_KEY = intPreferencesKey("user_color")
        private const val CACHE_KEY = "cached_user_color"
        val DEFAULT_COLOR = Color(0xFF2196F3).toArgb() // expose as public if needed
    }

    // Flow from DataStore, but also cache in SharedPreferences for instant load
    val userColorFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        val color = prefs[COLOR_KEY] ?: DEFAULT_COLOR
        sharedPrefs.edit().putInt(CACHE_KEY, color).apply()
        color
    }

    // Load instantly from cache
    fun getCachedColor(): Int {
        return sharedPrefs.getInt(CACHE_KEY, DEFAULT_COLOR)
    }

    // Save to both DataStore and cache
    suspend fun saveUserColor(color: Int) {
        context.dataStore.edit { prefs -> prefs[COLOR_KEY] = color }
        sharedPrefs.edit().putInt(CACHE_KEY, color).apply()
    }
}
