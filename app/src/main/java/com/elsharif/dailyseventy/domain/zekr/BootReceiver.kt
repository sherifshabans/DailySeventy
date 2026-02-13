package com.elsharif.dailyseventy.domain.zekr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.elsharif.dailyseventy.domain.data.preferences.ZekrPrefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling zekr alarm")

            // ✅ إعادة جدولة المنبه إذا كان مفعل
            if (ZekrPrefs.isEnabled(context)) {
                ZekrAlarmManager.scheduleNext(context)
            }
        }
    }
}