package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.net.toUri
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.preferences.AlarmPreferences
import com.elsharif.dailyseventy.domain.data.preferences.AzanSoundPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class AzanMediaPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var notificationManager: android.app.NotificationManager? = null
    private var stopJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 9999
        private const val CHANNEL_ID = "AZAN_MEDIA_PLAYER_CHANNEL"
        const val ACTION_STOP = "ACTION_STOP_AZAN"
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_SOUND_URI = "EXTRA_SOUND_URI"
        const val EXTRA_PRAYER_TYPE = "EXTRA_PRAYER_TYPE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopAzan()
                stopSelf()
            }
            else -> {
                val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الأذان"
                val prayerType = intent?.getStringExtra(EXTRA_PRAYER_TYPE) ?: "MAIN"

                // 🔍 التشيك على نوع الصلاة
                val isFajr = isFajrPrayer(prayerName) || isFajrPrayerByType(prayerType)

                // 🎵 اختيار الصوت المناسب
                val selectedSoundResId = AzanSoundPrefs.getSoundForPrayer(applicationContext, isFajr)
                val soundUri: Uri = "android.resource://${packageName}/$selectedSoundResId".toUri()

                startAzan(soundUri, prayerName, prayerType)
            }
        }
        return START_NOT_STICKY
    }



    private fun startAzan(soundUri: Uri, prayerName: String, prayerType: String) {
        stopAzan()

        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            setDataSource(applicationContext, soundUri)
            setOnPreparedListener {
                start()
                // حفظ حالة التشغيل
                AlarmPreferences.setAlarmMusicPlaying(applicationContext, true)
                Log.d("AlarmMusicService", "Music started successfully")
                showPlayingNotification(prayerName, prayerType)
            }
            setOnCompletionListener {
                stopAzan()
                stopSelf()
            }
            setOnErrorListener { _, _, _ ->
                stopAzan()
                stopSelf()
                true
            }
            prepareAsync()
        }

        stopJob = CoroutineScope(Dispatchers.Main).launch {
            delay(5 * 60 * 1000)
            stopAzan()
            stopSelf()
        }
    }

    private fun stopAzan() {
        stopJob?.cancel()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            // تحديث حالة التوقيف
            AlarmPreferences.setAlarmMusicPlaying(applicationContext, false)
            Log.d("AlarmMusicService", "Music stopped and released")

        }
        mediaPlayer = null
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun showPlayingNotification(prayerName: String, prayerType: String) {
        val stopIntent = Intent(this, AzanMediaPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.PendingIntent.getService(this, 0, stopIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE)
        } else {
            android.app.PendingIntent.getService(this, 0, stopIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // إضافة emoji حسب نوع الصلاة
        val icon = if (isFajrPrayer(prayerName) || isFajrPrayerByType(prayerType)) "🌅" else "🕌"

        val notification = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$icon تشغيل الأذان")
            .setContentText("جارٍ تشغيل أذان $prayerName")
            .setSmallIcon(R.drawable.doaa)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true)
            .addAction(R.drawable.ic_stop, "إيقاف", pendingStopIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "خدمة تشغيل الأذان",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "خدمة تشغيل صوت الأذان"
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAzan()
        super.onDestroy()
    }
}