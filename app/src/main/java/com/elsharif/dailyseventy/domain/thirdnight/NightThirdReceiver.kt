package com.elsharif.dailyseventy.domain.thirdnight

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThird

class NightThirdWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val partName = inputData.getString("third_name") ?: return Result.failure()
        val part = NightThird.valueOf(partName) // رجع enum
        val message = applicationContext.getString(part.labelRes)
        val title = applicationContext.getString(R.string.notification_title)

        val discription =  applicationContext.getString(R.string.night_started, message)

        NightThirdNotifier.notify(
            context = applicationContext,
            title = title,
            message =discription
        )


        return Result.success()
    }
}
