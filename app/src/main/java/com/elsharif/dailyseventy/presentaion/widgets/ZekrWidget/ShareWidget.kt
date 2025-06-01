package com.elsharif.dailyseventy.presentaion.widgets.ZekrWidget

import android.content.Context
import android.content.Intent
import androidx.glance.action.ActionParameters
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback

class ShareActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val zikr = parameters[ZekerWidget.zikrKey] ?: return

        val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
        val shareText = "$zikr\n\nبواسطة تطبيق $appName"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val chooser = Intent.createChooser(intent, "Share via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(chooser)
    }
}
