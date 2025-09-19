// PrayerTimesSection.kt
package com.elsharif.dailyseventy.presentation.home.view

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeListItem
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState

@SuppressLint("NewApi", "UnusedBoxWithConstraintsScope")
@Composable
fun PrayerTimesSection(viewModel: PrayerTimeViewModel = hiltViewModel()) {

    val state by viewModel.prayerTimesState.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    var iconState by remember { mutableStateOf(Icons.Rounded.KeyboardArrowUp) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable {
                            isVisible = !isVisible
                            iconState = if (isVisible) Icons.Rounded.KeyboardArrowUp
                            else Icons.Rounded.KeyboardArrowDown
                        }
                ) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        imageVector = iconState,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = stringResource(R.string.prayertimes),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )

            if (isVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.weight(0.5f),
                            text = stringResource(R.string.pray),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            modifier = Modifier.weight(0.5f),
                            text = stringResource(R.string.praytime),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            maxLines = 1
                        )
                        Text(
                            modifier = Modifier.weight(0.5f),
                            text = stringResource(R.string.remainingtime),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (state) {
                        is PrayerUiState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "نستعد لمواقيت الصلاة… 🌙",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
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
                            val prayers = (state as PrayerUiState.Success).prayers
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                            val now = remember { mutableStateOf(java.time.LocalTime.now()) }

                            // تحديث الوقت كل ثانية
                            LaunchedEffect(Unit) {
                                while (true) {
                                    now.value = java.time.LocalTime.now()
                                    kotlinx.coroutines.delay(1000)
                                }
                            }

                            // حساب الصلاة القادمة بناءً على الوقت الحالي
                            val nextPrayer = remember(prayers, now.value) {
                                prayers.minByOrNull { prayer ->
                                    val time = runCatching { java.time.LocalTime.parse(prayer.time, formatter) }.getOrNull()
                                    if (time != null) {
                                        val duration = java.time.Duration.between(now.value, time)
                                        if (!duration.isNegative) duration else java.time.Duration.ofDays(1)
                                    } else java.time.Duration.ofDays(1)
                                }
                            }

                            LazyColumn {
                                items(prayers) { prayer ->
                                    PrayerTimeListItem(
                                        uiPrayerTime = prayer,
                                        isNextPrayer = prayer == nextPrayer
                                    )                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
