package com.elsharif.dailyseventy.domain.friday

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elsharif.dailyseventy.R

class FridayReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val type = inputData.getString("type") ?: return Result.failure()

        when (type) {
            "kahf" -> {
                NotificationHelper.showNotification(
                    applicationContext,
                    applicationContext.getString(R.string.friday_reminders),
                    applicationContext.getString(R.string.kahf_reminder),
                    "kahf"
                )
            }
            "asr" -> {
                NotificationHelper.showNotification(
                    applicationContext,
                    applicationContext.getString(R.string.friday_reminders),
                    applicationContext.getString(R.string.asr_time_reminder),
                    "asr"
                )
            }
        }

        return Result.success()
    }
}
