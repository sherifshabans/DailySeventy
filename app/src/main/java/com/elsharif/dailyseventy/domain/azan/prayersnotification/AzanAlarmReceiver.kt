package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.elsharif.dailyseventy.MainActivity
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.AzanSoundPrefs

class AzanAlarmReceiver : BroadcastReceiver() {

    private val TAG = "AzanAlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("CONTENT")
        val title = intent.getStringExtra("TITLE")
        val icon = intent.getIntExtra("ICON", R.drawable.doaa)
        val id = intent.getIntExtra("ID", 0)
        val prayerName = intent.getStringExtra("PRAYER") ?: ""
        val type = intent.getStringExtra("TYPE") ?: "MAIN"

        // ⬇️ نجيب الصوت اللي اختاره المستخدم
        val selectedSoundResId = AzanSoundPrefs.loadSelectedSound(context)
        val azanSound: Uri = "android.resource://${context.packageName}/$selectedSoundResId".toUri()

        // بدء خدمة تشغيل الوسائط
        startMediaPlayerService(context, azanSound, prayerName, type)

        // عرض الإشعار بدون صوت
        sendNotification(context, icon, title, content, prayerName, id)
    }

    private fun startMediaPlayerService(context: Context, soundUri: Uri, prayerName: String, type: String) {
        val serviceIntent = Intent(context, AzanMediaPlayerService::class.java).apply {
            putExtra(AzanMediaPlayerService.EXTRA_SOUND_URI, soundUri)
            putExtra(AzanMediaPlayerService.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AzanMediaPlayerService.EXTRA_PRAYER_TYPE, type)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun sendNotification(
        context: Context,
        iconId: Int,
        title: String?,
        content: String?,
        prayerName: String,
        notificationId: Int
    ) {
        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, notificationId, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(context, notificationId, i, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val stopIntent = Intent(context, AzanMediaPlayerService::class.java).apply {
            action = AzanMediaPlayerService.ACTION_STOP
        }
        val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(context, notificationId + 1000, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, notificationId + 1000, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = createSilentNotificationChannel(manager, context)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, iconId))
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSilent(true) // بدون صوت في الإشعار
            .addAction(R.drawable.ic_stop, "إيقاف الأذان", pendingStopIntent)

        manager.notify(notificationId, notificationBuilder.build())
    }

    private fun createSilentNotificationChannel(manager: NotificationManager, context: Context): String {
        val channelId = "AZAN_SILENT_CHANNEL"
        val channelName = "تنبيهات الأذان الصامتة"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.setSound(null, null) // بدون صوت
                notificationChannel.enableVibration(true)
                notificationChannel.enableLights(true)
                manager.createNotificationChannel(notificationChannel)
            }
        }
        return channelId
    }
}