package com.elsharif.dailyseventy.domain.zekr

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ZekkrAlarmUtil {

    fun setRepeatingZekkrNotification(
        context: Context,
        title: String,
        content: String,
        iconResId: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        /*val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }*/

        val intent = Intent(context, ZekrAlarmReceiver::class.java).apply {
            putExtra("TITLE", title)
            putExtra("CONTENT", content)
            putExtra("ICON", iconResId)
            putExtra("ID", 1001)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val startTimeMillis = System.currentTimeMillis() // ⏰ start now
        val intervalMillis = 15 * 60 * 1000L // 5 minutes

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTimeMillis,
            intervalMillis,
            pendingIntent
        )
    }

    fun cancelZekkrNotification(context: Context) {
        val intent = Intent(context, ZekrAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
