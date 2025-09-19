package com.elsharif.dailyseventy.domain.islamicReminder

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

class IslamicReminderManager(private val context: Context) {
    companion object {
        const val WORK_NAME = "islamic_reminder_work"
    }

    fun setupPeriodicReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<IslamicReminderWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWork
            )
    }

    private fun calculateInitialDelay(): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21) // 8 صباحاً
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return calendar.timeInMillis - now
    }

    fun cancelReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    // 🔹 تشغيل الووركر فورًا (للتست)
    fun runReminderNow() {
        val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // 🔹 تشغيل الووركر بتاريخ هجري مخصص (للتست)
    fun runReminderWithHijriDay(day: Int) {
        val inputData = workDataOf("testHijriDay" to day)
        val workRequest = OneTimeWorkRequestBuilder<IslamicReminderWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}