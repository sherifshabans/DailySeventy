package com.elsharif.dailyseventy.domain.azan.prayersnotification

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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.elsharif.dailyseventy.MainActivity
import com.elsharif.dailyseventy.R

class AzanAlarmReceiver : BroadcastReceiver() {

    private val TAG = "AzanAlarmReceiver"
    override fun onReceive(context: Context, intent: Intent) {

        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val content = intent.getStringExtra("CONTENT")
        val title = intent.getStringExtra("TITLE")
        val icon = intent.getIntExtra("ICON", R.drawable.doaa)
        val id = intent.getIntExtra("ID",0)

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context, id, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        val azanSound = ("android.resource://" + context.packageName + "/" + R.raw.azan).toUri()
        sendNotification(context, icon, title, content, azanSound, pendingIntent)

    }

    private fun sendNotification(
        context: Context,
        iconId: Int,
        title: String?,
        content: String?,
        sound: Uri,
        pendingIntent: PendingIntent
    ) {
        val manager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder =
            createNotificationBuilder(context, iconId, title, content, sound, pendingIntent)
        createNotificationChannel(manager, sound)
        manager.notify(0, notificationBuilder.build())
    }

    private fun createNotificationBuilder(
        context: Context,
        iconId: Int,
        title: String?,
        content: String?,
        sound: Uri,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
        Log.d(TAG, "createNotificationBuilder: $title $content ${sound.path}")
        notificationBuilder
            .setSmallIcon(iconId)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    iconId
                )
            ).setContentTitle(title)
            .setSound(sound)
          //  .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE) // Keep other defaults except sound

            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setColorized(true)

        return notificationBuilder
    }

    private fun createNotificationChannel(manager: NotificationManager, sound: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
            notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
            notificationChannel.setSound(sound, audioAttributes)
            notificationChannel.enableVibration(true) // Optional: enable vibration
            notificationChannel.enableLights(true) // Optional: enable LED lights

            manager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "AZAN_CHANNEL"
        private const val CHANNEL_NAME = "azan channel"
    }
}
