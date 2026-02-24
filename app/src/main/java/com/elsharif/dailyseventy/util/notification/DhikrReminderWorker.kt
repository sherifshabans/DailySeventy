package com.elsharif.dailyseventy.util.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.random.Random

@HiltWorker
class DhikrReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params : WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val type  = inputData.getString("type") ?: "tree"
        val index = Random.nextInt(0, 5)

        when (type) {
            "tree"   -> NotificationHelper.showTreeNotification(applicationContext, index)
            "garden" -> NotificationHelper.showGardenNotification(applicationContext, index)
        }

        return Result.success()
    }
}