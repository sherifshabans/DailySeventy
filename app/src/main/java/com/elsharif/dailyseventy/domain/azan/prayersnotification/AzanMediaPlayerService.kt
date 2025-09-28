package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.core.app.NotificationCompat
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.sharedpreferences.AzanSoundPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AzanMediaPlayerService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var notificationManager: NotificationManager? = null
    private var stopJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 9999
        private const val CHANNEL_ID = "AZAN_MEDIA_PLAYER_CHANNEL"
        const val ACTION_STOP = "ACTION_STOP_AZAN"
        const val EXTRA_SOUND_URI = "EXTRA_SOUND_URI"
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_PRAYER_TYPE = "EXTRA_PRAYER_TYPE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopAzan()
                stopSelf()
            }
            else -> {
                val soundUri = intent?.getParcelableExtra<Uri>(EXTRA_SOUND_URI)
                val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الأذان"
                val prayerType = intent?.getStringExtra(EXTRA_PRAYER_TYPE) ?: "MAIN"

                if (soundUri != null) {
                    startAzan(soundUri, prayerName, prayerType)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startAzan(soundUri: Uri, prayerName: String, prayerType: String) {
        stopAzan() // تأكد من إيقاف أي تشغيل سابق

        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(applicationContext, soundUri)
            setOnPreparedListener {
                start()
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

        // إيقاف تلقائي بعد 5 دقائق كحد أقصى (للأمان)
        stopJob = CoroutineScope(Dispatchers.Main).launch {
            delay(5 * 60 * 1000) // 5 دقائق
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
        }
        mediaPlayer = null
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun showPlayingNotification(prayerName: String, prayerType: String) {
        val stopIntent = Intent(this, AzanMediaPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("تشغيل الأذان")
            .setContentText("جارٍ تشغيل أذان $prayerName")
            .setSmallIcon(R.drawable.doaa)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true) // بدون صوت في الإشعار
            .addAction(R.drawable.ic_stop, "إيقاف", pendingStopIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "خدمة تشغيل الأذان",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "خدمة تشغيل صوت الأذان"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAzan()
        super.onDestroy()
    }
}