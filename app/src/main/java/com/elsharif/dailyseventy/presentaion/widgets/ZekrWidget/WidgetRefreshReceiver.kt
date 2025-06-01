package com.elsharif.dailyseventy.presentaion.widgets.ZekrWidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// This receiver handles the scheduled update
class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Default).launch {
            ZekerWidget.updateAll(context)
        }

        ZekerWidget.scheduleRefresh(context)

    }
}
