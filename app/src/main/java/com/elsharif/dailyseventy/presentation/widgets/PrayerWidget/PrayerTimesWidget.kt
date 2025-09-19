package com.elsharif.dailyseventy.presentation.widgets.PrayerWidget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.di.HijriFormatterEntryPoint
import com.elsharif.dailyseventy.di.PrayerTimesEntryPoint
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.ui.theme.Black
import com.elsharif.dailyseventy.ui.theme.White
import com.elsharif.dailyseventy.util.getTodayExact
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


private const val TAG = "PrayerTimesWidget"

class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PrayerTimesWidget
}

object PrayerTimesWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {


        provideContent {

            val isDarkSystem = AppPreferences(context).isDarkModeEnabled()
            val prayerTiming by getPrayerTimes(context).collectAsState(initial = listOf())
            var prayerTimingRemembered by remember { mutableStateOf(listOf<UiPrayerTime>()) }

            val hijriFormat = EntryPointAccessors
                .fromApplication(context.applicationContext, HijriFormatterEntryPoint::class.java)
                .hijriFormatter()

        /*    val currentHijrahDate = HijrahDate.now()

            val hijriFormat = currentHijrahDate.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(java.util.Locale("ar"))
            )
        */    val todayExact = getTodayExact()
            LaunchedEffect(prayerTiming) {
                if (prayerTiming.isNotEmpty()) prayerTimingRemembered = prayerTiming
            }
            Box(
                modifier = GlanceModifier.background(if (isDarkSystem) Black else White),
                contentAlignment = Alignment.TopCenter
            ) {


                Column(
                    modifier = GlanceModifier.fillMaxSize() ,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = if (prayerTiming.isNotEmpty()) Alignment.Vertical.Top else Alignment.Vertical.CenterVertically,

                    ) {
                    if (prayerTiming.isNotEmpty()) {

                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Vertical.CenterVertically
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.leftcorner),
                                    contentDescription = null
                                )
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Image(
                                    provider = ImageProvider(R.drawable.rightcorner),
                                    contentDescription = null
                                )
                            }
                            Column(verticalAlignment = Alignment.Vertical.CenterVertically) {
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Text(
                                    text = hijriFormat.format(todayExact),
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(
                                            if (!isDarkSystem) Black else White
                                        )
                                    ),
                                    modifier = GlanceModifier.padding(top = 8.dp)
                                )
                                Spacer(modifier = GlanceModifier.defaultWeight())
                            }
                        }

                        Column(
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                            horizontalAlignment = Alignment.Horizontal.Start
                        ) {
                            prayerTiming.forEach {
                                PrayerTimeListItem(context = context, isDarkSystem, uiPrayerTime = it)
                            }
                        }
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
        }

    }

}

@RequiresApi(Build.VERSION_CODES.O)
private fun getPrayerTimes(context: Context): Flow<List<UiPrayerTime>> {
    val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PrayerTimesEntryPoint::class.java
    )

    val getUserLocationUseCase = entryPoint.getUserLocationUseCase()
    val getCurrentPrayerTimesAuthorityUseCase = entryPoint.getCurrentPrayerTimesAuthorityUseCase()
    val getPrayerTimesUseCase = entryPoint.getPrayerTimesUseCase()

    return combine(
        getUserLocationUseCase(),
        getCurrentPrayerTimesAuthorityUseCase()
    ) { location, auth ->


        getPrayerTimesUseCase(location.first, location.second, LocalDate.now(), auth).onEach {
            AppPreferences(context).updatePrayerTimes(it)
        }.first()

        AppPreferences(context).getPrayerTimes().map {
            Log.d(TAG, "getPrayerTimes: Local $it")
            it.filter { it.date == LocalDate.now().toString() }
                .also { Log.d(TAG, "getPrayerTimes: ${it.size}") }.map {
                val imgId = context.resources.getIdentifier(
                    it.prayer.imageId, "drawable",
                    context.packageName
                )

                val nameId = context.resources.getIdentifier(
                    it.prayer.name, "string",
                    context.packageName
                )

                val extractedTime = parseTime(it.time)
                val currentTime = LocalTime.now()
                var remainingDuration = Duration.between(
                    currentTime,
                    LocalTime.parse(extractedTime, DateTimeFormatter.ofPattern("hh:mm a"))
                )
                if (remainingDuration.isNegative) remainingDuration = Duration.ZERO

                val hours = remainingDuration.toHours()
                val minutes = remainingDuration.minusHours(hours).toMinutes()
                val seconds = remainingDuration.minusHours(hours).minusMinutes(minutes).seconds

                val formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds)


                UiPrayerTime(imgId, context.getString(nameId), extractedTime, formattedDuration)
            }
        }.first()
    }
}


@SuppressLint("RestrictedApi")
@Composable
private fun PrayerTimeListItem(context: Context, isDarkSystem: Boolean, uiPrayerTime: UiPrayerTime) {
    Column(horizontalAlignment = Alignment.Horizontal.End) {
        Row(
            horizontalAlignment = Alignment.Horizontal.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            val d: Drawable? = AppCompatResources.getDrawable(context, uiPrayerTime.iconRes)



            Text(
                text = uiPrayerTime.time,
                modifier = GlanceModifier.padding(horizontal = 2.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = ColorProvider(if (!isDarkSystem) Black else White)
                )
            )

            Text(
                text = uiPrayerTime.name,
                modifier = GlanceModifier.defaultWeight().padding(horizontal = 2.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    color = ColorProvider(if (!isDarkSystem) Black else White)
                ),
            )

            Image(
                provider = ImageProvider(d!!.toBitmap()), contentDescription = uiPrayerTime.name,
                modifier = GlanceModifier.padding(horizontal = 4.dp)
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        Row(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(MaterialTheme .colorScheme.primary).padding(4.dp)) {}
    }

}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseTime(inputTime: String): String {
    // Extract time from input string
    val timeStartIndex = inputTime.indexOf(':') - 2
    val timeEndIndex = inputTime.indexOf('(') - 1
    val extractedTime = inputTime.substring(timeStartIndex, timeEndIndex)

    // Format time
    val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
    val time = LocalTime.parse(extractedTime, inputFormat)

    val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
    return outputFormat.format(time)

}
