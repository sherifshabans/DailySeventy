package com.elsharif.dailyseventy.domain.dailyazkar


import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun scheduleSunriseReminder(context: Context, hour: Int, minute: Int) {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
    }

    val delay = target.timeInMillis - now.timeInMillis

    val inputData = Data.Builder()
        .putString("type", "sunrise") // نوع خاص للشروق
        .build()

    val workRequest = OneTimeWorkRequestBuilder<AzkarWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "sunrise_azkar_work",
        ExistingWorkPolicy.REPLACE, // أو KEEP حسب مزاجك
        workRequest
    )
}
