package com.elsharif.dailyseventy.domain.sensordomain

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.elsharif.dailyseventy.ui.MainActivity
import com.elsharif.dailyseventy.R

class AlarmMusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()
    private val channelId = "alarm_channel+10"
    private val notificationId = 1025

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

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("⏰ منبه القيام/الفجر يعمل الآن")
            .setContentText("اضغط لفتح التطبيق وإيقاف المنبه")
            .setSmallIcon(R.drawable.doaa)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    fun startAlarmMusic() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.alarm_song)?.apply {
                    isLooping = true

                    // ضبط الصوت ليكون عالي ولا يتأثر بوضع الصامت
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                    }

                    setVolume(1.0f, 1.0f)
                    start()
                    Log.d("AlarmMusicService", "Music started successfully")
                }
            } else if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
                Log.d("AlarmMusicService", "Music resumed")
            }
        } catch (e: Exception) {
            Log.e("AlarmMusicService", "Error starting music: ${e.message}", e)
        }
    }

    fun stopAlarmMusic() {
        try {
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