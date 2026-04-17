package com.elsharif.dailyseventy.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CounterWidget : GlanceAppWidget() {

    val countKey = intPreferencesKey("count")

    private val remembrances = listOf(
        "المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم، فإنّ المحبّ لا يغفل عن حبيبه...",
        "سبحان الله",
        "الحمد لله",
        "لا إله إلا الله",
        "الله أكبر",
        "أستغفر الله",
        "اللهم صل وسلم على نبينا محمد",
        "لا حول ولا قوة إلا بالله",
        "سبحان الله وبحمده، سبحان الله العظيم",
        "اللهم اغفر لي",
        "اللهم اجعلني من التوابين"
    )

    private fun getCurrentZikr(): String {
        val currentTime = System.currentTimeMillis()
        val index = ((currentTime / (1 * 30 * 1000)) % remembrances.size).toInt()
        return remembrances[index]
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
    //    val count = currentState(key = countKey) ?: 0
        val zikr = getCurrentZikr()

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp) // Padding from system edge
                .background( ColorProvider(Color.Transparent)) // Transparent outer box
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        color = (Color(0xFF294878)))
                    .cornerRadius(24.dp) // This works for inner feel
                    .padding(12.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
            Text(
                text = zikr,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(MaterialTheme.colorScheme.onPrimary),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    ),
                modifier = GlanceModifier.padding(4.dp)
            )
// Row: Reset | Counter | Inc
/*
            Row(
                modifier = GlanceModifier
                    .padding(8.dp)
                    .wrapContentSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                // Reset Button
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color(0xFFD32F2F))) // red
                        .padding(8.dp)
                        .clickable(actionRunCallback(ResetActionCallback::class.java))
                        .wrapContentSize()
                        .cornerRadius(10.dp)
                ) {
                    Text(
                        text = "تصفير",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp
                        )
                    )
                }

                // Counter
                Text(
                    text = count.toString(),
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(Color.White),
                        fontSize = 26.sp
                    ),
                    modifier = GlanceModifier.padding(horizontal = 12.dp)
                )

                // Inc Button
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color(0xFFAAC7FF))) // green
                        .padding(8.dp)
                        .clickable(actionRunCallback(IncrementActionCallback::class.java))
                        .wrapContentSize()
                        .cornerRadius(10.dp)
                ) {
                    Text(
                        text = "تسبيح",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp
                        )
                    )
                }
            }
*/

        }

        }

    }

    // Schedule periodic updates
    @SuppressLint("ShortAlarm")
    fun scheduleRefresh(context: Context) {
        val intent = Intent(context, WidgetRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            1 * 30 * 1000, // Every 1 minutes
            pendingIntent
        )
    }
}

class SimpleCounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = CounterWidget

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        CounterWidget.scheduleRefresh(context)
    }
}

class IncrementActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentCount = prefs[CounterWidget.countKey]
            prefs[CounterWidget.countKey] = (currentCount ?: 0) + 1
        }
        CounterWidget.update(context, glanceId)
    }
}

class ResetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CounterWidget.countKey] = 0
        }
        CounterWidget.update(context, glanceId)
    }
}
// This receiver handles the scheduled update
class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Default).launch {
            CounterWidget.updateAll(context)
        }
    }
}
