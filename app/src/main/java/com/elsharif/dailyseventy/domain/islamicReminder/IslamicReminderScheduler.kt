package com.elsharif.dailyseventy.domain.islamicReminder

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elsharif.dailyseventy.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
object IslamicReminderScheduler {

    private const val TAG = "IslamicReminder"
    private const val MONDAY_THURSDAY_WORK = "monday_thursday_reminder"
    private const val WHITE_DAYS_WORK = "white_days_reminder"
    private const val EID_REMINDER_WORK = "eid_reminder"
    private const val RELIGIOUS_OCCASION_WORK = "religious_occasion_reminder"
    private const val REMINDER_HOUR = 20 // 8 مساءً
    private const val REMINDER_MINUTE = 0
    private const val REMINDER_HOUR_White = 21 // 9 مساءً
    private const val REMINDER_MINUTE_White = 0

    private const val REMINDER_HOUR_Hijri = 18 // 6 مسا
    private const val REMINDER_MINUTE_Hijri = 0

    fun scheduleMondayThursdayReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val sundayNightDelay = getNextSundayNightDelay()
        Log.d(TAG, "Monday reminder delay: ${sundayNightDelay / (1000 * 60 * 60)} hours")

        if (sundayNightDelay > 0) {
            val mondayReminder = createMondayThursdayWorkRequest(
                delay = sundayNightDelay,
                title = context.getString(R.string.monday_fasting_reminder),
                message = context.getString(R.string.monday_fasting_message),
                type = "monday"
            )

            workManager.enqueueUniqueWork(
                "${MONDAY_THURSDAY_WORK}_monday",
                ExistingWorkPolicy.REPLACE,
                mondayReminder
            )
        }

        val wednesdayNightDelay = getNextWednesdayNightDelay()
        Log.d(TAG, "Thursday reminder delay: ${wednesdayNightDelay / (1000 * 60 * 60)} hours")

