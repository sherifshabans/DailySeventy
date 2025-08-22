package com.elsharif.dailyseventy.domain.preAzan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.elsharif.dailyseventy.MainActivity
import com.elsharif.dailyseventy.R

class PreAzanAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "PRE_AZAN_CHANNEL"
        private const val CHANNEL_NAME = "Pre-Azan Reminder Channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("CONTENT") ?: ""
        val title = intent.getStringExtra("TITLE") ?: "تنبيه الصلاة"
        val icon = intent.getIntExtra("ICON", R.mipmap.ic_launcher)
        val id = intent.getIntExtra("ID", 5555)

        val openMainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, id, openMainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val reminderSound: Uri =
            ("android.resource://${context.packageName}/${R.raw.preazan}").toUri()

        sendNotification(context, icon, title, content, reminderSound, pendingIntent, id)
    }

    private fun sendNotification(
        context: Context,
        iconId: Int,
        title: String,
        content: String,
        sound: Uri,
        pendingIntent: PendingIntent,
        id: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(manager, sound)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, iconId))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(sound)
        }

        manager.notify(id, builder.build())
    }

    private fun createNotificationChannel(manager: NotificationManager, sound: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(sound, audioAttributes)
                enableVibration(true)
                description = "Reminders 10 minutes before Azan"
            }

            manager.createNotificationChannel(channel)
        }
    }
}
