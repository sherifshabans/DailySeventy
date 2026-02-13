package com.elsharif.dailyseventy.domain.friday

import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar
import android.content.Context

// 📌 سورة الكهف (ثابتة 10 صباحًا)
fun scheduleKahfReminder(context: Context) {


    val workManager = WorkManager.getInstance(context)
    val delayKahf = getNextFridayDelay(10, 0)
    workManager.cancelAllWorkByTag("friday_kahf")

    val kahfWork = PeriodicWorkRequestBuilder<FridayReminderWorker>(7, TimeUnit.DAYS)
        .setInitialDelay(delayKahf, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("type" to "kahf"))
        .build()

    workManager.enqueueUniquePeriodicWork(
        "friday_kahf",
        ExistingPeriodicWorkPolicy.REPLACE,
        kahfWork
    )
}

// 📌 دعاء بعد العصر (مرن حسب وقت العصر)
fun scheduleAsrReminder(context: Context, asrHour: Int, asrMinute: Int) {
    val workManager = WorkManager.getInstance(context)
    var delayAsr = getNextFridayDelay(asrHour, asrMinute)

    workManager.cancelAllWorkByTag("friday_asr")


    delayAsr += 30 * 60 * 1000

    val asrWork = PeriodicWorkRequestBuilder<FridayReminderWorker>(7, TimeUnit.DAYS)
        .setInitialDelay(delayAsr, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("type" to "asr"))
        .build()

    workManager.enqueueUniquePeriodicWork(
        "friday_asr",
        ExistingPeriodicWorkPolicy.REPLACE,
        asrWork
    )
}

// 📌 تحسب المدة لحد الجمعة الجاية
fun getNextFridayDelay(hour: Int, minute: Int): Long {
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }
    return cal.timeInMillis - now
}
