package com.elsharif.dailyseventy.domain.zekr

import android.annotation.SuppressLint
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

class ZekrAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val content = intent.getStringExtra("CONTENT")
        val title = intent.getStringExtra("TITLE")
        val icon = intent.getIntExtra("ICON", R.mipmap.ic_launcher)
        val id = intent.getIntExtra("ID", 1)

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context, id, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val zekrSound = ("android.resource://${context.packageName}/${R.raw.tazker}")
            .toUri()

        sendNotification(context, icon, title, content, zekrSound, pendingIntent,id)
    }

    private fun sendNotification(
        context: Context,
        iconId: Int,
        title: String?,
        content: String?,
        sound: Uri,
        pendingIntent: PendingIntent,
        id:Int,
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(manager, sound)
        val notificationBuilder = createNotificationBuilder(context, iconId, title, content, sound, pendingIntent)
        manager.notify(id, notificationBuilder.build())
    }

    private fun createNotificationBuilder(
        context: Context,
        iconId: Int,
        title: String?,
        content: String?,
        sound: Uri,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, iconId))
            .setContentTitle(title)
            .setContentText(content)
            .setSound(sound)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    @SuppressLint("WrongConstant")
    private fun createNotificationChannel(manager: NotificationManager, sound: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MAX
            )
            channel.setSound(sound, audioAttributes)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "ZEKR_CHANNEL"
        private const val CHANNEL_NAME = "Zekr Reminder Channel"
    }
}
