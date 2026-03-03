package com.elsharif.dailyseventy.presentation.prayertimes


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.components.OutlinedRow
import com.elsharif.dailyseventy.presentation.components.PrimaryColorDivider
import com.elsharif.dailyseventy.presentation.components.maps.MapView
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTimesAuthority
import com.elsharif.dailyseventy.util.Screen
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
            DashboardScreenTopBar(Screen.PrayerTimes.titleRes,navController)
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
                        text = stringResource(R.string.loading_prayer_times),
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
                            ?: stringResource(R.string.error_loading_prayer_times),
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.check_internet),
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

@SuppressLint("MissingPermission", "LocalContextConfigurationRead")
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
    val addressText by viewModel.addressText.collectAsState()
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

    val currentLocale = context.resources.configuration.locales[0]

    LaunchedEffect(currentLocale) {
        viewModel.refreshAddressForLanguageChange()
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
        header = Header.Default(stringResource(R.string.choose_authority)),
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
        // الخريطة
        PrayerTimesMapView(
            viewModel = viewModel,
            onMapClick = { geoPoint ->
                onMapClick(geoPoint)
                // 🟢 تحديث العنوان من الفيو موديل مباشرة
                viewModel.updateLocation(geoPoint)
                viewModel.updateAddressFromGeoPoint(geoPoint)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            /*var isGettingLocation by remember { mutableStateOf(false) }

            // زر جلب الموقع الحالي - محسن
            Button(
                onClick = {
                    isGettingLocation = true
                    val fused = com.google.android.gms.location.LocationServices
                        .getFusedLocationProviderClient(context)

                    fused.lastLocation.addOnCompleteListener { task ->
                        isGettingLocation = false
                        if (task.isSuccessful && task.result != null) {
                            val loc = task.result
                            val geo = GeoPoint(loc.latitude, loc.longitude)

                            // 🔥 هنا المشكلة! يجب استدعاء updateLocation من الـ ViewModel
                            // بدلاً من onMapClick فقط
                            viewModel.updateLocation(geo) // هذا يحدث الخريطة والموقع في الـ ViewModel

                            // تحديث العنوان أيضاً
                            viewModel.updateAddressFromGeoPoint(geo)
                        } else {
                            // في حالة فشل lastLocation، جرب طلب موقع جديد
                            isGettingLocation = true
                            val locationRequest = LocationRequest.Builder(
                                Priority.PRIORITY_HIGH_ACCURACY, 5000L
                            ).build()

                            val locationCallback = object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult) {
                                    isGettingLocation = false
                                    locationResult.lastLocation?.let { location ->
                                        val geoPoint = GeoPoint(location.latitude, location.longitude)

                                        // 🔥 استدعاء updateLocation مرة أخرى هنا
                                        viewModel.updateLocation(geoPoint)
                                        viewModel.updateAddressFromGeoPoint(geoPoint)

                                        // إزالة callback بعد الحصول على الموقع
                                        fused.removeLocationUpdates(this)
                                    }
                                }
                            }

                            try {
                                fused.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback,
                                    context.mainLooper
                                )

                                // timeout بعد 10 ثواني
                                Handler(context.mainLooper).postDelayed({
                                    isGettingLocation = false
                                    fused.removeLocationUpdates(locationCallback)
                                }, 10000)

                            } catch (e: SecurityException) {
                                isGettingLocation = false
                                // معالجة خطأ الصلاحيات
                            }
                        }
                    }
                },
                enabled = !isGettingLocation,
                modifier = Modifier.weight(1f)
            ) {
                if (isGettingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري التحديد...")
                } else {
                    Text(stringResource(R.string.GetMyLocation))
                }
            }*/

            // زر جلب الموقع الحالي
            // استخدام الزر المحسن
            /*GetLocationButton(
                context = context,
                viewModel = viewModel,
                onLocationUpdate = { geoPoint ->
                    onMapClick(geoPoint)
                },
                modifier = Modifier.weight(1f)
            )*/

            Column(
                modifier =
                    Modifier.weight(1f)

            ) {
                LocationButton(
                    enabled = if(isNetworkAvailable) true else false,
                    context = context,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            // عرض المنطقة + المدينة + الدولة
              Text(
                    text = addressText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

        }


        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.choose_location),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Date Picker Row
        OutlinedRow(
            Modifier
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
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
                        !isNetworkAvailable && selectedAuthority.name.isEmpty() -> stringResource(R.string.not_available_offline)
                        selectedAuthority.name.isEmpty() -> stringResource(R.string.no_authority_available)
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
                        text = stringResource(R.string.last_saved_prayer_times),
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
                        text = stringResource(R.string.no_prayer_times_available),
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!isNetworkAvailable) stringResource(R.string.check_internet_try_again)
                        else stringResource(R.string.try_different_date_or_location),
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


// ─────────────────────────────────────────────────────────────────────────────
// التعديل المطلوب في PrayerTimesPage.kt
// فقط دالة PrayerTimesMapView — بدّل بيها الموجودة
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PrayerTimesMapView(viewModel: PrayerTimeViewModel, onMapClick: (GeoPoint) -> Unit) {
    val mapState by viewModel.mapState.collectAsState()

    when (mapState) {
        is MapUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.loading_map))
                }
            }
        }

        is MapUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (mapState as MapUiState.Error).message
                            ?: stringResource(R.string.map_loading_error),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.check_internet),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.retryConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.retry), color = Color.White)
                    }
                }
            }
        }

        is MapUiState.Offline -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFF37474F)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.offline_mode),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.retryConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.check_connection), color = Color.White)
                    }
                }
            }
        }

        is MapUiState.Success -> {
            // ✅ الـ MapView الجديد بيحتوي على Search Bar جوّاه تلقائياً
            MapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                currentLocation = (mapState as MapUiState.Success).location,
                onMapClick = { geoPoint ->
                    viewModel.updateLocation(geoPoint)
                    viewModel.updateAddressFromGeoPoint(geoPoint)
                    onMapClick(geoPoint)
                }
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

    val backgroundColor = when {
        isNextPrayer -> MaterialTheme.colorScheme.primaryContainer
        else  -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isNextPrayer -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
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
                color = textColor,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(0.5f)
            )

            Text(
                text = uiPrayerTime.time,
                fontSize = 14.sp,
                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.weight(0.5f)
            )

            Text(
                text = remainingTime,
                fontSize = 14.sp,
                fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.End,
                maxLines = 1
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

@SuppressLint("MissingPermission")
@Composable
private fun GetLocationButton(
    context: Context,
    viewModel: PrayerTimeViewModel,
    onLocationUpdate: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    Button(
        onClick = {
            isLoading = true
            locationError = null

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // إعدادات طلب الموقع
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000L
            ).apply {
                setMinUpdateIntervalMillis(5000L)
                setMaxUpdateDelayMillis(15000L)
            }.build()

            // callback لاستقبال الموقع
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    isLoading = false
                    locationResult.lastLocation?.let { location ->
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        onLocationUpdate(geoPoint)
                        viewModel.updateAddressFromGeoPoint(geoPoint)

                        // إيقاف تحديثات الموقع بعد الحصول على النتيجة
                        fusedLocationClient.removeLocationUpdates(this)
                    } ?: run {
                        locationError = "فشل في تحديد الموقع"
                    }
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable) {
                        isLoading = false
                        locationError = "خدمة الموقع غير متاحة"
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            try {
                // محاولة الحصول على آخر موقع محفوظ أولاً
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        val geoPoint = GeoPoint(location.latitude, location.longitude)

                        // فحص عمر الموقع (إذا كان أقل من 5 دقائق استخدمه)
                        val locationAge = System.currentTimeMillis() - location.time
                        if (locationAge < 5 * 60 * 1000) { // 5 دقائق
                            isLoading = false
                            onLocationUpdate(geoPoint)
                            viewModel.updateAddressFromGeoPoint(geoPoint)
                        } else {
                            // الموقع قديم، طلب موقع جديد
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                context.mainLooper
                            )

                            // إيقاف الطلب بعد 15 ثانية لتجنب البطء
                            Handler(context.mainLooper).postDelayed({
                                if (isLoading) {
                                    isLoading = false
                                    locationError = "انتهت مهلة انتظار الموقع"
                                    fusedLocationClient.removeLocationUpdates(locationCallback)
                                }
                            }, 15000)
                        }
                    } else {
                        // لا يوجد موقع محفوظ، طلب موقع جديد
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            context.mainLooper
                        )

                        // إيقاف الطلب بعد 15 ثانية
                        Handler(context.mainLooper).postDelayed({
                            if (isLoading) {
                                isLoading = false
                                locationError = "انتهت مهلة انتظار الموقع"
                                fusedLocationClient.removeLocationUpdates(locationCallback)
                            }
                        }, 15000)
                    }
                }
            } catch (e: SecurityException) {
                isLoading = false
                locationError = "لا توجد صلاحية للوصول للموقع"
            }
        },
        enabled = !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("جاري التحديد...")
        } else {
            Icon(
                  imageVector = Icons.Default.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.GetMyLocation))
        }
    }

    // عرض رسالة الخطأ إن وجدت
    locationError?.let { error ->
        LaunchedEffect(error) {
            delay(3000)
            locationError = null
        }
        Text(
            text = error,
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    viewModel: PrayerTimeViewModel,
    onResult: (loading: Boolean, error: String?) -> Unit
) {
    onResult(true, null)

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // إعدادات طلب الموقع
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 5000L
    ).apply {
        setMinUpdateIntervalMillis(2000L)
        setMaxUpdateDelayMillis(10000L)
    }.build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            onResult(false, null)
            locationResult.lastLocation?.let { location ->
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                viewModel.updateLocation(geoPoint)
                viewModel.updateAddressFromGeoPoint(geoPoint)
            }
            fusedLocationClient.removeLocationUpdates(this)
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            if (!availability.isLocationAvailable) {
                onResult(false, "خدمة الموقع غير متاحة حالياً")
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    try {
        // محاولة الحصول على آخر موقع أولاً
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val locationAge = System.currentTimeMillis() - location.time

                // إذا كان الموقع أقل من دقيقتين استخدمه
                if (locationAge < 2 * 60 * 1000) {
                    onResult(false, null)
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    viewModel.updateLocation(geoPoint)
                    viewModel.updateAddressFromGeoPoint(geoPoint)
                    return@addOnCompleteListener
                }
            }

            // الموقع المحفوظ قديم أو غير موجود، اطلب موقع جديد
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )

            // timeout بعد 15 ثانية
            Handler(context.mainLooper).postDelayed({
                onResult(false, "انتهت مهلة انتظار الموقع، جرب مرة أخرى")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }, 15000)
        }
    } catch (e: SecurityException) {
        onResult(false, "خطأ في الصلاحيات")
    } catch (e: Exception) {
        onResult(false, "خطأ في تحديد الموقع")
    }
}