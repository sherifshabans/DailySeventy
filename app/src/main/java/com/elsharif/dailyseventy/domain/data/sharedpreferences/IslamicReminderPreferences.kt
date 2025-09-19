package com.elsharif.dailyseventy.domain.data.sharedpreferences

import android.content.Context

class IslamicReminderPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("islamic_reminders", Context.MODE_PRIVATE)

    var isFastingReminderEnabled: Boolean
        get() = prefs.getBoolean("fasting_reminder_enabled", true)
        set(value) = prefs.edit().putBoolean("fasting_reminder_enabled", value).apply()

    var isEidReminderEnabled: Boolean
        get() = prefs.getBoolean("eid_reminder_enabled", true)
        set(value) = prefs.edit().putBoolean("eid_reminder_enabled", value).apply()

    var isReligiousOccasionReminderEnabled: Boolean
        get() = prefs.getBoolean("religious_occasion_enabled", true)
        set(value) = prefs.edit().putBoolean("religious_occasion_enabled", value).apply()

    var isMondayThursdayEnabled: Boolean
        get() = prefs.getBoolean("monday_thursday_enabled", true)
        set(value) = prefs.edit().putBoolean("monday_thursday_enabled", value).apply()

    var isWhiteDaysEnabled: Boolean
        get() = prefs.getBoolean("white_days_enabled", true)
        set(value) = prefs.edit().putBoolean("white_days_enabled", value).apply()
}
