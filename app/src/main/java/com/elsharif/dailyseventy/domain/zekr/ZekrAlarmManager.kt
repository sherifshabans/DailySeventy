package com.elsharif.dailyseventy.domain.zekr

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.elsharif.dailyseventy.domain.data.preferences.ZekrPrefs

class ZekrAlarmManager : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // ✅ تحقق من التفعيل
        if (!ZekrPrefs.isEnabled(context)) {
            return
        }

        // ✅ أرسل الإشعار مباشرة
        ZekrNotificationHelper.showNotification(context)

        // ✅ جدول المنبه التالي
        scheduleNext(context)
    }

    companion object {
        private const val REQUEST_CODE = 9876
        private const val TAG = "ZekrAlarmManager"

        /**
         * جدول المنبه التالي بدقة عالية
         */
        fun scheduleNext(context: Context) {
            if (!ZekrPrefs.isEnabled(context)) {
                Log.d(TAG, "Zekr disabled, not scheduling")
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intervalMinutes = ZekrPrefs.getInterval(context)
            val triggerTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L)

            val intent = Intent(context, ZekrAlarmManager::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // ✅ استخدام setExactAndAllowWhileIdle للدقة العالية
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for ${intervalMinutes}min (Doze-compatible)")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for ${intervalMinutes}min")
            }
        }

        /**
         * إلغاء المنبه
         */
        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ZekrAlarmManager::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Zekr alarm cancelled")
        }
    }
}