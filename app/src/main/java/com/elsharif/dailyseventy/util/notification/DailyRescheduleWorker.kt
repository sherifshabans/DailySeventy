package com.elsharif.dailyseventy.util.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyRescheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params : WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        DhikrNotificationScheduler.scheduleAll(applicationContext)
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyRescheduleWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .addTag("daily_reschedule")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_reschedule",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}