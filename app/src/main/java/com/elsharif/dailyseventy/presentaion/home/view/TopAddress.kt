package com.elsharif.dailyseventy.presentaion.home.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentaion.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentaion.prayertimes.model.UiPrayerTime
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
@Composable
fun TopAddressSection(viewModel: PrayerTimeViewModel = hiltViewModel()) {

    val prayerTimes by viewModel.prayerTimesFlow.collectAsState(listOf())
    val currentHijrahDate = HijrahDate.now()

    val hijriDateFormatted = currentHijrahDate.format(
        DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(Locale("ar"))
    )

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    // Track remaining time as state
    var remainingTime by remember { mutableStateOf("") }

    // Find next upcoming prayer dynamically
    val upcomingPrayer: UiPrayerTime? = remember(prayerTimes) {
        val now = LocalTime.now()
        prayerTimes.firstOrNull {
            val time = LocalTime.parse(it.time, timeFormatter)
            time.isAfter(now)
        }
    }

    // Update countdown every second
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
            .height(100.dp)
    ) {
        // Background image
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.mosque01),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hijri Date
            Text(
                textAlign = TextAlign.Center,
                text = "اليوم: $hijriDateFormatted هـ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Upcoming Prayer
            if (upcomingPrayer != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Prayer Icon
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = upcomingPrayer.iconRes),
                        contentDescription = upcomingPrayer.name,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
                    )

                    Column {
                        Text(
                            text = upcomingPrayer.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "متبقي: $remainingTime",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                Text(
                    text = "لا توجد صلاة قادمة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
