package com.elsharif.dailyseventy.domain.zekr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.elsharif.dailyseventy.R

object ZekrNotificationHelper {

    private const val CHANNEL_ID = "ZEKR_CHANNEL"
    private const val CHANNEL_NAME = "Zekr Reminder Channel"
    private const val NOTIFICATION_ID = 1001

    fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val zekrSound: Uri =
            ("android.resource://${context.packageName}/${R.raw.tazker}").toUri()

        createChannel(manager, zekrSound)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.doaa)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.doaa))
            .setContentTitle("وقت الصلاة على النبي ﷺ")
            .setContentText("المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم، فإنّ المحبّ لا يغفل عن حبيبه...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(zekrSound, AudioManager.STREAM_NOTIFICATION)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم، فإنّ المحبّ لا يغفل عن حبيبه...")
            )
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(manager: NotificationManager, sound: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(sound, audioAttributes)
                enableVibration(true)
                enableLights(true)
            }

            manager.createNotificationChannel(channel)
        }
    }
}