package com.elsharif.dailyseventy.util.Permissions


import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // كآخر محاولة، افتح الإعدادات الرئيسية
        try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(settingsIntent)
        } catch (e2: Exception) {
            // لا يمكن فتح الإعدادات
        }
    }
}
