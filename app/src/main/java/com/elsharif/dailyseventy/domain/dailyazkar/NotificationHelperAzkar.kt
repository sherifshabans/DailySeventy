package com.elsharif.dailyseventy.domain.dailyazkar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.elsharif.dailyseventy.ui.MainActivity
import com.elsharif.dailyseventy.R

object NotificationHelperAzkar {


    fun showNotification(
        context: Context,
        title: String,
        message: String,
        category: String,
    ) {
        val channelId = "azkar_channel_$category"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val soundUri =  if (title=="وقت الشروق🌞"){
            "android.resource://${context.packageName}/${R.raw.wackup}".toUri()
        }
        else  "android.resource://${context.packageName}/${R.raw.ayaelkorsy}".toUri()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                category,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("category", category)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            category.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.doaa)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri) // للأجهزة أقل من أندرويد O
            .build()

        notificationManager.notify(category.hashCode(), notification)
    }
}
