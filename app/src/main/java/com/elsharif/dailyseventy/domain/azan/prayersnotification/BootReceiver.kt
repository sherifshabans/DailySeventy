package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, registering prayer times")
            AzanPrayersUtil.registerPrayers(context)
        }
    }
} 