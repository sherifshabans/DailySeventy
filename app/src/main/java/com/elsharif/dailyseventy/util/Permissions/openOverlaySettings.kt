package com.elsharif.dailyseventy.util.Permissions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

fun openOverlaySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        openAppSettings(context)
    }
}