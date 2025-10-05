package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import com.elsharif.dailyseventy.domain.data.preferences.AzanSoundPrefs

fun updateAzanChannel(context: Context): String {

    val selectedSoundResId = AzanSoundPrefs.loadSelectedSound(context)
    val azanSound: Uri = "android.resource://${context.packageName}/$selectedSoundResId".toUri()

    // 🟢 خلي لكل صوت قناة خاصة
    val channelId = "AZAN_CHANNEL_$selectedSoundResId"
    val channelName = "Azan Channel - $selectedSoundResId"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // لو القناة لسه مش موجودة، اعملها
        if (manager.getNotificationChannel(channelId) == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(azanSound, audioAttributes)
                enableVibration(true)
                enableLights(true)
            }

            manager.createNotificationChannel(channel)
        }
    }

    return channelId
}

