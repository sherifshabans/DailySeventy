package com.elsharif.dailyseventy.presentation.home.view

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.components.PrayerSkyBackground
import com.elsharif.dailyseventy.presentation.components.PrayerSkyType
import com.elsharif.dailyseventy.presentation.components.prayerNameToSkyType
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi", "LocalContextConfigurationRead")
@Composable
fun TopAddressSection(
    viewModel: PrayerTimeViewModel,
) {
    val context = LocalContext.current

    LaunchedEffect(context) { viewModel.refreshAddressForLanguageChange() }
    DisposableEffect(context) { viewModel.refreshAddressForLanguageChange(); onDispose { } }

    val state       by viewModel.prayerTimesState.collectAsState()
    val addressText by viewModel.addressText.collectAsState()

    val locale = LocalContext.current.resources.configuration.locales[0]
    val hijriDateFormatted = HijrahDate.now().format(
        DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(locale)
    )
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    var remainingTime by remember { mutableStateOf("00:00:00") }

    val upcomingPrayer: UiPrayerTime? = remember(state) {
        if (state is PrayerUiState.Success) {
            val prayers = (state as PrayerUiState.Success).prayers
            val now = LocalTime.now()
            prayers.firstOrNull { LocalTime.parse(it.time, timeFormatter).isAfter(now) }
        } else null
    }

    // ── تحديد نوع السماء من الصلاة القادمة ──────────────────────────────────
    val skyType by remember(upcomingPrayer) {
        derivedStateOf {
            upcomingPrayer?.name?.let { prayerNameToSkyType(it) }
                ?: run {
                    // لو مفيش صلاة قادمة، نحدد من الوقت الحالي
                    val h = LocalTime.now().hour
                    when {
                        h < 5  -> PrayerSkyType.ISHA
                        h < 7  -> PrayerSkyType.FAJR
                        h < 9  -> PrayerSkyType.SUNRISE
                        h < 13 -> PrayerSkyType.DHUHR
                        h < 16 -> PrayerSkyType.ASR
                        h < 19 -> PrayerSkyType.MAGHRIB
                        else   -> PrayerSkyType.ISHA
                    }
                }
        }
    }

    // ── عداد الوقت المتبقي ────────────────────────────────────────────────────
    LaunchedEffect(upcomingPrayer) {
        if (upcomingPrayer != null) {
            val targetTime = LocalTime.parse(upcomingPrayer.time, timeFormatter)
            while (true) {
                val now = LocalTime.now()
                if (now.isBefore(targetTime)) {
                    val d = Duration.between(now, targetTime)
                    remainingTime = String.format(
                        "%02d:%02d:%02d", d.toHours(), d.toMinutes() % 60, d.seconds % 60
                    )
                } else { remainingTime = "بدأت"; break }
                delay(1000)
            }
        }
    }

    // ── الكارد الرئيسي ────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        // ✅ السماء الديناميكية بدل الصورة الثابتة
        PrayerSkyBackground(
            skyType  = skyType,
            modifier = Modifier.matchParentSize()
        )

        // المحتوى فوق السماء
        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── التاريخ الهجري ────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.today_date, hijriDateFormatted),
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White.copy(alpha = 0.90f),
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )

            // ── معلومات الصلاة القادمة ────────────────────────────────────────
            Box(
                modifier        = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is PrayerUiState.Loading -> {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                strokeWidth = 2.dp,
                                modifier    = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = stringResource(R.string.loadingprays),
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium,
                                fontSize   = 13.sp
                            )
                        }
                    }

                    is PrayerUiState.Error -> {
                        Text(
                            text  = (state as PrayerUiState.Error).message.toString(),
                            color = Color(0xFFFF8A80)
                        )
                    }

                    is PrayerUiState.Success -> {
                        AnimatedContent(
                            targetState   = upcomingPrayer,
                            transitionSpec = {
                                fadeIn(tween(600)) togetherWith fadeOut(tween(400))
                            },
                            label = "prayer"
                        ) { prayer ->
                            if (prayer != null) {
                                PrayerInfoRow(
                                    prayer        = prayer,
                                    remainingTime = remainingTime,
                                    skyType       = skyType
                                )
                            } else {
                                Text(
                                    text       = stringResource(R.string.notprayupcoming),
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── الموقع ─────────────────────────────────────────────────────────
            if (addressText.isNotEmpty()) {
                Text(
                    text       = addressText,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.White.copy(alpha = 0.70f),
                    textAlign  = TextAlign.End,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  صف معلومات الصلاة
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrayerInfoRow(
    prayer        : UiPrayerTime,
    remainingTime : String,
    skyType       : PrayerSkyType
) {
    val accentColor = when (skyType) {
        PrayerSkyType.FAJR    -> Color(0xFFCE93D8)
        PrayerSkyType.SUNRISE -> Color(0xFFFFD54F)
        PrayerSkyType.DHUHR   -> Color(0xFF80D8FF)
        PrayerSkyType.ASR     -> Color(0xFFFFCC02)
        PrayerSkyType.MAGHRIB -> Color(0xFFFF8A65)
        PrayerSkyType.ISHA    -> Color(0xFFB0BEC5)
    }

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier              = Modifier.fillMaxWidth()
    ) {
        // أيقونة الصلاة
        androidx.compose.foundation.Image(
            painter            = androidx.compose.ui.res.painterResource(id = prayer.iconRes),
            contentDescription = prayer.name,
            modifier           = Modifier.size(38.dp).padding(end = 6.dp)
        )

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text       = prayer.name,
                fontSize   = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White
            )
            Text(
                text       = stringResource(R.string.remaining_time, remainingTime),
                fontSize   = 12.sp,
                color      = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}