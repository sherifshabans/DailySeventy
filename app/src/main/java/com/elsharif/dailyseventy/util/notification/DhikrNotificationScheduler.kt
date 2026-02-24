package com.elsharif.dailyseventy.util.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object DhikrNotificationScheduler {

    // أوقات الإشعارات بالساعة (يختار عشوائياً من كل نطاق)
    private val TREE_WINDOWS   = listOf(8..10, 14..16, 20..21)
    private val GARDEN_WINDOWS = listOf(9..11, 15..17, 21..22)

    fun scheduleAll(context: Context) {
        scheduleForType(context, "tree",   TREE_WINDOWS)
        scheduleForType(context, "garden", GARDEN_WINDOWS)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("dhikr_reminder")
    }

    private fun scheduleForType(
        context : Context,
        type    : String,
        windows : List<IntRange>
    ) {
        val wm = WorkManager.getInstance(context)

        windows.forEachIndexed { i, window ->
            val targetHour   = window.random()          // ساعة عشوائية من النطاق
            val targetMinute = Random.nextInt(0, 60)    // دقيقة عشوائية

            val nowMillis    = System.currentTimeMillis()
            val calendar     = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, targetHour)
                set(java.util.Calendar.MINUTE, targetMinute)
                set(java.util.Calendar.SECOND, 0)
                // لو الوقت عدا اليوم → بكرة
                if (timeInMillis <= nowMillis) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            val delayMs = calendar.timeInMillis - nowMillis

            val request = OneTimeWorkRequestBuilder<DhikrReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("type" to type))
                .addTag("dhikr_reminder")
                .addTag("${type}_${i}")
                .build()

            wm.enqueueUniqueWork(
                "${type}_reminder_$i",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    // إعادة الجدولة كل يوم (تُستدعى بعد تنفيذ كل إشعار)
    fun rescheduleDaily(context: Context) {
        scheduleAll(context)
    }
}