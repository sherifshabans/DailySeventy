package com.elsharif.dailyseventy.presentaion.hijriCalendar

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentaion.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.Locale

//You will need these dependencies for Hijri Date classes
//implementation group: 'com.github.msarhan', name: 'ummalqura-calendar', version: '1.1.9'
//implementation group: 'net.time4j', name: 'time4j-android', version: '4.8-2021a'

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HijriCalendar(
    navController: NavController,
    selectedDate: HijrahDate,
    daysColor: Color = MaterialTheme.colorScheme.primary,
    selectedDayTextColor: Color = MaterialTheme.colorScheme.primary,
    onDateSelected: (HijrahDate) -> Unit,
){
    val currentHijrahDate = HijrahDate.now()
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var firstDayOfMonth by remember {
        mutableStateOf(
            currentDate.with(
                ChronoField.DAY_OF_MONTH,
                1
            )
        )
    }
    var currentDay= currentHijrahDate.get(ChronoField.DAY_OF_MONTH)
    var currentMonth= currentHijrahDate.get(ChronoField.MONTH_OF_YEAR)
    var currentYear= currentHijrahDate.get(ChronoField.YEAR)

    var hijriCalendar by remember { mutableStateOf(HijrahDate.from(firstDayOfMonth)) }
    var selectedDay by remember { mutableIntStateOf(currentHijrahDate.get(ChronoField.DAY_OF_MONTH)) }
    var selectedMonth by remember { mutableIntStateOf(currentHijrahDate.get(ChronoField.MONTH_OF_YEAR)) }
    var selectedYear by remember { mutableIntStateOf(currentHijrahDate.get(ChronoField.YEAR)) }

    val hijriDateFormatted = currentHijrahDate.format(
        DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(Locale("ar"))
    )

    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.Hijri.route, navController =navController ) }
    ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(16.dp))
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.secondary),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    // Title Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Month Chooser
                        YearPicker(
                            value = selectedYear,
                            onValueChange = { selectedYear = it.toInt() },
                            modifier = Modifier.weight(.5f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    currentDate = currentDate.minusMonths(1)
                                    firstDayOfMonth = currentDate.with(ChronoField.DAY_OF_MONTH, 1)
                                    hijriCalendar = HijrahDate.from(firstDayOfMonth)
                                    selectedMonth = hijriCalendar.get(ChronoField.MONTH_OF_YEAR)
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = hijriCalendar.format(DateTimeFormatter.ofPattern("MMMM")),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = {
                                    currentDate = currentDate.plusMonths(1)
                                    firstDayOfMonth = currentDate.with(ChronoField.DAY_OF_MONTH, 1)
                                    hijriCalendar = HijrahDate.from(firstDayOfMonth)
                                    selectedMonth = hijriCalendar.get(ChronoField.MONTH_OF_YEAR)
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                    }
                    Divider()

                    // Days Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        for (day in daysOfWeek) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp),
                                color = daysColor
                            )
                        }
                    }

                    // Month Days
                    val firstDayOfWeek = hijriCalendar.with(TemporalAdjusters.firstDayOfMonth())
                        .get(ChronoField.DAY_OF_WEEK)

                    val daysInMonth = hijriCalendar.range(ChronoField.DAY_OF_MONTH)
                    val startDay = daysInMonth.minimum
                    val endDay = daysInMonth.maximum
                    val hijriDays = (startDay..endDay).toList()
                    val emptyCellsCount = (firstDayOfWeek - 1) % 7

                    val displayableDays =
                        List(emptyCellsCount) { "" } + hijriDays.map(Long::toString)
                    LazyVerticalGrid(columns = Fixed(7)) {
                        displayableDays.forEach { hijriDay ->
                            val isSelected =
                                hijriDay.toIntOrNull() == selectedDay &&
                                        selectedMonth == selectedDate.get(ChronoField.MONTH_OF_YEAR) &&
                                        selectedYear == selectedDate.get(ChronoField.YEAR)

                            val backgroundColor = if (isSelected) Color.White else Color.Transparent

                            item {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(52.dp)
                                        .clickable {
                                            selectedDay = hijriDay.toIntOrNull() ?: selectedDay
                                            onDateSelected(
                                                HijrahDate.of(
                                                    selectedYear,
                                                    selectedMonth,
                                                    selectedDay
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hijriDay,
                                        textAlign = TextAlign.Center,
                                        color = if (isSelected) selectedDayTextColor else MaterialTheme
                                            .typography.bodyLarge.color,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .background(backgroundColor, shape = CircleShape)
                                            .padding(8.dp)

                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .border(1.dp, MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )

                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "اليوم: $hijriDateFormatted هـ ",
                        style = TextStyle(fontSize = 18.sp, color = Color.White),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                }
            }

        }
    }


}



@Composable
private fun YearPicker(
    value: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val years = (1400..2025).toList().map { it.toString() }

    val currentIndex = years.indexOf(value.toString())

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val newIndex = (currentIndex - 1).coerceIn(0, years.size - 1)
                onValueChange(years[newIndex])
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Increase Month",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.weight(.15f)
            )
        }

        Text(
            text = years[currentIndex],
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        IconButton(
            onClick = {
                val newIndex = (currentIndex + 1).coerceIn(0, years.size - 1)
                onValueChange(years[newIndex])
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Decrease Month",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.weight(.15f)
            )
        }
    }
}

