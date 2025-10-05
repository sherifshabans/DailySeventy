package com.elsharif.dailyseventy.presentation.widgets.PrayerWidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import androidx.glance.layout.size
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
import com.elsharif.dailyseventy.util.cornerRadiusCompat
import com.elsharif.dailyseventy.util.getTodayExact
import com.elsharif.dailyseventy.util.updatePrayerTimesWidget
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter

private const val TAG = "PrayerTimesWidget"

class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerTimesWidget()

    // معالجة تغيير اللغة بطريقة محسّنة
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_LOCALE_CHANGED -> {
                Log.d(TAG, "Locale changed - updating widget")
                // استخدام الدالة الجديدة لتحديث الويدجت
                kotlinx.coroutines.GlobalScope.launch {
                    context.updatePrayerTimesWidget()
                }
            }
        }
    }
}

// دالة extension لتحديث ويدجت أوقات الصلاة

class PrayerTimesWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // إعادة تحديث الويدجت عند تغيير اللغة
        val currentLocale = context.resources.configuration.locales[0].toString()
        Log.d(TAG, "Widget locale: $currentLocale")

        provideContent {
            val isDarkSystem = AppPreferences(context).isDarkModeEnabled()
            val prayerTiming by getPrayerTimes(context).collectAsState(initial = listOf())
            var prayerTimingRemembered by remember { mutableStateOf(listOf<UiPrayerTime>()) }

            val hijriFormat = EntryPointAccessors
                .fromApplication(context.applicationContext, HijriFormatterEntryPoint::class.java)
                .hijriFormatter()

            val currentHijrahDate = HijrahDate.now()

            val locale = context.resources.configuration.locales[0]
            val hijriDateFormatted = currentHijrahDate.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(locale)
            )
            val todayExact = getTodayExact()

            val cornerRadius = 6
            val backgroundAlpha = 0.01f

            // تحديث البيانات عند تغيير اللغة
            LaunchedEffect(currentLocale, prayerTiming) {
                if (prayerTiming.isNotEmpty()) prayerTimingRemembered = prayerTiming
            }

            Box(
                modifier = GlanceModifier
                    .cornerRadiusCompat(cornerRadius, color = White, backgroundAlpha = 1f),

                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = if (prayerTiming.isNotEmpty()) Alignment.Vertical.Top else Alignment.Vertical.CenterVertically,
                ) {
                    if (prayerTiming.isNotEmpty()) {
                        // Header with Hijri date
                        Box(
                            modifier = GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Vertical.CenterVertically
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.rightcorner),
                                    contentDescription = null,
                                )
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Image(
                                    provider = ImageProvider(R.drawable.leftcorner),
                                    contentDescription = null,
                                )
                            }
                            Column(verticalAlignment = Alignment.Vertical.CenterVertically) {
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Text(
                                    text = hijriDateFormatted,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(
                                            Black
                                        )
                                    ),
                                    modifier = GlanceModifier.padding(top = 8.dp)
                                )
                                Spacer(modifier = GlanceModifier.defaultWeight())
                            }
                        }

                        // Prayer times list
                        Column(
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                            horizontalAlignment = Alignment.Horizontal.Start
                        ) {
                            prayerTiming.forEach { prayer ->
                                PrayerTimeListItem(
                                    context = context,
                                    isDarkSystem = isDarkSystem,
                                    uiPrayerTime = prayer
                                )
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
    val preferences = AppPreferences(context)

    return flow {
        try {
            val location = getUserLocationUseCase().first()
            val auth = getCurrentPrayerTimesAuthorityUseCase().first()

            // محاولة جلب البيانات من API
            try {
                val apiData = getPrayerTimesUseCase(location.first, location.second, LocalDate.now(), auth).first()
                preferences.updatePrayerTimes(apiData)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch from API, using cached data: ${e.message}")
            }

            // جلب البيانات المحفوظة محلياً
            val cachedPrayers = preferences.getPrayerTimes().first()
            Log.d(TAG, "getPrayerTimes: Cached prayers count = ${cachedPrayers.size}")

            val todaysPrayers = cachedPrayers
                .filter { it.date == LocalDate.now().toString() }
                .also { Log.d(TAG, "getPrayerTimes: Today's prayers count = ${it.size}") }
                .mapNotNull { prayerData ->
                    try {
                        // استخدام نفس منطق getPrayerNameResource و getPrayerImageResource من ViewModel
                        val cleanName = if (prayerData.prayer.name.contains(":string/")) {
                            prayerData.prayer.name.substringAfter(":string/")
                        } else {
                            prayerData.prayer.name
                        }

                        val nameRes = getPrayerNameResource(context, cleanName)
                        val imgRes = getPrayerImageResource(context, prayerData.prayer.imageId, cleanName)
                        val extractedTime = parseTime(prayerData.time)

                        // حساب الوقت المتبقي
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

                        UiPrayerTime(
                            iconRes = imgRes,
                            name = context.getString(nameRes), // هنا بيتم استخدام اللغة الحالية
                            time = extractedTime,
                            remainingTime = formattedDuration
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing prayer ${prayerData.prayer.name}: ${e.message}")
                        null
                    }
                }
                .sortedBy { prayer ->
                    // ترتيب الصلوات حسب الوقت
                    try {
                        LocalTime.parse(prayer.time, DateTimeFormatter.ofPattern("hh:mm a"))
                    } catch (e: Exception) {
                        LocalTime.MIDNIGHT
                    }
                }

            emit(todaysPrayers)

        } catch (e: Exception) {
            Log.e(TAG, "Error in getPrayerTimes: ${e.message}")
            emit(emptyList())
        }
    }
}

// دالة مساعدة لتحويل أسماء الصلوات إلى Resource IDs (نفس منطق ViewModel)
private fun getPrayerNameResource(context: Context, prayerName: String): Int = when (prayerName.lowercase()) {
    "fajr" -> R.string.fajr
    "sunrise" -> R.string.sun_rise
    "dhuhr" -> R.string.dhuhr
    "asr" -> R.string.asr
    "maghrib" -> R.string.maghrib
    "isha" -> R.string.isha
    "imsak" -> R.string.imsak
    "sunset" -> R.string.sunset
    "midnight" -> R.string.midnight
    else -> R.string.fajr // قيمة افتراضية
}

// دالة مساعدة لتحويل أسماء الصور إلى Resource IDs مع معالجة أفضل
private fun getPrayerImageResource(context: Context, imageName: String, prayerName: String): Int {
    // محاولة استخدام imageName المرسل
    val imgId = try {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    } catch (e: Exception) {
        0
    }

    if (imgId != 0) return imgId

    // إذا فشل، استخدم أيقونات ثابتة حسب اسم الصلاة
    return when (prayerName.lowercase()) {
        "fajr" -> R.drawable.fajr
        "sunrise" -> R.drawable.duha
        "dhuhr" -> R.drawable.dhuhr
        "asr" -> R.drawable.asr
        "maghrib" -> R.drawable.maghrib
        "isha" -> R.drawable.isha
        "imsak" -> R.drawable.fajr  // نفس أيقونة الفجر
        "sunset" -> R.drawable.maghrib  // نفس أيقونة المغرب
        "midnight" -> R.drawable.isha  // نفس أيقونة العشاء
        else -> R.drawable.fajr // أيقونة صغيرة افتراضية
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun PrayerTimeListItem(
    context: Context,
    isDarkSystem: Boolean,
    uiPrayerTime: UiPrayerTime
) {
    Column(horizontalAlignment = Alignment.Horizontal.End) {
        Row(
            horizontalAlignment = Alignment.Horizontal.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            // الوقت
            Text(
                text = uiPrayerTime.time,
                modifier = GlanceModifier.padding(horizontal = 2.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start,
                    color = ColorProvider( Black )
                )
            )

            // اسم الصلاة
            Text(
                text = uiPrayerTime.name,
                modifier = GlanceModifier.defaultWeight().padding(horizontal = 2.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    color = ColorProvider( Black )
                ),
            )

            // أيقونة الصلاة - مع تحديد الحجم
            Image(
                provider = ImageProvider(uiPrayerTime.iconRes),
                contentDescription = uiPrayerTime.name,
                modifier = GlanceModifier
                    .padding(horizontal = 4.dp)
                    .size(24.dp) // تحديد حجم ثابت للأيقونة
            )
        }

        Spacer(GlanceModifier.height(4.dp))

        // خط فاصل
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GlanceTheme.colors.primary)
                .padding(4.dp)
        ) {}
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseTime(inputTime: String): String {
    return try {
        // استخراج الوقت من النص
        val timeStartIndex = inputTime.indexOf(':') - 2
        val timeEndIndex = inputTime.indexOf('(') - 1
        val extractedTime = inputTime.substring(timeStartIndex, timeEndIndex)

        // تحويل الصيغة
        val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
        val time = LocalTime.parse(extractedTime, inputFormat)

        val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
        outputFormat.format(time)
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing time: $inputTime, ${e.message}")
        "12:00 PM" // قيمة افتراضية
    }
}