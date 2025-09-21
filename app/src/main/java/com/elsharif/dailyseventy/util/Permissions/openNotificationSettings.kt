package com.elsharif.dailyseventy.util.Permissions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

fun openNotificationSettings(context: Context) {
    try {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // إذا فشل، افتح الإعدادات العامة للتطبيق
        openAppSettings(context)
    }
}