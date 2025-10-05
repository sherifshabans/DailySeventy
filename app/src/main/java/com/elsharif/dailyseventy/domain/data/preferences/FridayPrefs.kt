package com.elsharif.dailyseventy.domain.data.preferences

import android.content.Context

object FridayPrefs {
    private const val PREF_NAME = "friday_prefs"
    private const val KEY_KAHF = "kahf_enabled"
    private const val KEY_ASR = "asr_enabled"

    fun save(context: Context, kahf: Boolean, asr: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_KAHF, kahf)
            putBoolean(KEY_ASR, asr)
            apply()
        }
    }

    fun loadKahf(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_KAHF, true) // default: enabled
    }

    fun loadAsr(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ASR, true) // default: enabled
    }
}
