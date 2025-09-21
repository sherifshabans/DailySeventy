/* PrayerTimeViewModel.kt *//*


package com.elsharif.dailyseventy.presentation.prayertimes

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.dailyazkar.scheduleSunriseReminder
import com.elsharif.dailyseventy.domain.data.sharedpreferences.FridayPrefs
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThird
import com.elsharif.dailyseventy.domain.friday.scheduleAsrReminder
import com.elsharif.dailyseventy.domain.friday.scheduleKahfReminder
import com.elsharif.dailyseventy.domain.thirdnight.scheduleNightThirdNotifications
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTimesAuthority
import com.example.core.domain.prayertiming.DomainPrayerTiming
import com.example.core.domain.prayertiming.DomainPrayerTimingSchool
import com.example.core.usecase.GetCurrentPrayerTimesAuthorityUseCase
import com.example.core.usecase.GetPrayerTimesAuthoritiesUseCase
import com.example.core.usecase.GetPrayerTimesUseCase
import com.example.core.usecase.GetUserLocationUseCase
import com.example.core.usecase.SetCurrentPrayerTimesAuthorityUseCase
import com.example.core.usecase.SetUserLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(val location: GeoPoint) : MapUiState()
    data class Error(val message: String? = null) : MapUiState()
}

@SuppressLint("NewApi")
@HiltViewModel
class PrayerTimeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val setUserLocationUseCase: SetUserLocationUseCase,
    private val getPrayerTimesAuthoritiesUseCase: GetPrayerTimesAuthoritiesUseCase,
    private val getCurrentPrayerTimesAuthorityUseCase: GetCurrentPrayerTimesAuthorityUseCase,
    private val setCurrentPrayerTimesAuthorityUseCase: SetCurrentPrayerTimesAuthorityUseCase,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
) : ViewModel() {

    private val _mapState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val mapState: StateFlow<MapUiState> = _mapState

    init {
        fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() = viewModelScope.launch {
        _mapState.value = MapUiState.Loading
        try {
            val locationPair = getUserLocationUseCase().first()
            val location = GeoPoint(locationPair.first, locationPair.second)
            _mapState.value = MapUiState.Success(location)
        } catch (e: Exception) {
            _mapState.value = MapUiState.Error("تعذر جلب الموقع الحالي")
        }
    }

    fun updateLocation(location: GeoPoint) = viewModelScope.launch {
        _mapState.value = MapUiState.Loading
        try {
            setUserLocationUseCase(location.latitude, location.longitude).collect()
            _mapState.value = MapUiState.Success(location)
        } catch (e: Exception) {
            _mapState.value = MapUiState.Error("فشل تحديث الموقع")
        }
    }

    val currentLocationFlow: Flow<GeoPoint> = mapState.map { state ->
        when (state) {
            is MapUiState.Success -> state.location
            else -> GeoPoint(30.0, 30.0)
        }
    }

    private val _prayerTimesState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val prayerTimesState: StateFlow<PrayerUiState> = _prayerTimesState

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentDateFlow = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val prayerTimesAuthoritiesFlow: Flow<List<UiPrayerTimesAuthority>> =
        getPrayerTimesAuthoritiesUseCase().map { it.map { UiPrayerTimesAuthority(it.id, it.name) } }

    val currentPrayerAuthorityFlow: Flow<UiPrayerTimesAuthority> =
        getCurrentPrayerTimesAuthorityUseCase().map { UiPrayerTimesAuthority(it.id, it.name) }

    // دالة مساعدة لتحويل أسماء الصلوات إلى Resource IDs
    private fun getPrayerNameResource(prayerName: String): Int = when (prayerName.lowercase()) {
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

    // دالة مساعدة لتحويل أسماء الصور إلى Resource IDs
    private fun getPrayerImageResource(imageName: String): Int {
        val imgId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (imgId != 0) imgId else R.drawable.doaa // قيمة افتراضية
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal val prayerTimesFlow =
        combine(currentLocationFlow, currentDateFlow, currentPrayerAuthorityFlow) { location, date, authority ->
            Log.d("PrayerViewModel", "Getting prayers for date: $date")

            val timingsList = getPrayerTimesUseCase(
                location.latitude, location.longitude, date, DomainPrayerTimingSchool(
                    authority.idx, authority.name
                )
            ).firstOrNull() ?: emptyList()

            Log.d("PrayerViewModel", "Retrieved ${timingsList.size} prayer timings")
            Log.d("PrayerViewModel", "Prayer names: ${timingsList.map { it.prayer.name }}")

            val filteredPrayers = timingsList.filter { it.date == date.toString() }
                .sortedBy {
                    // parse الوقت في LocalTime عشان نقدر نرتبه
                    val timeStr = parseTime(it.time) // hh:mm a
                    LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a"))
                }
            Log.d("PrayerViewModel", "Filtered prayers for $date: ${filteredPrayers.size}")

            filteredPrayers.mapNotNull { prayerTiming ->
                try {
                    val cleanName = if (prayerTiming.prayer.name.contains(":string/")) {
                        prayerTiming.prayer.name.substringAfter(":string/")
                    } else {
                        prayerTiming.prayer.name
                    }

                    val nameRes = getPrayerNameResource(cleanName)
                    val imgRes = getPrayerImageResource(prayerTiming.prayer.imageId)
                    val extractedTime = parseTime(prayerTiming.time)

                    Log.d("PrayerViewModel", "Processing prayer: $cleanName -> ${context.getString(nameRes)}")

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

                    UiPrayerTime(imgRes, context.getString(nameRes), extractedTime, formattedDuration)
                } catch (e: Exception) {
                    Log.e("PrayerViewModel", "Error processing prayer ${prayerTiming.prayer.name}: ${e.message}")
                    null // تجاهل الصلاة اللي فيها مشكلة
                }
            }
        }

    init {
        viewModelScope.launch {
            prayerTimesFlow.collect { prayers ->
                Log.d("PrayerViewModel", "Final prayers count: ${prayers.size}")
                _prayerTimesState.value = if (prayers.isNotEmpty()) {
                    PrayerUiState.Success(prayers)
                } else {
                    PrayerUiState.Error("لا توجد مواقيت الصلاة لليوم")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDate(date: LocalDate) = viewModelScope.launch { _currentDateFlow.emit(date) }

    fun updateAuthority(prayerTimesAuthority: UiPrayerTimesAuthority) = viewModelScope.launch {
        setCurrentPrayerTimesAuthorityUseCase(
            DomainPrayerTimingSchool(prayerTimesAuthority.idx, prayerTimesAuthority.name)
        ).collect()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleFridayReminders(context: Context, kahfEnabled: Boolean, asrEnabled: Boolean) {
        FridayPrefs.save(context, kahfEnabled, asrEnabled)

        viewModelScope.launch {
            val prayers = prayerTimesFlow.first()

            if (kahfEnabled) {
                scheduleKahfReminder(context)
            }

            if (asrEnabled) {
                val asr = prayers.firstOrNull { it.name.contains("Asr", true) }
                if (asr != null) {
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val asrTime = LocalTime.parse(asr.time, formatter)
                    scheduleAsrReminder(context, asrTime.hour, asrTime.minute)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleNightThirdNotificationsFromPrayerTimes(context: Context, selection: Set<NightThird>) {
        viewModelScope.launch {
            prayerTimesFlow.collect { prayers ->
                val maghrib = prayers.firstOrNull { it.name.contains("Maghrib", true) }?.time
                    ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }

                val fajr = prayers.firstOrNull { it.name.contains("Fajr", true) }?.time
                    ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }

                if (maghrib != null && fajr != null) {
                    scheduleNightThirdNotifications(context, maghrib, fajr, selection)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleSunriseAzkar(context: Context) {
        viewModelScope.launch {
            val prayers = prayerTimesFlow.first()

            val sunrise = prayers.firstOrNull {
                it.name.contains("Sunrise", true) || it.name.contains("الشروق")
            }

            if (sunrise != null) {
                val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                val sunriseTime = LocalTime.parse(sunrise.time, formatter)
                scheduleSunriseReminder(context, sunriseTime.hour, sunriseTime.minute)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(inputTime: String): String {
        val timeStartIndex = inputTime.indexOf(':') - 2
        val timeEndIndex = inputTime.indexOf('(') - 1
        val extractedTime = inputTime.substring(timeStartIndex, timeEndIndex)

        val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
        val time = LocalTime.parse(extractedTime, inputFormat)

        val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
        return outputFormat.format(time)
    }
}*/

