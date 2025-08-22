package com.elsharif.dailyseventy.domain.friday

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FridayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type")

        when (type) {
            "kahf" -> {
                NotificationHelper.showNotification(
                    context,
                    "Friday Reminder",
                    "Don’t forget to read Surah Al-Kahf after Dhuhr."
                )
            }
            "asr" -> {
                NotificationHelper.showNotification(
                    context,
                    "Friday Reminder",
                    "Make extra duaa after Asr on Friday."
                )
            }
        }
    }
}
