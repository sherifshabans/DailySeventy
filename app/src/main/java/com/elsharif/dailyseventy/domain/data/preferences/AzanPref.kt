package com.elsharif.dailyseventy.domain.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.elsharif.dailyseventy.R

object AzanSoundPrefs {

    private const val PREF_NAME = "azan_prefs"
    private const val KEY_SELECTED_SOUND = "selected_sound_res_id"
    private const val KEY_SELECTED_FAJR_SOUND = "selected_fajr_sound_res_id" // ✨ جديد

    /**
     * Save selected Azan sound for regular prayers
     */
    fun saveSelectedSound(context: Context, soundResId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit(commit = true) {
            putInt(KEY_SELECTED_SOUND, soundResId)
        }
    }

    /**
     * Save selected Azan sound for Fajr prayer
     */
    fun saveSelectedFajrSound(context: Context, soundResId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit(commit = true) {
            putInt(KEY_SELECTED_FAJR_SOUND, soundResId)
        }
    }

    /**
     * Load selected Azan sound for regular prayers
     */
    fun loadSelectedSound(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val resId = prefs.getInt(KEY_SELECTED_SOUND, R.raw.elmola)
        return if (isValidResId(context, resId)) resId else R.raw.elmola
    }

    /**
     * Load selected Azan sound for Fajr prayer
     */
    fun loadSelectedFajrSound(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val resId = prefs.getInt(KEY_SELECTED_FAJR_SOUND, R.raw.mosharyfajr)
        return if (isValidResId(context, resId)) resId else R.raw.mosharyfajr
    }

    /**
     * Get appropriate sound based on prayer type
     */
    fun getSoundForPrayer(context: Context, isFajr: Boolean): Int {
        return if (isFajr) {
            loadSelectedFajrSound(context)
        } else {
            loadSelectedSound(context)
        }
    }

    private fun isValidResId(context: Context, resId: Int): Boolean {
        return try {
            context.resources.getResourceName(resId)
            true
        } catch (e: Exception) {
            false
        }
    }
}