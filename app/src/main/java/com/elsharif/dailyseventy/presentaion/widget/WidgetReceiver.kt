package com.elsharif.dailyseventy.presentaion.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver


class WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = ZekerWidget

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ZekerWidget.scheduleRefresh(context)
    }
}
