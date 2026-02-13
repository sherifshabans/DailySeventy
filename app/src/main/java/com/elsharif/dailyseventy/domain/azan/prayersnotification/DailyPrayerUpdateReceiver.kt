package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * 🕛 Receiver لتحديث مواقيت الصلاة كل يوم في منتصف الليل
 * بيشتغل تلقائياً بدون فتح التطبيق
 */
class DailyPrayerUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DailyPrayerUpdate"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "🔔 Daily prayer update triggered at midnight")

        try {
            // 🔄 أعد جدولة الصلوات لليوم الجديد
            AzanPrayersUtil.registerPrayersImmediately(context)

            Log.d(TAG, "✅ Daily prayer alarms updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating daily prayers: ${e.message}", e)
        }
    }
}