package com.elsharif.dailyseventy.domain.islamicReminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.sharedpreferences.IslamicReminderPreferences
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class IslamicReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val preferences = IslamicReminderPreferences(context)
    private val notificationHelper = NotificationHelper(context)
    private val locale = context.resources.configuration.locales[0]

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            checkTodayReminders()
            checkTomorrowReminders()
            Result.success()
        } catch (e: Exception) {
            Log.e("IslamicReminderWorker", "Error in doWork", e)
            Result.retry()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun checkTodayReminders() {
        val hijriToday = IslamicCalendarHelper.getCurrentHijriDate()

        // فحص الأعياد اليوم
        checkEidToday(hijriToday)

        // فحص المناسبات الدينية اليوم
        checkReligiousOccasionsToday(hijriToday)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun checkTomorrowReminders() {
        val tomorrow = LocalDate.now().plusDays(1)
        val hijriTomorrow = IslamicCalendarHelper
            .getCurrentHijriDate()
            .plus(1, ChronoUnit.DAYS)

        // فحص صيام الإثنين والخميس
        checkMondayThursdayFasting(tomorrow)

        // فحص الأيام البيض
        checkWhiteDaysFasting(hijriTomorrow)

        // فحص أيام الصيام الخاصة
        checkSpecialFastingDays(hijriTomorrow)
    }

    @SuppressLint("MissingPermission", "StringFormatInvalid")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkEidToday(hijriDate: HijrahDate) {
        if (!preferences.isEidReminderEnabled) return

        val events = IslamicCalendarHelper.getIslamicEvents(context)
            .filter { it.type == EventType.EID }

        events.forEach { event ->
            if (hijriDate.get(ChronoField.MONTH_OF_YEAR) == event.hijriMonth &&
                hijriDate.get(ChronoField.DAY_OF_MONTH) == event.hijriDay) {

                val title = context.getString(R.string.eid_title, event.nameAr)
                val message = context.getString(R.string.eid_message, event.nameAr)

                notificationHelper.showGeneralNotification(title, message)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkReligiousOccasionsToday(hijriDate: HijrahDate) {
        if (!preferences.isReligiousOccasionReminderEnabled) return

        val events = IslamicCalendarHelper.getIslamicEvents(context)
            .filter { it.type == EventType.RELIGIOUS_OCCASION }

        events.forEach { event ->
            if (hijriDate.get(ChronoField.MONTH_OF_YEAR) == event.hijriMonth &&
                hijriDate.get(ChronoField.DAY_OF_MONTH) == event.hijriDay) {

                val title = context.getString(R.string.occasion_title, event.nameAr)
                val message = context.getString(R.string.occasion_message, event.nameAr)

                notificationHelper.showGeneralNotification(title, message)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun checkMondayThursdayFasting(tomorrow: LocalDate) {
        if (!preferences.isFastingReminderEnabled || !preferences.isMondayThursdayEnabled) return

        val title = context.getString(R.string.fasting_title)
        val message = when {
            IslamicCalendarHelper.isMonday(tomorrow) ->
                context.getString(R.string.monday_fasting)
            IslamicCalendarHelper.isThursday(tomorrow) ->
                context.getString(R.string.thursday_fasting)
            else -> return
        }

        notificationHelper.showFastingNotification(title, message)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkWhiteDaysFasting(hijriTomorrow: HijrahDate) {
        if (!preferences.isFastingReminderEnabled || !preferences.isWhiteDaysEnabled) return

        val tomorrowDay = hijriTomorrow.get(ChronoField.DAY_OF_MONTH)

        if (IslamicCalendarHelper.isWhiteDay(tomorrowDay)) {
            val hijriFormatted = IslamicCalendarHelper.formatHijriDate(hijriTomorrow, locale)
            val title = context.getString(R.string.white_days_title)
            val message = context.getString(R.string.white_days_message, tomorrowDay, hijriFormatted)

            notificationHelper.showFastingNotification(title, message)
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkSpecialFastingDays(hijriTomorrow: HijrahDate) {
        if (!preferences.isFastingReminderEnabled) return

        val events = IslamicCalendarHelper.getIslamicEvents(context)
            .filter { it.type == EventType.FASTING }

        events.forEach { event ->
            if (hijriTomorrow.get(ChronoField.MONTH_OF_YEAR) == event.hijriMonth &&
                hijriTomorrow.get(ChronoField.DAY_OF_MONTH) == event.hijriDay) {

                val hijriFormatted = IslamicCalendarHelper.formatHijriDate(hijriTomorrow, locale)
                val title = context.getString(R.string.special_fasting_title, event.nameAr)
                val message = context.getString(R.string.special_fasting_message, event.nameAr, hijriFormatted)

                notificationHelper.showFastingNotification(title, message)
            }
        }
    }
}
