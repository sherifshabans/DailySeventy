package com.elsharif.dailyseventy.presentaion.widgets.HijriDateWidget


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.elsharif.dailyseventy.util.cornerRadiusCompat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter


class MyClockWidget : GlanceAppWidget() {


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val currentHijrahDate = HijrahDate.now()

            val hijriDateFormatted = currentHijrahDate.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(java.util.Locale("ar"))
            )

            Content(hijriDateFormatted)
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun Content(
    hijrahDate: String
) {
    val cornerRadius = 6
    val backgroundAlpha = 0.02f
    val color = Color(0xFFCDDDFF)
    Box(
        modifier = GlanceModifier
            .cornerRadiusCompat(cornerRadius, color, backgroundAlpha)
            .fillMaxSize()
            .padding(12.dp)
    ) {
   // If I need to remove the background remove the box
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadiusCompat(0, color = Color(0xFF294878), 0f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "التاريخ الهجري: $hijrahDate  هـ",
            style = TextStyle(fontSize = 20.sp, color = ColorProvider(Color.White)),
            modifier = GlanceModifier.cornerRadiusCompat(0, color = Color(0xFF294878), 0f)

        )

    }
    }
}
