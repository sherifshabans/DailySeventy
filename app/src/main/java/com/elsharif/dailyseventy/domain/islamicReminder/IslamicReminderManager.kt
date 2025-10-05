package com.elsharif.dailyseventy.domain.islamicReminder

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.IslamicReminderPreferences

object IslamicReminderManager {

    private const val TAG = "IslamicReminderMgr"

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleAllReminders(context: Context, preferences: IslamicReminderPreferences) {
        Log.d(TAG, "Starting to schedule reminders...")

        // Cancel old reminders first
        IslamicReminderScheduler.cancelAllReminders(context)

        // Schedule reminders only if enabled in preferences
        if (preferences.isFastingReminderEnabled) {
            Log.d(TAG, "Fasting reminders enabled")

            if (preferences.isMondayThursdayEnabled) {
                Log.d(TAG, "Scheduling Monday/Thursday reminders")
                IslamicReminderScheduler.scheduleMondayThursdayReminder(context)
            }

            if (preferences.isWhiteDaysEnabled) {
                Log.d(TAG, "Scheduling White Days reminders")
                IslamicReminderScheduler.scheduleWhiteDaysReminder(context)
            }
        } else {
            Log.d(TAG, "Fasting reminders disabled")
        }

        if (preferences.isEidReminderEnabled) {
            Log.d(TAG, "Scheduling Eid reminders")
            IslamicReminderScheduler.scheduleEidReminder(context, "eid_fitr", 10, 1)
            IslamicReminderScheduler.scheduleEidReminder(context, "eid_adha", 12, 10)
        } else {
            Log.d(TAG, "Eid reminders disabled")
        }

        if (preferences.isReligiousOccasionReminderEnabled) {
            Log.d(TAG, "Scheduling religious occasion reminders")
            IslamicReminderScheduler.scheduleReligiousOccasionReminder(
                context, "day_arafat", 12, 9,
                R.string.day_arafat_reminder, R.string.day_arafat_reminder_message
            )
            IslamicReminderScheduler.scheduleReligiousOccasionReminder(
                context, "day_ashura", 1, 10,
                R.string.day_ashura_reminder, R.string.day_ashura_reminder_message
            )
            IslamicReminderScheduler.scheduleReligiousOccasionReminder(
                context, "prophet_birthday", 3, 12,
                R.string.prophet_birthday_reminder, R.string.prophet_birthday_reminder_message
            )
            IslamicReminderScheduler.scheduleReligiousOccasionReminder(
                context, "isra_miraj", 7, 27,
                R.string.isra_miraj_reminder, R.string.isra_miraj_reminder_message
            )
            IslamicReminderScheduler.scheduleReligiousOccasionReminder(
                context, "laylat_qadr", 9, 27,
                R.string.laylat_qadr_reminder, R.string.laylat_qadr_reminder_message
            )
            IslamicReminderScheduler.scheduleReligiousOccasionReminder(
                context, "begin_ramadan", 9, 1,
                R.string.begin_ramadan_reminder, R.string.begin_ramadan_reminder_message
            )
        } else {
            Log.d(TAG, "Religious occasion reminders disabled")
        }

        Log.d(TAG, "Finished scheduling reminders")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateReminderSettings(
        context: Context,
        preferences: IslamicReminderPreferences,
        fastingEnabled: Boolean,
        eidEnabled: Boolean,
        religiousEnabled: Boolean,
        mondayThursdayEnabled: Boolean,
        whiteDaysEnabled: Boolean
    ) {
        Log.d(TAG, "Updating reminder settings...")
        preferences.saveAllSettings(
            fastingEnabled,
            eidEnabled,
            religiousEnabled,
            mondayThursdayEnabled,
            whiteDaysEnabled
        )
        scheduleAllReminders(context, preferences)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cancelSpecificReminder(context: Context, reminderType: String) {
        Log.d(TAG, "Cancelling $reminderType reminders")
        IslamicReminderScheduler.cancelSpecificReminder(context, reminderType)
    }
}