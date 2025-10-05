package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.elsharif.dailyseventy.MainActivity
import com.elsharif.dailyseventy.R

class PreAndPostAzanAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID_PRE = "PRE_AZAN_CHANNEL"
        private const val CHANNEL_NAME_PRE = "Pre-Azan Reminder Channel"

        private const val CHANNEL_ID_POST = "POST_AZAN_CHANNEL"
        private const val CHANNEL_NAME_POST = "Post-Azan Reminder Channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("TYPE") ?: "PRE" // PRE or POST
        val prayerName = intent.getStringExtra("PRAYER") ?: ""
        val content = intent.getStringExtra("CONTENT") ?: ""
        val title = intent.getStringExtra("TITLE") ?: "تنبيه الصلاة"
        val icon = intent.getIntExtra("ICON", R.drawable.doaa)
        val id = intent.getIntExtra("ID", 5555)

        val openMainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, id, openMainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sound: Uri = if (type == "PRE") {
            ("android.resource://${context.packageName}/${R.raw.preazan}").toUri()
        } else {
            ("android.resource://${context.packageName}/${R.raw.postazan}").toUri()
        }

        val reminderText = if (type == "PRE") {
            content
        } else {
            context.getString(R.string.after_prayer_reminder, prayerName)
        }
        sendNotification(
            context,
            icon,
            title,
   reminderText,
            sound,
            pendingIntent,
            id,
            type
        )

    }

    private fun sendNotification(
        context: Context,
        iconId: Int,
        title: String,
        content: String,
        sound: Uri,
        pendingIntent: PendingIntent,
        id: Int,
        type: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(manager, sound, type)

        val channelId = if (type == "PRE") CHANNEL_ID_PRE else CHANNEL_ID_POST

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, iconId))
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(sound, AudioManager.STREAM_NOTIFICATION)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // 🔧 استخدام كل الـ defaults

        }

        manager.notify(id, builder.build())
    }

    private fun createNotificationChannel(manager: NotificationManager, sound: Uri, type: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val (channelId, channelName) = if (type == "PRE") {
                CHANNEL_ID_PRE to CHANNEL_NAME_PRE
            } else {
                CHANNEL_ID_POST to CHANNEL_NAME_POST
            }

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(sound, audioAttributes)
                enableVibration(true)
                description = if (type == "PRE") {
                    "${R.string.prayer_is_in_10_minutes}"
                } else {
                    "${R.string.after_prayer_reminder}"
                }
            }

            manager.createNotificationChannel(channel)
        }
    }
}
