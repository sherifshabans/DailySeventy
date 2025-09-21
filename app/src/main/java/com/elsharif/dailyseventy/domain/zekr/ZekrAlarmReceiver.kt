package com.elsharif.dailyseventy.domain.zekr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elsharif.dailyseventy.R

class ZekrWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("TITLE") ?: "تذكير"
        val content = inputData.getString("CONTENT") ?: "اذكر الله"
        val iconResId = inputData.getInt("ICON", R.drawable.doaa)
        val id = inputData.getInt("ID", 1001)

        val zekrSound: Uri =
            ("android.resource://${applicationContext.packageName}/${R.raw.tazker}").toUri()

        showNotification(title, content, iconResId, id, zekrSound)
        return Result.success()
    }

    private fun showNotification(title: String, content: String, iconResId: Int, id: Int, sound: Uri) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(manager, sound)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, iconResId))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(sound, AudioManager.STREAM_NOTIFICATION) // 🔧 إضافة stream type
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 🔧 استخدام كل الـ defaults

            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    private fun createChannel(manager: NotificationManager, sound: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // 🔧 إضافة content type
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(sound, audioAttributes)
                enableVibration(true)
            }

            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "ZEKR_CHANNEL"
        private const val CHANNEL_NAME = "Zekr Reminder Channel"
    }
}