        if (wednesdayNightDelay > 0) {
            val thursdayReminder = createMondayThursdayWorkRequest(
                delay = wednesdayNightDelay,
                title = context.getString(R.string.thursday_fasting_reminder),
                message = context.getString(R.string.thursday_fasting_message),
                type = "thursday"
            )

            workManager.enqueueUniqueWork(
                "${MONDAY_THURSDAY_WORK}_thursday",
                ExistingWorkPolicy.REPLACE,
                thursdayReminder
            )
        }
    }

    fun scheduleWhiteDaysReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        for (day in 13..15) {
            val delay = calculateNextWhiteDayDelay(day)

            if (delay > 0) {
                Log.d(TAG, "White day $day delay: ${delay / (1000 * 60 * 60 * 24)} days")

                val whiteDayWork = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        "type" to "white_day_$day",
                        "title" to context.getString(R.string.white_days_reminder),
                        "message" to context.getString(R.string.white_days_reminder_message, day),
                        "hijri_day" to day
                    ))
                    .build()

                workManager.enqueueUniqueWork(
                    "${WHITE_DAYS_WORK}_$day",
                    ExistingWorkPolicy.REPLACE,
                    whiteDayWork
                )
            }
        }
    }

    fun scheduleEidReminder(context: Context, eidType: String, hijriMonth: Int, hijriDay: Int) {
        val delay = calculateHijriDateDelay(hijriMonth, hijriDay, reminderDayBefore = true)

        if (delay <= 0) {
            Log.w(TAG, "Eid $eidType delay is negative or zero, skipping")
            return
        }

        Log.d(TAG, "Eid $eidType delay: ${delay / (1000 * 60 * 60 * 24)} days")

        val workManager = WorkManager.getInstance(context)

        val title = when (eidType) {
            "eid_fitr" -> context.getString(R.string.eid_fitr_reminder)
            "eid_adha" -> context.getString(R.string.eid_adha_reminder)
            else -> context.getString(R.string.eid_reminder)
        }

        val message = when (eidType) {
            "eid_fitr" -> context.getString(R.string.eid_fitr_reminder_message)
            "eid_adha" -> context.getString(R.string.eid_adha_reminder_message)
            else -> context.getString(R.string.eid_reminder_message)
        }

        val eidWork = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "type" to eidType,
                "title" to title,
                "message" to message,
                "hijri_month" to hijriMonth,
                "hijri_day" to hijriDay
            ))
            .build()

        workManager.enqueueUniqueWork(
            "${EID_REMINDER_WORK}_$eidType",
            ExistingWorkPolicy.REPLACE,
            eidWork
        )
    }

    fun scheduleReligiousOccasionReminder(
        context: Context,
        occasionType: String,
        hijriMonth: Int,
        hijriDay: Int,
        titleRes: Int,
        messageRes: Int
    ) {
        val delay = calculateHijriDateDelay(hijriMonth, hijriDay, reminderDayBefore = true)

        if (delay <= 0) {
            Log.w(TAG, "Occasion $occasionType delay is negative or zero, skipping")
            return
        }

        Log.d(TAG, "Occasion $occasionType delay: ${delay / (1000 * 60 * 60 * 24)} days")

        val workManager = WorkManager.getInstance(context)

        val occasionWork = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "type" to occasionType,
                "title" to context.getString(titleRes),
                "message" to context.getString(messageRes),
                "hijri_month" to hijriMonth,
                "hijri_day" to hijriDay
            ))
            .build()

        workManager.enqueueUniqueWork(
            "${RELIGIOUS_OCCASION_WORK}_$occasionType",
            ExistingWorkPolicy.REPLACE,
            occasionWork
        )
    }

    private fun getNextSundayNightDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis - now
    }

    private fun getNextWednesdayNightDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis - now
    }

    private fun calculateNextWhiteDayDelay(day: Int): Long {
        try {
            val today = HijrahDate.now()
            val currentMonth = today.get(ChronoField.MONTH_OF_YEAR)
            val currentYear = today.get(ChronoField.YEAR)

            var targetDate = HijrahDate.of(currentYear, currentMonth, day)
            val gregorianDate = LocalDate.from(targetDate)
            val targetDateTime = gregorianDate.atTime(REMINDER_HOUR_White, REMINDER_MINUTE_White)
            val targetMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentMillis = System.currentTimeMillis()

            if (targetMillis <= currentMillis) {
                targetDate = if (currentMonth == 12) {
                    HijrahDate.of(currentYear + 1, 1, day)
                } else {
                    HijrahDate.of(currentYear, currentMonth + 1, day)
                }
            }

            val finalGregorianDate = LocalDate.from(targetDate)
            val finalDateTime = finalGregorianDate.atTime(REMINDER_HOUR_White, REMINDER_MINUTE_White)
            return finalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - currentMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating white day delay for day $day: ${e.message}", e)
            return -1
        }
    }

    private fun calculateHijriDateDelay(
        hijriMonth: Int,
        hijriDay: Int,
        reminderDayBefore: Boolean = true
    ): Long {
        try {
            val today = HijrahDate.now()
            val currentYear = today.get(ChronoField.YEAR)

            var targetDay = hijriDay
            var targetMonth = hijriMonth
            var targetYear = currentYear

            if (reminderDayBefore) {
                targetDay -= 1
                if (targetDay < 1) {
                    targetMonth -= 1
                    if (targetMonth < 1) {
                        targetMonth = 12
                        targetYear -= 1
                    }
                    val previousMonthLength = HijrahDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                    targetDay = previousMonthLength
                }
            }

            var targetDate = try {
                HijrahDate.of(targetYear, targetMonth, targetDay)
            } catch (e: Exception) {
                val lastDay = HijrahDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                HijrahDate.of(targetYear, targetMonth, lastDay)
            }

            val gregorianDate = LocalDate.from(targetDate)
            val targetDateTime = gregorianDate.atTime(REMINDER_HOUR_Hijri, REMINDER_MINUTE_Hijri)
            val targetMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentMillis = System.currentTimeMillis()

            if (targetMillis <= currentMillis) {
                targetYear = currentYear + 1
                targetDate = try {
                    HijrahDate.of(targetYear, targetMonth, targetDay)
                } catch (e: Exception) {
                    val lastDay = HijrahDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                    HijrahDate.of(targetYear, targetMonth, lastDay)
                }

                val finalGregorianDate = LocalDate.from(targetDate)
                val finalDateTime = finalGregorianDate.atTime(REMINDER_HOUR_Hijri, REMINDER_MINUTE_Hijri)
                return finalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - currentMillis
            }

            return targetMillis - currentMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating Hijri date delay for month $hijriMonth, day $hijriDay: ${e.message}", e)
            return -1
        }
    }

    private fun createMondayThursdayWorkRequest(
        delay: Long,
        title: String,
        message: String,
        type: String
    ): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<IslamicReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "type" to type,
                "title" to title,
                "message" to message
            ))
            .build()
    }

    fun cancelAllReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)

        workManager.cancelUniqueWork("${MONDAY_THURSDAY_WORK}_monday")
        workManager.cancelUniqueWork("${MONDAY_THURSDAY_WORK}_thursday")

        for (day in 13..15) {
            workManager.cancelUniqueWork("${WHITE_DAYS_WORK}_$day")
        }

        workManager.cancelUniqueWork("${EID_REMINDER_WORK}_eid_fitr")
        workManager.cancelUniqueWork("${EID_REMINDER_WORK}_eid_adha")

        listOf(
            "day_arafat", "day_ashura", "prophet_birthday",
            "isra_miraj", "laylat_qadr", "begin_ramadan"
        ).forEach { occasion ->
            workManager.cancelUniqueWork("${RELIGIOUS_OCCASION_WORK}_$occasion")
        }

        Log.d(TAG, "All reminders cancelled")
    }

    fun cancelSpecificReminder(context: Context, reminderType: String) {
        val workManager = WorkManager.getInstance(context)

        when (reminderType) {
            "monday_thursday" -> {
                workManager.cancelUniqueWork("${MONDAY_THURSDAY_WORK}_monday")
                workManager.cancelUniqueWork("${MONDAY_THURSDAY_WORK}_thursday")
            }
            "white_days" -> {
                for (day in 13..15) {
                    workManager.cancelUniqueWork("${WHITE_DAYS_WORK}_$day")
                }
            }
            "eid" -> {
                workManager.cancelUniqueWork("${EID_REMINDER_WORK}_eid_fitr")
                workManager.cancelUniqueWork("${EID_REMINDER_WORK}_eid_adha")
            }
            "religious" -> {
                listOf(
                    "day_arafat", "day_ashura", "prophet_birthday",
                    "isra_miraj", "laylat_qadr", "begin_ramadan"
                ).forEach { occasion ->
                    workManager.cancelUniqueWork("${RELIGIOUS_OCCASION_WORK}_$occasion")
                }
            }
        }

        Log.d(TAG, "Cancelled $reminderType reminders")
    }
}