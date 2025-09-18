package com.elsharif.dailyseventy.presentation.prayertimes

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.util.Screen
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class MonthlyPrayerData(
    val date: LocalDate,
    val dayOfWeek: String,
    val prayers: List<UiPrayerTime>,
    val isToday: Boolean
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyPrayerTimesPage(
    navController: NavController,
    viewModel: PrayerTimeViewModel = hiltViewModel()
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // States للبيانات
    var monthlyData by remember { mutableStateOf<List<MonthlyPrayerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val monthPickerState = rememberUseCaseState()

    // جلب بيانات الشهر
    LaunchedEffect(selectedMonth) {
        isLoading = true
        errorMessage = null

        try {
            val monthData = mutableListOf<MonthlyPrayerData>()
            val daysInMonth = selectedMonth.lengthOfMonth()
            val today = LocalDate.now()

            for (day in 1..daysInMonth) {
                val date = selectedMonth.atDay(day)
                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ar"))

                // تغيير التاريخ في الـ ViewModel وجلب المواقيت
                viewModel.setDate(date)

                // انتظار قصير للحصول على البيانات
                kotlinx.coroutines.delay(100)

                // جلب المواقيت من الـ ViewModel
                val prayerTimesState = viewModel.prayerTimesState.value
                val prayers = when (prayerTimesState) {
                    is PrayerUiState.Success -> prayerTimesState.prayers
                    else -> emptyList()
                }

                monthData.add(
                    MonthlyPrayerData(
                        date = date,
                        dayOfWeek = dayOfWeek,
                        prayers = prayers,
                        isToday = date == today
                    )
                )
            }

            monthlyData = monthData

            // التمرير لليوم الحالي
            val todayIndex = monthData.indexOfFirst { it.isToday }
            if (todayIndex >= 0) {
                scope.launch {
                    lazyListState.animateScrollToItem(todayIndex)
                }
            }

        } catch (e: Exception) {
            errorMessage = "حدث خطأ في تحميل مواقيت الشهر: ${e.message}"
        } finally {
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.MonthlyPrayerTimes.route,navController)
        }
    )
    { paddingValues -> 
            CalendarDialog(
                state = monthPickerState,
                selection = CalendarSelection.Date { date ->
                    selectedMonth = YearMonth.from(date)
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues )
            ) {
                // شريط اختيار الشهر
                MonthSelector(
                    selectedMonth = selectedMonth,
                    onMonthClick = { monthPickerState.show() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading -> {
                        LoadingView()
                    }

                    errorMessage != null -> {
                        ErrorView(
                            message = errorMessage!!,
                            onRetry = {
                                selectedMonth = selectedMonth // إعادة تشغيل LaunchedEffect
                            }
                        )
                    }

                    monthlyData.isEmpty() -> {
                        EmptyView()
                    }

                    else -> {
                        MonthlyPrayerTimesTable(
                            monthlyData = monthlyData,
                            lazyListState = lazyListState
                        )
                    }
                }
            }
        }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthSelector(
    selectedMonth: YearMonth,
    onMonthClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        onClick = onMonthClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("ar"))} ${selectedMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "جاري تحميل مواقيت الشهر...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة المحاولة")
            }
        }
    }
}

@Composable
private fun EmptyView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "لا توجد مواقيت متاحة لهذا الشهر",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthlyPrayerTimesTable(
    monthlyData: List<MonthlyPrayerData>,
    lazyListState: LazyListState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header للجدول
            TableHeader()

            // محتويات الجدول
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(monthlyData) { index, dayData ->
                    TableRow(
                        dayData = dayData,
                        isEven = index % 2 == 0
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val headers = listOf("اليوم", "التاريخ", "الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")

        headers.forEach { header ->
            Text(
                text = header,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TableRow(
    dayData: MonthlyPrayerData,
    isEven: Boolean
) {
    val backgroundColor = when {
        dayData.isToday -> MaterialTheme.colorScheme.primaryContainer
        isEven -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        dayData.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .let { modifier ->
                if (dayData.isToday) {
                    modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    )
                } else {
                    modifier
                }
            }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // يوم الأسبوع
        Text(
            text = dayData.dayOfWeek,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal
        )

        // التاريخ
        Text(
            text = dayData.date.dayOfMonth.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal
        )

        // أوقات الصلوات
        val prayerNames = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")

        prayerNames.forEach { prayerName ->
            val prayer = dayData.prayers.find {
                it.name.contains(prayerName, ignoreCase = true) ||
                        getPrayerArabicName(prayerName).contains(it.name, ignoreCase = true)
            }

            Text(
                text = prayer?.let { parseTime(it.time) } ?: "--:--",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun getPrayerArabicName(prayerName: String): String = when (prayerName.lowercase()) {
    "fajr" -> "الفجر"
    "sunrise" -> "الشروق"
    "dhuhr" -> "الظهر"
    "asr" -> "العصر"
    "maghrib" -> "المغرب"
    "isha" -> "العشاء"
    else -> ""
}

private fun parseTime(time: String): String {
    return try {
        // استخراج الوقت من النص (مثل "05:30 (EET)" -> "05:30")
        val timePattern = Regex("""(\d{2}:\d{2})""")
        val matchResult = timePattern.find(time)
        matchResult?.value ?: time
    } catch (e: Exception) {
        time
    }
}