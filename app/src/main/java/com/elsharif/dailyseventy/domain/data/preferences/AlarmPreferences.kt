package com.elsharif.dailyseventy.domain.data.preferences

import android.annotation.SuppressLint
import android.content.Context

object AlarmPreferences {
    private const val PREF_NAME = "step_alarm_prefs"
    private const val KEY_ALARM_HOUR = "alarm_hour"
    private const val KEY_ALARM_MINUTE = "alarm_minute"
    private const val KEY_REQUIRED_STEPS = "required_steps"
    private const val KEY_ALARM_ENABLED = "alarm_enabled"
    private const val KEY_ALARM_SONG_PATH = "alarm_song_path"
    private const val KEY_LAST_ALARM_DATE = "last_alarm_date"

    private const val KEY_ALARM_MUSIC_PLAYING = "alarm_music_playing"

    // إضافة نوع المنبه الجديد
    private const val KEY_ALARM_TYPE = "alarm_type"

    // أنواع المنبه
    const val ALARM_TYPE_MOVEMENT = "movement"
    const val ALARM_TYPE_LIGHT = "light"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // حفظ واسترجاع وقت المنبه
    fun saveAlarmTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit()
            .putInt(KEY_ALARM_HOUR, hour)
            .putInt(KEY_ALARM_MINUTE, minute)
            .apply()
    }

    fun getAlarmHour(context: Context): Int =
        prefs(context).getInt(KEY_ALARM_HOUR, 6)

    fun getAlarmMinute(context: Context): Int =
        prefs(context).getInt(KEY_ALARM_MINUTE, 0)

    // حفظ واسترجاع عدد الخطوات المطلوبة
    fun saveRequiredSteps(context: Context, steps: Int) {
        prefs(context).edit()
            .putInt(KEY_REQUIRED_STEPS, steps)
            .apply()
    }

    fun getRequiredSteps(context: Context): Int =
        prefs(context).getInt(KEY_REQUIRED_STEPS, 50)

    // تفعيل وإلغاء المنبه
    @SuppressLint("UseKtx")
    fun setAlarmEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ALARM_ENABLED, enabled)
            .apply()
    }

    fun isAlarmEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ALARM_ENABLED, false)

    // نوع المنبه (جديد)
    fun saveAlarmType(context: Context, type: String) {
        prefs(context).edit()
            .putString(KEY_ALARM_TYPE, type)
            .apply()
    }

    fun getAlarmType(context: Context): String =
        prefs(context).getString(KEY_ALARM_TYPE, ALARM_TYPE_MOVEMENT) ?: ALARM_TYPE_MOVEMENT

    fun isMovementAlarm(context: Context): Boolean =
        getAlarmType(context) == ALARM_TYPE_MOVEMENT

    fun isLightAlarm(context: Context): Boolean =
        getAlarmType(context) == ALARM_TYPE_LIGHT

    // مسار نغمة المنبه
    fun saveSongPath(context: Context, songPath: String) {
        prefs(context).edit()
            .putString(KEY_ALARM_SONG_PATH, songPath)
            .apply()
    }

    fun getSongPath(context: Context): String =
        prefs(context).getString(
            KEY_ALARM_SONG_PATH,
            "android.resource://com.elsharif.dailyseventy/raw/alarm_song"
        ) ?: "android.resource://com.elsharif.dailyseventy/raw/alarm_song"

    // تاريخ آخر تشغيل للمنبه
    fun setLastAlarmDate(context: Context, date: String) {
        prefs(context).edit()
            .putString(KEY_LAST_ALARM_DATE, date)
            .apply()
    }

    fun getLastAlarmDate(context: Context): String =
        prefs(context).getString(KEY_LAST_ALARM_DATE, "") ?: ""

    // مسح كل البيانات
    @SuppressLint("UseKtx")
    fun clearAllData(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // الحصول على معلومات المنبه في صورة نص
    fun getAlarmInfo(context: Context): String {
        val hour = getAlarmHour(context)
        val minute = getAlarmMinute(context)
        val steps = getRequiredSteps(context)
        val enabled = isAlarmEnabled(context)
        val type = getAlarmType(context)

        val typeText = when (type) {
            ALARM_TYPE_MOVEMENT -> "منبه الحركة"
            ALARM_TYPE_LIGHT -> "منبه الإضاءة"
            else -> "منبه الحركة"
        }

        return if (type == ALARM_TYPE_MOVEMENT) {
            "المنبه: ${if (enabled) "مفعل" else "معطل"} | " +
                    "$typeText | " +
                    "الوقت: ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} | " +
                    "الخطوات: $steps"
        } else {
            "المنبه: ${if (enabled) "مفعل" else "معطل"} | " +
                    "$typeText | " +
                    "يعمل عند الظلام"
        }
    }
    // حالة تشغيل الصوت
    fun setAlarmMusicPlaying(context: Context, playing: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ALARM_MUSIC_PLAYING, playing)
            .apply()
    }

    fun isAlarmMusicPlaying(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ALARM_MUSIC_PLAYING, false)
}