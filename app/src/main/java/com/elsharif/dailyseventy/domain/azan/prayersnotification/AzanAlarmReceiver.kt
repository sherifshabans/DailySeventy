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
import com.elsharif.dailyseventy.ui.MainActivity
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.sharedpreferences.AzanSoundPrefs

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
        // ⬇️ نجيب الصوت اللي اختاره المستخدم
        val selectedSoundResId = AzanSoundPrefs.loadSelectedSound(context)
        val azanSound: Uri = "android.resource://${context.packageName}/$selectedSoundResId".toUri()


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
        createNotificationChannel(  manager, context, sound)
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

        // 🟢 هنا نربط الـ channelId بالصوت المختار
        val channelId = getAzanChannelId(context, sound)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
        Log.d(TAG, "createNotificationBuilder: $title $content ${sound.path}")

        notificationBuilder
            .setSmallIcon(iconId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, iconId))
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setSound(sound) // لأجهزة أقل من أندرويد O
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setColorized(true)

        return notificationBuilder
    }


    private fun createNotificationChannel(manager: NotificationManager, context: Context, sound: Uri): String {
        val channelId = getAzanChannelId(context, sound)
        val channelName = "Azan Channel - ${sound.lastPathSegment}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // لو القناة لسه مش موجودة اعملها
            if (manager.getNotificationChannel(channelId) == null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()

                val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.setSound(sound, audioAttributes)
                notificationChannel.enableVibration(true)
                notificationChannel.enableLights(true)

                manager.createNotificationChannel(notificationChannel)
            }
        }
        return channelId
    }

    private fun getAzanChannelId(context: Context, sound: Uri): String {
        return "AZAN_CHANNEL_${sound.lastPathSegment}"
    }


    companion object {
        private const val CHANNEL_ID = "AZAN_CHANNEL"
        private const val CHANNEL_NAME = "azan channel"
    }
}