/* PrayerTimeViewModel.kt */

package com.elsharif.dailyseventy.presentation.prayertimes

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.dailyazkar.scheduleSunriseReminder
import com.elsharif.dailyseventy.domain.data.sharedpreferences.FridayPrefs
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThird
import com.elsharif.dailyseventy.domain.friday.scheduleAsrReminder
import com.elsharif.dailyseventy.domain.friday.scheduleKahfReminder
import com.elsharif.dailyseventy.domain.thirdnight.scheduleNightThirdNotifications
import com.elsharif.dailyseventy.presentation.prayertimes.model.PrayerUiState
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.presentation.prayertimes.model.UiPrayerTimesAuthority
import com.example.core.domain.prayertiming.DomainPrayerTiming
import com.example.core.domain.prayertiming.DomainPrayerTimingSchool
import com.example.core.usecase.GetCurrentPrayerTimesAuthorityUseCase
import com.example.core.usecase.GetPrayerTimesAuthoritiesUseCase
import com.example.core.usecase.GetPrayerTimesUseCase
import com.example.core.usecase.GetUserLocationUseCase
import com.example.core.usecase.SetCurrentPrayerTimesAuthorityUseCase
import com.example.core.usecase.SetUserLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(val location: GeoPoint) : MapUiState()
    data class Error(val message: String? = null) : MapUiState()
    data class Offline(val location: GeoPoint) : MapUiState() // حالة جديدة للأوفلاين
}

