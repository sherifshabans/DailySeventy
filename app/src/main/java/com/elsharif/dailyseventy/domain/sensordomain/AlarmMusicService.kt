package com.elsharif.dailyseventy.domain.sensordomain

import android.annotation.SuppressLint
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
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.elsharif.dailyseventy.ui.MainActivity
import com.elsharif.dailyseventy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class AlarmMusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()
    private val channelId = "alarm_channel+10"
    private val notificationId = 1025
    private var stopJob: Job? = null

    companion object {
        const val ACTION_STOP = "com.elsharif.dailyseventy.ACTION_STOP_ALARM_MUSIC"
        const val EXTRA_ALARM_TYPE = "EXTRA_ALARM_TYPE"
    }

    inner class MusicBinder : Binder() {
        fun getService(): AlarmMusicService = this@AlarmMusicService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("AlarmMusicService", "Service created")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmMusicService", "Service started")

        // معالجة أمر الإيقاف
        if (intent?.action == ACTION_STOP) {
            stopAlarmMusic()
            stopSelf()
            return START_NOT_STICKY
        }

        // إنشاء الـ notification أولاً
        val notification = createAlarmNotification()
        startForeground(notificationId, notification)

        // تشغيل الموسيقى
        startAlarmMusic()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "منبه القيام/الفجر",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات منبه القيام/الفجر"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
                enableVibration(true)
                enableLights(true)
                setSound(null, null) // بدون صوت في الإشعار
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createAlarmNotification(): Notification {
        // Intent لفتح التطبيق
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("step_alarm", "step_alarm")
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent لإيقاف الموسيقى
        val stopIntent = Intent(this, AlarmMusicService::class.java).apply {
            action = ACTION_STOP
        }

        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("⏰ منبه القيام/الفجر يعمل الآن")
            .setContentText("اضغط لفتح التطبيق وإيقاف المنبه")
            .setSmallIcon(R.drawable.doaa)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setSilent(true) // بدون صوت في الإشعار
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_stop, "إيقاف المنبه", stopPendingIntent)
            .setDeleteIntent(stopPendingIntent) // إيقاف الموسيقى عند حذف الإشعار
            .build()
    }

    fun startAlarmMusic() {
        try {
            if (mediaPlayer == null) {
                // الطريقة الصحيحة لإنشاء Uri من الـ raw resource
                val alarmSoundUri = "android.resource://${packageName}/${R.raw.alarm_song}".toUri()

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, alarmSoundUri)

                    setOnPreparedListener {
                        start()
                        Log.d("AlarmMusicService", "Music started successfully")
                    }

                    setOnCompletionListener {
                        // إعادة التشغيل التلقائي
                        start()
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e("AlarmMusicService", "MediaPlayer error: what=$what, extra=$extra")
                        true
                    }

                    // إعدادات الصوت
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

                    setVolume(1.0f, 1.0f)
                    isLooping = true
                    prepareAsync()
                }
            } else if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
                Log.d("AlarmMusicService", "Music resumed")
            }

            // إيقاف تلقائي بعد 30 دقيقة
            stopJob = CoroutineScope(Dispatchers.Main).launch {
                delay(30 * 60 * 1000)
                stopAlarmMusic()
                stopSelf()
            }

        } catch (e: Exception) {
            Log.e("AlarmMusicService", "Error starting music: ${e.message}", e)
        }
    }
    fun stopAlarmMusic() {
        try {
            stopJob?.cancel()
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
                Log.d("AlarmMusicService", "Music stopped and released")
            }
        } catch (e: Exception) {
            Log.e("AlarmMusicService", "Error stopping music: ${e.message}", e)
        }
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    Log.d("AlarmMusicService", "Music paused")
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmMusicService", "Error pausing music: ${e.message}", e)
        }
    }

    fun resumeMusic() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    Log.d("AlarmMusicService", "Music resumed")
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmMusicService", "Error resuming music: ${e.message}", e)
        }
    }

    fun isMusicPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            Log.e("AlarmMusicService", "Error checking music state: ${e.message}", e)
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmMusic()
        Log.d("AlarmMusicService", "Service destroyed")
    }
}