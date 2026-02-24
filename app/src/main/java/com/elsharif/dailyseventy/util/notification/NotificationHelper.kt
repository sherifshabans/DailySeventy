package com.elsharif.dailyseventy.util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elsharif.dailyseventy.MainActivity
import com.elsharif.dailyseventy.R

object NotificationHelper {

    private const val TREE_CHANNEL_ID   = "tree_dhikr_channel"
    private const val GARDEN_CHANNEL_ID = "garden_dhikr_channel"

    private const val TRAVEL_CHANNEL_ID = "travel_mode_channel"

    const val TRAVEL_NOTIF_ID = 2001

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(
                NotificationChannel(
                    TREE_CHANNEL_ID,
                    "شجرة الذكر",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "تذكيرات لسقي شجرتك بالذكر" }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    GARDEN_CHANNEL_ID,
                    "روضة الذاكرين",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "تذكيرات للعناية بروضتك" }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    TRAVEL_CHANNEL_ID,
                    "وضع السفر",
                    NotificationManager.IMPORTANCE_LOW  // LOW عشان ميعملش صوت
                ).apply {
                    description = "إشعار ثابت أثناء وضع السفر"
                    setShowBadge(false)
                }
            )
        }
    }

    fun showTreeNotification(context: Context, messageIndex: Int) {
        val messages = listOf(
            Triple("🌱 شجرتك تنتظرك!", "اسقِها بالذكر الآن — سبحان الله", "tree"),
            Triple("🌿 هل نسيت شجرتك؟", "الحمد لله تكبر شجرتك وتورق", "tree"),
            Triple("🌳 شجرتك تشتاق!", "لا إله إلا الله — اجعلها تزهر اليوم", "tree"),
            Triple("✨ وقت الذكر!", "اسقِ شجرتك بالاستغفار والتسبيح", "tree"),
            Triple("🌸 شجرتك في انتظارك", "الله أكبر — كل كلمة تُنبت غصناً جديداً", "tree"),
        )

        val (title, body, route) = messages[messageIndex % messages.size]
        showNotification(context, TREE_CHANNEL_ID, 1001, title, body, route)
    }

    fun showGardenNotification(context: Context, messageIndex: Int) {
        val messages = listOf(
            Triple("🌺 روضتك تحتاجك!", "اذكر الله وازرع وردةً جديدة الآن", "garden"),
            Triple("🌻 ورد الذاكرين!", "أكمل وردك اليوم واحصد النور", "garden"),
            Triple("🌹 حان وقت الحصاد!", "ذكرك ينمو — تعال واحصد ثمار روضتك", "garden"),
            Triple("🌿 اسقِ روضتك بالذكر", "سبحان الله وبحمده — كل تسبيحة تُنبت زهرة", "garden"),
            Triple("📿 وردك في انتظارك!", "لا تنقطع — روضتك تزهر بذكر الله", "garden"),
        )

        val (title, body, route) = messages[messageIndex % messages.size]
        showNotification(context, GARDEN_CHANNEL_ID, 1002, title, body, route)
    }

    private fun showNotification(
        context  : Context,
        channelId: String,
        notifId  : Int,
        title    : String,
        body     : String,
        route    : String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", route)
        }

        val pi = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(
            if(channelId ==TREE_CHANNEL_ID)
                R.drawable.tree
            else R.drawable.garden
            ) // غيّرها لأيقونتك
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notifId, notif)
    }
    fun showTravelNotification(context: Context, destination: String, distance: Int) {
        val emoji = if (distance < 1000) "🚂" else "✈️"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "travel")
        }

        val pi = PendingIntent.getActivity(
            context, TRAVEL_NOTIF_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, TRAVEL_CHANNEL_ID)
            .setSmallIcon(R.drawable.travel)
            .setContentTitle("$emoji وضع السفر نشط")
            .setContentText("إلى $destination · $distance كم · يجوز القصر والجمع")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("إلى $destination · $distance كم\n✂️ القصر · 🔗 الجمع · 🌙 الفطر")
            )
            .setContentIntent(pi)
            .setOngoing(true)          // ✅ ثابت — المستخدم ميقدرش يمسحه
            .setSilent(true)           // ✅ بدون صوت أو اهتزاز
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(TRAVEL_NOTIF_ID, notif)
    }

    fun cancelTravelNotification(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(TRAVEL_NOTIF_ID)
    }


}