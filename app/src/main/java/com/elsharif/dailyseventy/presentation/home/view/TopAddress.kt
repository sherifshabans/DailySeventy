// TopAddressSection.kt
package com.elsharif.dailyseventy.presentation.home.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
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

    LaunchedEffect(context) {
        viewModel.refreshAddressForLanguageChange()
    }

    // أو استخدم DisposableEffect للتحديث عند العودة للشاشة
    DisposableEffect(context) {
        viewModel.refreshAddressForLanguageChange()
        onDispose { }
    }
    val state by viewModel.prayerTimesState.collectAsState()
    val currentHijrahDate = HijrahDate.now()
    val addressText by viewModel.addressText.collectAsState()

    val locale = LocalContext.current.resources.configuration.locales[0] // اللغة الحالية للجهاز
    val hijriDateFormatted = currentHijrahDate.format(
        DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(locale)
    )
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    var remainingTime by remember { mutableStateOf("00:00:00") }

    val upcomingPrayer: UiPrayerTime? = remember(state) {
        if (state is PrayerUiState.Success) {
            val prayers = (state as PrayerUiState.Success).prayers
            val now = LocalTime.now()
            prayers.firstOrNull {
                val time = LocalTime.parse(it.time, timeFormatter)
                time.isAfter(now)
            }
        } else null
    }

    LaunchedEffect(upcomingPrayer) {
        if (upcomingPrayer != null) {
            val targetTime = LocalTime.parse(upcomingPrayer.time, timeFormatter)
            while (true) {
                val now = LocalTime.now()
                if (now.isBefore(targetTime)) {
                    val duration = Duration.between(now, targetTime)
                    val hours = duration.toHours()
                    val minutes = (duration.toMinutes() % 60)
                    val seconds = (duration.seconds % 60)
                    remainingTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    remainingTime = "بدأت"
                    break
                }
                delay(1000)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp) // نفس الارتفاع الأصلي
    ) {
        // صورة الخلفية
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.mosque01),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // المحتوى الرئيسي - بدون طبقة إضافية
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // التاريخ الهجري في الأعلى
            Text(
                text = stringResource(R.string.today_date, hijriDateFormatted),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )


            // معلومات الصلاة في الوسط
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is PrayerUiState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.loadingprays),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    is PrayerUiState.Error -> {
                        Text(
                            text = (state as PrayerUiState.Error).message.toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is PrayerUiState.Success -> {
                        if (upcomingPrayer != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(id = upcomingPrayer.iconRes),
                                    contentDescription = upcomingPrayer.name,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(end = 4.dp)
                                )

                                Column {
                                    Text(
                                        text = upcomingPrayer.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.remaining_time, remainingTime),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.notprayupcoming),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // العنوان في الأسفل
            if (addressText.isNotEmpty()) {
                Text(
                    text = addressText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}