package com.elsharif.dailyseventy.domain.data.preferences

import android.content.Context

object ZekrPrefs {
    private const val PREF_NAME = "zekr_prefs"
    private const val KEY_ENABLED = "zekr_enabled"
    private const val KEY_INTERVAL = "zekr_interval" // in minutes

    fun save(context: Context, enabled: Boolean, intervalMinutes: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_ENABLED, enabled)
            putInt(KEY_INTERVAL, intervalMinutes)
            apply()
        }
    }

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false) // default: disabled
    }

    fun getInterval(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_INTERVAL, 15) // default: 15 minutes
    }
}