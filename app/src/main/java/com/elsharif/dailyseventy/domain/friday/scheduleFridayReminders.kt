package com.elsharif.dailyseventy.domain.friday

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

@SuppressLint("ServiceCast")
fun scheduleFridayReminders(
    context: Context,
    duhrHour: Int,
    duhrMinute: Int,
    asrHour: Int,
    asrMinute: Int
) {


    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    // 1️⃣ Reminder for Surah Al-Kahf (Friday after Duhr)
    val kahfIntent = Intent(context, FridayReceiver::class.java).apply {
        putExtra("type", "kahf")
    }
    val kahfPendingIntent = PendingIntent.getBroadcast(
        context, 1001, kahfIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val fridayDuhr = getNextFridayTime(duhrHour, duhrMinute)
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        fridayDuhr,
        AlarmManager.INTERVAL_DAY * 7, // every Friday
        kahfPendingIntent
    )

    // 2️⃣ Reminder after Asr time (Friday)
    val asrIntent = Intent(context, FridayReceiver::class.java).apply {
        putExtra("type", "asr")
    }
    val asrPendingIntent = PendingIntent.getBroadcast(
        context, 1002, asrIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val fridayAsr = getNextFridayTime(asrHour, asrMinute)
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        fridayAsr,
        AlarmManager.INTERVAL_DAY * 7,
        asrPendingIntent
    )
}

// Utility: calculate next Friday at given hour & minute
fun getNextFridayTime(hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }
    return cal.timeInMillis
}
