package com.elsharif.dailyseventy.domain.friday

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elsharif.dailyseventy.R
import androidx.core.net.toUri

object NotificationHelper {
    @SuppressLint("ServiceCast")
    fun showNotification(context: Context, title: String, message: String, type: String) {
        // Unique channel per type
        val channelId = "friday_channel_$type"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // اختر الصوت حسب النوع
        val soundUri: Uri = when (type) {
            "kahf" -> "android.resource://${context.packageName}/${R.raw.kahfgomaa}".toUri()
            "asr" -> "android.resource://${context.packageName}/${R.raw.doaagomaa}".toUri()
            else -> "android.resource://${context.packageName}/${R.raw.doaagomaa}".toUri() // fallback
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Friday Reminders ($type)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // 🔧 إضافة content type
                        .build()
                )
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.doaa)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri, AudioManager.STREAM_NOTIFICATION) // 🔧 إضافة stream type
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 🔧 استخدام كل الـ defaults

            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
