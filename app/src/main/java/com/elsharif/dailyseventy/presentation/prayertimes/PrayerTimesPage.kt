package com.elsharif.dailyseventy.presentation.prayertimes


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.common.OutlinedRow
import com.elsharif.dailyseventy.presentation.common.PrimaryColorDivider
import com.elsharif.dailyseventy.presentation.common.maps.MapView
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTimesAuthority
import com.elsharif.dailyseventy.util.Screen
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


private const val TAG = "PrayerTimesPage"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PrayerTimesPage(
    navController: NavController,
    viewModel: PrayerTimeViewModel = hiltViewModel()
) {

    val currentLocation by viewModel.currentLocationFlow.collectAsState(GeoPoint(30.0, 30.0))
    val selectedDate by viewModel.currentDateFlow.collectAsState(LocalDate.now())
    val prayerTimesAuthorities by viewModel.prayerTimesAuthoritiesFlow.collectAsState(emptyList())
    val selectedAuthority by viewModel.currentPrayerAuthorityFlow.collectAsState(
        UiPrayerTimesAuthority(-1, "")
    )

    val prayerTimesState by viewModel.prayerTimesState.collectAsState()
    Scaffold(
        topBar = {
            DashboardScreenTopBar(Screen.PrayerTimes.route,navController)
        }
    ) { paddingValues ->

    when (prayerTimesState) {
        is PrayerUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "نستعد لمواقيت الصلاة… 🌙",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        is PrayerUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (prayerTimesState as PrayerUiState.Error).message
                            ?: "تعذر تحميل المواقيت ⚠️",
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تأكد من اتصالك بالإنترنت وأعد المحاولة",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        is PrayerUiState.Success -> {
            val prayerTimes = (prayerTimesState as PrayerUiState.Success).prayers
            PrayerTimesViews(
                paddingValues=paddingValues,
                viewModel = viewModel,
                currentLocation = currentLocation,
                pickedDate = selectedDate.toString(),
                selectedAuthority = selectedAuthority,
                prayerTimesAuthorities = prayerTimesAuthorities,
                prayerTimes = prayerTimes,
                onMapClick = { viewModel.updateLocation(it) },
                onDateChange = { viewModel.setDate(it) },
                onAuthorityChange = { viewModel.updateAuthority(it) }
            )
        }
    }}
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerTimesViews(
    paddingValues: PaddingValues,
    viewModel: PrayerTimeViewModel,
    currentLocation: GeoPoint,
    pickedDate: String,
    selectedAuthority: UiPrayerTimesAuthority,
    prayerTimesAuthorities: List<UiPrayerTimesAuthority>,
    prayerTimes: List<UiPrayerTime>,
    onMapClick: (GeoPoint) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onAuthorityChange: (UiPrayerTimesAuthority) -> Unit
) {
    val datePickerState = rememberUseCaseState()
    val authorityDialogListState = rememberUseCaseState()
    val context = LocalContext.current

    // تحقق من حالة الاتصال
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val isNetworkAvailable = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ||
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    val authorityListOptions =
        prayerTimesAuthorities.map {
            ListOption(
                titleText = it.name, selected = it == selectedAuthority && it.name.isNotEmpty()
            )
        }

    CalendarDialog(
        state = datePickerState,
        selection = CalendarSelection.Date(onSelectDate = onDateChange)
    )

    ListDialog(
        state = authorityDialogListState,
        header = Header.Default("اختر التفويض"),
        selection = ListSelection.Single(options = authorityListOptions) { index, _ ->
            if (index < prayerTimesAuthorities.size) {
                onAuthorityChange(prayerTimesAuthorities[index])
            }
        }
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        PrayerTimesMapView(viewModel = viewModel, onMapClick = onMapClick)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "اختر الموقع بدقة",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Date Picker Row
        OutlinedRow(
            Modifier
                .padding(top = 16.dp, start = 12.dp, end = 12.dp)
                .clickable { datePickerState.show() }
        ) {
            Text(text = pickedDate, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.weight(1f))
            Image(painter = painterResource(R.drawable.ic_calender), contentDescription = null)
        }

        // Authority Picker Row with offline handling
        OutlinedRow(
            Modifier
                .padding(top = 14.dp, start = 12.dp, end = 12.dp)
                .clickable {
                    if (prayerTimesAuthorities.isNotEmpty()) {
                        authorityDialogListState.show()
                    }
                }
                .let { modifier ->
                    if (!isNetworkAvailable || prayerTimesAuthorities.isEmpty()) {
                        modifier.alpha(0.6f)
                    } else {
                        modifier
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // عرض حالة الاتصال إذا لزم الأمر
                if (!isNetworkAvailable) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "غير متصل",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = when {
                        !isNetworkAvailable && selectedAuthority.name.isEmpty() -> "غير متاح أثناء عدم الاتصال"
                        selectedAuthority.name.isEmpty() -> "لا يوجد تفويض متاح"
                        else -> selectedAuthority.name
                    },
                    color = if (!isNetworkAvailable || selectedAuthority.name.isEmpty()) Color.Gray else Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Image(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .let { mod ->
                        if (!isNetworkAvailable || prayerTimesAuthorities.isEmpty()) {
                            mod.alpha(0.5f)
                        } else {
                            mod
                        }
                    }
            )
        }

        // رسالة توضيحية في حالة عدم الاتصال
        if (!isNetworkAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "يتم عرض آخر المواقيت المحفوظة محلياً",
                        color = Color(0xFFE65100),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // الصلاة القادمة
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val now = LocalTime.now()
        val nextPrayer = prayerTimes.minByOrNull {
            val time = runCatching { LocalTime.parse(it.time, formatter) }.getOrNull()
            if (time != null) {
                val duration = Duration.between(now, time)
                if (!duration.isNegative) duration else Duration.ofDays(1)
            } else Duration.ofDays(1)
        }

        if (prayerTimes.isEmpty()) {
            // عرض رسالة في حالة عدم توفر مواقيت
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد مواقيت متاحة",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!isNetworkAvailable) {
                            "تأكد من الاتصال بالإنترنت وأعد المحاولة"
                        } else {
                            "حاول اختيار تاريخ أو موقع مختلف"
                        },
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            prayerTimes.forEach { prayer ->
                PrayerTimeListItem(
                    uiPrayerTime = prayer,
                    isNextPrayer = prayer == nextPrayer
                )
            }
        }
    }
}


@Composable
fun PrayerTimesMapView(viewModel: PrayerTimeViewModel, onMapClick: (GeoPoint) -> Unit) {
    val mapState by viewModel.mapState.collectAsState()

    when (mapState) {
        is MapUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("جارٍ تحميل الخريطة…")
                }
            }
        }

        is MapUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (mapState as MapUiState.Error).message
                            ?: "تعذر تحميل الخريطة ⚠️",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تأكد من اتصالك بالإنترنت",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.retryConnection() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إعادة المحاولة", color = Color.White)
                    }
                }
            }
        }

        is MapUiState.Offline -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF37474F)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "غير متصل",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "وضع عدم الاتصال 📴",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المواقيت محفوظة محلياً",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 آخر موقع محفوظ",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.retryConnection() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحقق من الاتصال", color = Color.White)
                    }
                }
            }
        }

        is MapUiState.Success -> {
            MapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                currentLocation = (mapState as MapUiState.Success).location,
                onMapClick = onMapClick
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun PrayerTimeListItem(
    uiPrayerTime: UiPrayerTime,
    isNextPrayer: Boolean = false
) {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    var remainingTime by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(uiPrayerTime.time) {
        while (true) {
            val now = LocalTime.now()
            val prayerTime = runCatching { LocalTime.parse(uiPrayerTime.time, formatter) }.getOrNull()
            if (prayerTime != null) {
                var remainingDuration = Duration.between(now, prayerTime)
                if (remainingDuration.isNegative) remainingDuration = Duration.ZERO

                val hours = remainingDuration.toHours()
                val minutes = remainingDuration.minusHours(hours).toMinutes()
                val seconds = remainingDuration.minusHours(hours).minusMinutes(minutes).seconds

                remainingTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isNextPrayer) Color(0xFFE3F2FD) else Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp)
        ) {
            val d: Drawable? = runCatching {
                AppCompatResources.getDrawable(LocalContext.current, uiPrayerTime.iconRes)
            }.getOrNull()

            if (d != null) {
                Image(
                    bitmap = d.toBitmap().asImageBitmap(),
                    contentDescription = uiPrayerTime.name,
                    modifier = Modifier
                        .scale(2f)
                        .padding(4.dp)
                        .weight(.1f)
                )
            } else {
                // استخدم أي أيقونة افتراضية بدلًا من الرمي
                Image(
                    painter = painterResource(R.drawable.doaa),
                    contentDescription = uiPrayerTime.name,
                    modifier = Modifier
                        .scale(2f)
                        .padding(4.dp)
                        .weight(.1f)
                )
            }


            Text(
                text = uiPrayerTime.name,
                fontSize = 14.sp,
                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(0.5f)
            )

            Text(
                text = uiPrayerTime.time,
                fontSize = 14.sp,
                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(0.5f)
            )

            Text(
                text = remainingTime,
                fontSize = 14.sp,
                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(R.drawable.stopwatch),
                contentDescription = null
            )
        }
        PrimaryColorDivider(horizontalPadding = 10.dp)
    }
}