@SuppressLint("NewApi")
@HiltViewModel
class PrayerTimeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val setUserLocationUseCase: SetUserLocationUseCase,
    private val getPrayerTimesAuthoritiesUseCase: GetPrayerTimesAuthoritiesUseCase,
    private val getCurrentPrayerTimesAuthorityUseCase: GetCurrentPrayerTimesAuthorityUseCase,
    private val setCurrentPrayerTimesAuthorityUseCase: SetCurrentPrayerTimesAuthorityUseCase,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
) : ViewModel() {

    private val _mapState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val mapState: StateFlow<MapUiState> = _mapState
    private val _addressText = MutableStateFlow("")
    val addressText: StateFlow<String> = _addressText

    init {
        fetchCurrentLocation()
    }

    // فحص حالة الاتصال بالإنترنت
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    }

    fun updateAddressFromGeoPoint(geoPoint: GeoPoint) {
        val newAddress = getAddressFromGeoPoint(context, geoPoint)
        _addressText.value = newAddress
    }

    private fun getAddressFromGeoPoint(context: Context, geoPoint: GeoPoint): String {
        return try {
            val currentLocale = context.resources.configuration.locales[0]
            val geocoder = android.location.Geocoder(context, currentLocale)

            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val adminArea = addresses[0].adminArea ?: ""   // المحافظة / المنطقة
                val city = addresses[0].locality ?: ""         // المدينة
                val country = addresses[0].countryName ?: ""   // الدولة
                listOf(city,adminArea,  country)
                    .filter { it.isNotEmpty() }
                    .joinToString(" - ")
            } else {
                "موقع غير معروف"
            }
        } catch (e: Exception) {
            "تعذر تحديد الموقع"
        }
    }
    // 🔧 أضف دالة لتحديث العنوان عند تغيير اللغة
    fun refreshAddressForLanguageChange() = viewModelScope.launch {
        try {
            val locationPair = getUserLocationUseCase().first()
            val location = GeoPoint(locationPair.first, locationPair.second)
            updateAddressFromGeoPoint(location)
        } catch (e: Exception) {
            // في حالة فشل جلب الموقع، استخدم آخر موقع معروف من الـ state
            val currentLocation = when (val state = _mapState.value) {
                is MapUiState.Success -> state.location
                is MapUiState.Offline -> state.location
                else -> GeoPoint(30.0444, 31.2357) // القاهرة افتراضي
            }
            updateAddressFromGeoPoint(currentLocation)
        }
    }
    private fun fetchCurrentLocation() = viewModelScope.launch {
        _mapState.value = MapUiState.Loading
        try {
            val locationPair = getUserLocationUseCase().first()
            val location = GeoPoint(locationPair.first, locationPair.second)

            // ✅ تحديث العنوان مباشرة بعد جلب الموقع
            updateAddressFromGeoPoint(location)

            if (isNetworkAvailable()) {
                _mapState.value = MapUiState.Success(location)
            } else {
                _mapState.value = MapUiState.Offline(location)
            }
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Error fetching location: ${e.message}")
            val defaultLocation = GeoPoint(30.0444, 31.2357) // القاهرة افتراضي

            // ✅ كمان هنا نجيب العنوان الافتراضي
            updateAddressFromGeoPoint(defaultLocation)

            if (isNetworkAvailable()) {
                _mapState.value = MapUiState.Error("تعذر جلب الموقع الحالي")
            } else {
                _mapState.value = MapUiState.Offline(defaultLocation)
            }
        }
    }

    fun updateLocation(location: GeoPoint) = viewModelScope.launch {
        if (!isNetworkAvailable()) {
            _mapState.value = MapUiState.Offline(location)
            return@launch
        }

        _mapState.value = MapUiState.Loading
        try {
            setUserLocationUseCase(location.latitude, location.longitude).collect()
            _mapState.value = MapUiState.Success(location)
        } catch (e: Exception) {
            _mapState.value = MapUiState.Error("فشل تحديث الموقع")
        }
    }

    val currentLocationFlow: Flow<GeoPoint> = mapState.map { state ->
        when (state) {
            is MapUiState.Success -> state.location
            is MapUiState.Offline -> state.location
            else -> GeoPoint(30.0444, 31.2357) // القاهرة كموقع افتراضي
        }
    }

    private val _prayerTimesState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val prayerTimesState: StateFlow<PrayerUiState> = _prayerTimesState

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentDateFlow = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val prayerTimesAuthoritiesFlow: Flow<List<UiPrayerTimesAuthority>> =
        getPrayerTimesAuthoritiesUseCase()
            .map { it.map { UiPrayerTimesAuthority(it.id, it.name) } }
            .catch { e ->
                Log.e("PrayerViewModel", "Error getting authorities: ${e.message}")
                // في حالة الخطأ، نعيد قائمة فارغة أو قائمة افتراضية
                emit(emptyList())
            }

    val currentPrayerAuthorityFlow: Flow<UiPrayerTimesAuthority> =
        getCurrentPrayerTimesAuthorityUseCase()
            .map { UiPrayerTimesAuthority(it.id, it.name) }
            .catch { e ->
                Log.e("PrayerViewModel", "Error getting current authority: ${e.message}")
                // في حالة الخطأ، نعيد authority افتراضي
                emit(UiPrayerTimesAuthority(-1, "افتراضي"))
            }

    // دالة مساعدة لتحويل أسماء الصلوات إلى Resource IDs
    private fun getPrayerNameResource(prayerName: String): Int = when (prayerName.lowercase()) {
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

    // دالة مساعدة لتحويل أسماء الصور إلى Resource IDs
    private fun getPrayerImageResource(imageName: String): Int {
        val imgId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (imgId != 0) imgId else R.drawable.doaa // قيمة افتراضية
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal val prayerTimesFlow =
        combine(currentLocationFlow, currentDateFlow, currentPrayerAuthorityFlow) { location, date, authority ->
            Log.d("PrayerViewModel", "Getting prayers for date: $date, Network: ${isNetworkAvailable()}")

            val timingsList = try {
                getPrayerTimesUseCase(
                    location.latitude, location.longitude, date, DomainPrayerTimingSchool(
                        authority.idx, authority.name
                    )
                ).firstOrNull() ?: emptyList()
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error getting prayer times: ${e.message}")
                // في حالة الخطأ، نعيد قائمة فارغة أو البيانات المحفوظة في الكاش
                emptyList()
            }

            Log.d("PrayerViewModel", "Retrieved ${timingsList.size} prayer timings")

            val filteredPrayers = timingsList.filter { it.date == date.toString() }
                .sortedBy {
                    // parse الوقت في LocalTime عشان نقدر نرتبه
                    try {
                        val timeStr = parseTime(it.time) // hh:mm a
                        LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a"))
                    } catch (e: Exception) {
                        LocalTime.MIDNIGHT // قيمة افتراضية
                    }
                }
            Log.d("PrayerViewModel", "Filtered prayers for $date: ${filteredPrayers.size}")

            filteredPrayers.mapNotNull { prayerTiming ->
                try {
                    val cleanName = if (prayerTiming.prayer.name.contains(":string/")) {
                        prayerTiming.prayer.name.substringAfter(":string/")
                    } else {
                        prayerTiming.prayer.name
                    }

                    val nameRes = getPrayerNameResource(cleanName)
                    val imgRes = getPrayerImageResource(prayerTiming.prayer.imageId)
                    val extractedTime = parseTime(prayerTiming.time)

                    Log.d("PrayerViewModel", "Processing prayer: $cleanName -> ${context.getString(nameRes)}")

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

                    UiPrayerTime(imgRes, context.getString(nameRes), extractedTime, formattedDuration)
                } catch (e: Exception) {
                    Log.e("PrayerViewModel", "Error processing prayer ${prayerTiming.prayer.name}: ${e.message}")
                    null // تجاهل الصلاة اللي فيها مشكلة
                }
            }
        }.catch { e ->
            Log.e("PrayerViewModel", "Error in prayer times flow: ${e.message}")
            emit(emptyList()) // إرسال قائمة فارغة في حالة الخطأ
        }

    init {
        viewModelScope.launch {
            prayerTimesFlow.collect { prayers ->
                Log.d("PrayerViewModel", "Final prayers count: ${prayers.size}")
                _prayerTimesState.value = if (prayers.isNotEmpty()) {
                    PrayerUiState.Success(prayers)
                } else {
                    val message = if (isNetworkAvailable()) {
                        "لا توجد مواقيت الصلاة لليوم"
                    } else {
                        "البيانات غير متوفرة أثناء وضع عدم الاتصال"
                    }
                    PrayerUiState.Error(message)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDate(date: LocalDate) = viewModelScope.launch { _currentDateFlow.emit(date) }

    fun updateAuthority(prayerTimesAuthority: UiPrayerTimesAuthority) = viewModelScope.launch {
        try {
            setCurrentPrayerTimesAuthorityUseCase(
                DomainPrayerTimingSchool(prayerTimesAuthority.idx, prayerTimesAuthority.name)
            ).collect()
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Error updating authority: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleFridayReminders(context: Context, kahfEnabled: Boolean, asrEnabled: Boolean) {
        FridayPrefs.save(context, kahfEnabled, asrEnabled)

        viewModelScope.launch {
            try {
                val prayers = prayerTimesFlow.first()

                if (kahfEnabled) {
                    scheduleKahfReminder(context)
                }

                if (asrEnabled) {
                    val asr = prayers.firstOrNull { it.name.contains("Asr", true) }
                    if (asr != null) {
                        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                        val asrTime = LocalTime.parse(asr.time, formatter)
                        scheduleAsrReminder(context, asrTime.hour, asrTime.minute)
                    }
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error scheduling Friday reminders: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleNightThirdNotificationsFromPrayerTimes(context: Context, selection: Set<NightThird>) {
        viewModelScope.launch {
            try {
                // ✅ خذ البيانات مرة واحدة واجدول
                val prayers = prayerTimesFlow.first()

                val maghrib = prayers.firstOrNull { it.name.contains("Maghrib", true) }?.time
                    ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }
                val fajr = prayers.firstOrNull { it.name.contains("Fajr", true) }?.time
                    ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }

                if (maghrib != null && fajr != null) {
                    // ✅ الجدولة تحصل مرة واحدة بس
                    scheduleNightThirdNotifications(context, maghrib, fajr, selection)
                    Log.d("PrayerViewModel", "Night third notifications scheduled for: $selection")
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error scheduling night third notifications: ${e.message}")
            }
        }
    }
    /*fun scheduleNightThirdNotificationsFromPrayerTimes(context: Context, selection: Set<NightThird>) {
        viewModelScope.launch {
            try {
                prayerTimesFlow.collect { prayers ->
                    val maghrib = prayers.firstOrNull { it.name.contains("Maghrib", true) }?.time
                        ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }

                    val fajr = prayers.firstOrNull { it.name.contains("Fajr", true) }?.time
                        ?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("hh:mm a")) }

                    if (maghrib != null && fajr != null) {
                        scheduleNightThirdNotifications(context, maghrib, fajr, selection)
                    }
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error scheduling night third notifications: ${e.message}")
            }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleSunriseAzkar(context: Context) {
        viewModelScope.launch {
            try {
                val prayers = prayerTimesFlow.first()

                val sunrise = prayers.firstOrNull {
                    it.name.contains("Sunrise", true) || it.name.contains("الشروق")
                }

                if (sunrise != null) {
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val sunriseTime = LocalTime.parse(sunrise.time, formatter)
                    scheduleSunriseReminder(context, sunriseTime.hour, sunriseTime.minute)
                }
            } catch (e: Exception) {
                Log.e("PrayerViewModel", "Error scheduling sunrise azkar: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(inputTime: String): String {
        return try {
            val timeStartIndex = inputTime.indexOf(':') - 2
            val timeEndIndex = inputTime.indexOf('(') - 1
            val extractedTime = inputTime.substring(timeStartIndex, timeEndIndex)

            val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
            val time = LocalTime.parse(extractedTime, inputFormat)

            val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
            outputFormat.format(time)
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Error parsing time: $inputTime, ${e.message}")
            "12:00 PM" // قيمة افتراضية
        }
    }

    // دالة لإعادة المحاولة في حالة توفر الإنترنت مرة أخرى
    fun retryConnection() {
        fetchCurrentLocation()
    }
}