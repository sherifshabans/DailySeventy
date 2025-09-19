package com.elsharif.dailyseventy.domain.islamicReminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elsharif.dailyseventy.R
import androidx.core.net.toUri

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID_FASTING = "fasting_reminders"
        const val CHANNEL_ID_GENERAL = "general_reminders"
        const val NOTIFICATION_ID_FASTING = 18001
        const val NOTIFICATION_ID_GENERAL = 18002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fastingChannel = NotificationChannel(
                CHANNEL_ID_FASTING,
                context.getString(R.string.channel_fasting_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_fasting_description)
                setSound(
                    "android.resource://${context.packageName}/${R.raw.fasting_sound}".toUri(),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                context.getString(R.string.channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_general_description)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(fastingChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showFastingNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FASTING)
            .setSmallIcon(R.drawable.crescent_moon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound("android.resource://${context.packageName}${R.raw.fasting_sound}".toUri())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_FASTING, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showGeneralNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.pray)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GENERAL, notification)
    }
}
