package com.elsharif.dailyseventy.domain.data.preferences

import android.content.Context
import androidx.core.content.edit

class IslamicReminderPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("islamic_reminders", Context.MODE_PRIVATE)


    var isFastingReminderEnabled: Boolean
        get() = prefs.getBoolean("fasting_reminder_enabled", true)
        set(value) = prefs.edit { putBoolean("fasting_reminder_enabled", value) }

    var isEidReminderEnabled: Boolean
        get() = prefs.getBoolean("eid_reminder_enabled", true)
        set(value) = prefs.edit { putBoolean("eid_reminder_enabled", value) }

    var isReligiousOccasionReminderEnabled: Boolean
        get() = prefs.getBoolean("religious_occasion_enabled", true)
        set(value) = prefs.edit { putBoolean("religious_occasion_enabled", value) }

    var isMondayThursdayEnabled: Boolean
        get() = prefs.getBoolean("monday_thursday_enabled", true)
        set(value) = prefs.edit { putBoolean("monday_thursday_enabled", value) }

    var isWhiteDaysEnabled: Boolean
        get() = prefs.getBoolean("white_days_enabled", true)
        set(value) = prefs.edit { putBoolean("white_days_enabled", value) }

    // دالة لحفظ جميع الإعدادات مرة واحدة
    fun saveAllSettings(
        fastingEnabled: Boolean,
        eidEnabled: Boolean,
        religiousEnabled: Boolean,
        mondayThursdayEnabled: Boolean,
        whiteDaysEnabled: Boolean
    ) {
        prefs.edit {
            putBoolean("fasting_reminder_enabled", fastingEnabled)
            putBoolean("eid_reminder_enabled", eidEnabled)
            putBoolean("religious_occasion_enabled", religiousEnabled)
            putBoolean("monday_thursday_enabled", mondayThursdayEnabled)
            putBoolean("white_days_enabled", whiteDaysEnabled)
        }
    }
}