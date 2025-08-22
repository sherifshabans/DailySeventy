package com.elsharif.dailyseventy.presentaion.widgets.ZekrWidget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentaion.home.view.remembrances
import com.elsharif.dailyseventy.util.cornerRadiusCompat

object ZekerWidget : GlanceAppWidget() {


    // Must be SizeMode.Exact in order to get accurate widget size in LocalSize.current
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {

        Content(context)

        }

    }
    val zikrKey = ActionParameters.Key<String>("zikr")




    private fun getCurrentZikr(): String {
        val currentTime = System.currentTimeMillis()
        val index = ((currentTime / (5 * 1000)) % remembrances.size).toInt()
        return remembrances[index]
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun Content(context: Context) {
        val zikr = getCurrentZikr()

        val cornerRadius = 6
        val backgroundAlpha = 0.01f
        val color = Color(0xFFCDDDFF)

        Box(
            modifier = GlanceModifier
                .cornerRadiusCompat(cornerRadius, color, backgroundAlpha)
                .fillMaxSize()
                .padding(12.dp)
        ) {


            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadiusCompat(cornerRadius, color = Color(0xFF294878), backgroundAlpha = 1f)
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
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.padding(4.dp)
                )

                Box(
                    modifier = GlanceModifier
                        .cornerRadiusCompat(0, color = Color(0xFF294878), 0f)
                        .width(40.dp).height(40.dp)
                        .background(Color((0xFF294878))),
                    // Set a fixed size for alignment

                    contentAlignment = Alignment.BottomCenter // Center the content inside
                ) {

                    SquareIconButton(
                        modifier = GlanceModifier
                            .width(30.dp).height(30.dp)
                            .background(Color((0xFF294878))),
                        contentDescription = "",
                        onClick = actionRunCallback<ShareActionCallback>(
                            parameters = actionParametersOf(
                                zikrKey to zikr)
                        ),
                        imageProvider =ImageProvider(R.drawable.rounded_inbox_text_share_24),
                        // drawable resource only
                       backgroundColor = ColorProvider(Color((0xFF294878))),
                       contentColor =ColorProvider(Color(0xFFBFC5CE)),
                        )

                }


            }
        }
    }

    // In CounterWidget
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleRefresh(context: Context) {
        val intent = Intent(context, WidgetRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 5_000, // 30 seconds from now
            pendingIntent
        )
    }



}


