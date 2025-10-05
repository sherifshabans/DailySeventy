package com.elsharif.dailyseventy.domain.islamicReminder

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.friday.NotificationHelper
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.concurrent.TimeUnit

class IslamicReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val type = inputData.getString("type") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()
        val message = inputData.getString("message") ?: return Result.failure()

        // Show notification
        NotificationHelper.showNotification(
            applicationContext,
            title,
            message,
            "islamic_$type"
        )

        // Reschedule for Monday/Thursday or White Days or Eid/Religious Occasions
        when {
            type.startsWith("monday") -> {
                val delay = getNextSundayNightDelay()
                val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        "type" to "monday",
                        "title" to applicationContext.getString(R.string.monday_fasting_reminder),
                        "message" to applicationContext.getString(R.string.monday_fasting_message)
                    ))
                    .build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "${MONDAY_THURSDAY_WORK}_monday",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
            type.startsWith("thursday") -> {
                val delay = getNextWednesdayNightDelay()
                val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        "type" to "thursday",
                        "title" to applicationContext.getString(R.string.thursday_fasting_reminder),
                        "message" to applicationContext.getString(R.string.thursday_fasting_message)
                    ))
                    .build()
                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "${MONDAY_THURSDAY_WORK}_thursday",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
            type.startsWith("white_day_") -> {
                val day = inputData.getInt("hijri_day", 13)
                val delay = calculateNextWhiteDayDelay(day)
                if (delay > 0) {
                    val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(workDataOf(
                            "type" to "white_day_$day",
                            "title" to applicationContext.getString(R.string.white_days_reminder),
                            "message" to applicationContext.getString(R.string.white_days_reminder_message, day),
                            "hijri_day" to day
                        ))
                        .build()
                    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                        "${WHITE_DAYS_WORK}_$day",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
            type in listOf("eid_fitr", "eid_adha") -> {
                val hijriMonth = inputData.getInt("hijri_month", 1)
                val hijriDay = inputData.getInt("hijri_day", 1)
                val delay = calculateHijriDateDelay(hijriMonth, hijriDay, true)
                if (delay > 0) {
                    val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(workDataOf(
                            "type" to type,
                            "title" to applicationContext.getString(
                                if (type == "eid_fitr") R.string.eid_fitr_reminder else R.string.eid_adha_reminder
                            ),
                            "message" to applicationContext.getString(
                                if (type == "eid_fitr") R.string.eid_fitr_reminder_message else R.string.eid_adha_reminder_message
                            ),
                            "hijri_month" to hijriMonth,
                            "hijri_day" to hijriDay
                        ))
                        .build()
                    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                        "${EID_REMINDER_WORK}_$type",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
            type in listOf("day_arafat", "day_ashura", "prophet_birthday", "isra_miraj", "laylat_qadr", "begin_ramadan") -> {
                val hijriMonth = inputData.getInt("hijri_month", 1)
                val hijriDay = inputData.getInt("hijri_day", 1)
                val delay = calculateHijriDateDelay(hijriMonth, hijriDay, true)
                if (delay > 0) {
                    val (titleRes, messageRes) = when (type) {
                        "day_arafat" -> R.string.day_arafat_reminder to R.string.day_arafat_reminder_message
                        "day_ashura" -> R.string.day_ashura_reminder to R.string.day_ashura_reminder_message
                        "prophet_birthday" -> R.string.prophet_birthday_reminder to R.string.prophet_birthday_reminder_message
                        "isra_miraj" -> R.string.isra_miraj_reminder to R.string.isra_miraj_reminder_message
                        "laylat_qadr" -> R.string.laylat_qadr_reminder to R.string.laylat_qadr_reminder_message
                        else -> R.string.begin_ramadan_reminder to R.string.begin_ramadan_reminder_message
                    }
                    val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(workDataOf(
                            "type" to type,
                            "title" to applicationContext.getString(titleRes),
                            "message" to applicationContext.getString(messageRes),
                            "hijri_month" to hijriMonth,
                            "hijri_day" to hijriDay
                        ))
                        .build()
                    WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                        "${RELIGIOUS_OCCASION_WORK}_$type",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
        }

        return Result.success()
    }

    private fun getNextSundayNightDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
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
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis - now
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateNextWhiteDayDelay(day: Int): Long {
        try {
            val today = HijrahDate.now()
            val currentMonth = today.get(ChronoField.MONTH_OF_YEAR)
            val currentYear = today.get(ChronoField.YEAR)

            var targetDate = HijrahDate.of(currentYear, currentMonth, day)
            val gregorianDate = LocalDate.from(targetDate)
            val targetDateTime = gregorianDate.atTime(20, 0)
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
            val finalDateTime = finalGregorianDate.atTime(20, 0)
            return finalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - currentMillis
        } catch (e: Exception) {
            return -1
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            val targetDateTime = gregorianDate.atTime(20, 0)
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
                val finalDateTime = finalGregorianDate.atTime(20, 0)
                return finalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - currentMillis
            }

            return targetMillis - currentMillis
        } catch (e: Exception) {
            return -1
        }
    }

    companion object {
        private const val MONDAY_THURSDAY_WORK = "monday_thursday_reminder"
        private const val WHITE_DAYS_WORK = "white_days_reminder"
        private const val EID_REMINDER_WORK = "eid_reminder"
        private const val RELIGIOUS_OCCASION_WORK = "religious_occasion_reminder"
    }
}