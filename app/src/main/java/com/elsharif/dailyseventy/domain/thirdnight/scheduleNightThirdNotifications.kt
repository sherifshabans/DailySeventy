package com.elsharif.dailyseventy.domain.thirdnight

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThird
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@SuppressLint("NewApi")
fun scheduleNightThirdNotifications(
    context: Context,
    maghrib: LocalTime,
    fajr: LocalTime,
    selection: Set<NightThird>
) {
    val workManager = WorkManager.getInstance(context)

    workManager.cancelAllWorkByTag("night_third")

    val today = LocalDate.now()
    val maghribDateTime = LocalDateTime.of(today, maghrib)
    val fajrDateTime = LocalDateTime.of(today.plusDays(1), fajr)

    val nightDuration = Duration.between(maghribDateTime, fajrDateTime)
    val thirdDuration = nightDuration.dividedBy(3)

    val parts = listOf(
        NightThird.FIRST to maghribDateTime,
        NightThird.SECOND to maghribDateTime.plus(thirdDuration),
        NightThird.THIRD to maghribDateTime.plus(thirdDuration.multipliedBy(2))
    )

    for ((third, startTime) in parts) {
        if (!selection.contains(third)) continue

        val triggerAt = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val delay = triggerAt - System.currentTimeMillis()
        if (delay <= 0) continue

        val work = OneTimeWorkRequestBuilder<NightThirdWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("third_name" to third.name))
            .addTag("night_third") // ✅ علشان نقدر نلغي
            .build()

        workManager.enqueue(work)
    }
}

fun cancelNightThirdNotifications(context: Context) {
    WorkManager.getInstance(context).cancelAllWorkByTag("night_third")
}
