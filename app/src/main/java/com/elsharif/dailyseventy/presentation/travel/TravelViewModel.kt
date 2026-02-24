package com.elsharif.dailyseventy.presentation.travel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.data.preferences.TravelPreferences
import com.elsharif.dailyseventy.util.notification.NotificationHelper
import com.example.core.domain.prayertiming.DomainPrayerTimingSchool
import com.example.core.usecase.GetCurrentPrayerTimesAuthorityUseCase
import com.example.core.usecase.GetPrayerTimesUseCase
import com.example.core.usecase.GetUserLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.*

// ══════════════════════════════════════════════════════════════════════════════
//  DATA MODELS
// ══════════════════════════════════════════════════════════════════════════════

const val SHAFII_TRAVEL_KM = 82.00

// مسافة العتبة للنقل البري (قطار/سيارة) — أقل من 1000 كم = داخل البلد أو دولة مجاورة
private const val LOCAL_TRANSPORT_KM = 1000

data class CityResult(
    val nameAr    : String,
    val nameEn    : String,
    val countryAr : String,
    val lat       : Double,
    val lng       : Double,
    val distanceKm: Int,
    val isTravelShafii: Boolean,
    // ✅ قطار لو أقل من 1000 كم، طيارة لو أكثر
    val isLocalTransport: Boolean = distanceKm < LOCAL_TRANSPORT_KM
)

data class FiqhResult(
    val isSafar    : Boolean,
    val distanceKm : Int,
    val details    : List<String>
)

data class TravelPrayer(
    val name      : String,
    val time      : String,
    val rakaat    : Int,
    val icon      : ImageVector,
    val color     : Color,
    val isShortened: Boolean,
    val canMerge  : Boolean,
    val mergeWith : String? = null
)

// ══════════════════════════════════════════════════════════════════════════════
//  STATE
// ══════════════════════════════════════════════════════════════════════════════

data class TravelState(
    // ── بحث المدينة
    val searchQuery  : String           = "",
    val searchResults: List<CityResult> = emptyList(),
    val isSearching  : Boolean          = false,
    val searchError  : String?          = null,
    val selectedCity : CityResult?      = null,

    // ── الحكم الشرعي
    val fiqhResult     : FiqhResult? = null,
    val showFiqhDetails: Boolean     = false,

    // ── وضع السفر النشط
    val isActive   : Boolean = false,
    val destination: String  = "",

    // ✅ بيانات الوجهة النشطة — بتتحدث فقط لما نفعّل أو نغيّر الوجهة
    val activeDistanceKm : Int    = 0,
    val activeCityLat    : Double = 0.0,
    val activeCityLng    : Double = 0.0,
    val activeDuration   : String = "",   // ✅ مدة الرحلة النشطة — مش بتتغير مع السيرش

    // ── معلومات إضافية للكارد النشط
    val timeDiff: String = "",
    val duration: String = "",  // مؤقت للعرض في الـ fiqh card فقط

    // ── موقع المستخدم
    val userLat     : Double = 0.0,
    val userLng     : Double = 0.0,
    val userCityName: String = "",

    // ── المسافة للكعبة
    val distanceToKaaba: Int = 0,

    // ── أوقات الصلاة
    val prayerTimes: List<TravelPrayer> = emptyList(),

    // ── ديالوج تغيير الوجهة
    val showChangeDestDialog: Boolean = false,

    // ── حالة داخلية
    val distanceKm: Int = 0,  // بيتحدث مع السيرش (للفقه فقط)
)

// ══════════════════════════════════════════════════════════════════════════════
//  VIEW MODEL
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(FlowPreview::class)
@HiltViewModel
class TravelViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getPrayerTimesUseCase              : GetPrayerTimesUseCase,
    private val getCurrentPrayerTimesAuthorityUseCase: GetCurrentPrayerTimesAuthorityUseCase,
    private val getUserLocationUseCase             : GetUserLocationUseCase,
    private val travelPrefs                         : TravelPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(TravelState())
    val state: StateFlow<TravelState> = _state.asStateFlow()

    // ── مراقبة تغيير searchQuery بـ debounce للبحث التلقائي
    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadUserLocation()
        restoreTravelState()

        // بحث تلقائي بعد 600ms من توقف الكتابة
        searchQueryFlow
            .debounce(600)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .onEach { query -> performSearch(query) }
            .launchIn(viewModelScope)
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOCATION
    // ══════════════════════════════════════════════════════════════════════════

    private fun loadUserLocation() = viewModelScope.launch {
        try {
            val locationPair = getUserLocationUseCase().first()
            val lat = locationPair.first
            val lng = locationPair.second

            val cityName = getCityNameFromCoords(lat, lng)
            val distToKaaba = calculateDistance(lat, lng, 21.422487, 39.826206).toInt()

            _state.update {
                it.copy(
                    userLat          = lat,
                    userLng          = lng,
                    userCityName     = cityName,
                    distanceToKaaba  = distToKaaba
                )
            }

            // ✅ لو السفر مش نشط، جيب مواقيت موقع المستخدم
                fetchPrayerTimesForLocation(lat, lng, isSafar = false)


        } catch (e: Exception) {
            Log.e("TravelVM", "Error loading user location: ${e.message}")
        }
    }

    private suspend fun getCityNameFromCoords(lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                val geocoder  = Geocoder(context, Locale("ar"))
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    listOf(addr.locality, addr.adminArea, addr.countryName)
                        .filter { !it.isNullOrEmpty() }
                        .joinToString(" - ")
                } else "موقعك الحالي"
            } catch (e: Exception) {
                "موقعك الحالي"
            }
        }

    // ══════════════════════════════════════════════════════════════════════════
    //  SEARCH
    // ══════════════════════════════════════════════════════════════════════════

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, searchError = null) }
        searchQueryFlow.value = query
        if (query.length < 2) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
    }

    fun clearSearch() {
        _state.update {
            it.copy(
                searchQuery   = "",
                searchResults = emptyList(),
                selectedCity  = null,
                fiqhResult    = null,
                searchError   = null,
                isSearching   = false
            )
        }
        searchQueryFlow.value = ""
    }

    private fun performSearch(query: String) = viewModelScope.launch {
        _state.update { it.copy(isSearching = true, searchError = null) }
        try {
            val results = searchCities(query)
            _state.update { it.copy(searchResults = results, isSearching = false) }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isSearching = false,
                    searchError = "تعذر البحث، تحقق من الاتصال"
                )
            }
        }
    }

    private suspend fun searchCities(query: String): List<CityResult> =
        withContext(Dispatchers.IO) {
            try {
                val geocoder  = Geocoder(context, Locale("ar"))
                val addresses = geocoder.getFromLocationName(query, 5)

                val userLat = _state.value.userLat
                val userLng = _state.value.userLng

                addresses?.mapNotNull { addr ->
                    if (addr.latitude == 0.0 && addr.longitude == 0.0) return@mapNotNull null

                    val distKm = calculateDistance(
                        userLat, userLng, addr.latitude, addr.longitude
                    ).toInt()

                    val nameAr = addr.locality
                        ?: addr.subAdminArea
                        ?: addr.adminArea
                        ?: query

                    val countryAr = addr.countryName ?: ""

                    // جيب الاسم الإنجليزي
                    val geocoderEn  = Geocoder(context, Locale.ENGLISH)
                    val addressesEn = geocoderEn.getFromLocation(addr.latitude, addr.longitude, 1)
                    val nameEn = addressesEn?.firstOrNull()?.let {
                        it.locality ?: it.subAdminArea ?: it.adminArea ?: query
                    } ?: query

                    CityResult(
                        nameAr         = nameAr,
                        nameEn         = nameEn,
                        countryAr      = countryAr,
                        lat            = addr.latitude,
                        lng            = addr.longitude,
                        distanceKm     = distKm,
                        isTravelShafii = distKm >= SHAFII_TRAVEL_KM
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                Log.e("TravelVM", "Search error: ${e.message}")
                emptyList()
            }
        }

    // ══════════════════════════════════════════════════════════════════════════
    //  CITY SELECTION — ❌ لا تلمس activeDistanceKm هنا
    // ══════════════════════════════════════════════════════════════════════════

    fun onCitySelected(city: CityResult) {
        val fiqh = buildFiqhResult(city)
        val timeDiff = calculateTimeDiff(
            userLng  = _state.value.userLng,
            destLng  = city.lng
        )
        val durationHrs = (city.distanceKm / 80.0)
        val duration = formatDuration(durationHrs)

        _state.update {
            it.copy(
                selectedCity  = city,
                searchResults = emptyList(),
                fiqhResult    = fiqh,
                // ✅ distanceKm للفقه فقط — activeDistanceKm مش بيتغير هنا
                distanceKm    = city.distanceKm,
                timeDiff      = timeDiff,
                duration      = duration
            )
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TRAVEL MODE — التفعيل والإيقاف والتغيير
    // ══════════════════════════════════════════════════════════════════════════

    fun activateWithCity() {
        val city = _state.value.selectedCity ?: return
        val durationHrs = city.distanceKm / if (city.isLocalTransport) 80.0 else 800.0
        val duration    = formatDuration(durationHrs)
        val timeDiff    = calculateTimeDiff(_state.value.userLng, city.lng)

        _state.update {
            it.copy(
                isActive         = true,
                destination      = city.nameAr,
                activeDistanceKm = city.distanceKm,
                activeCityLat    = city.lat,
                activeCityLng    = city.lng,
                // ✅ بنحفظ المدة النشطة هنا فقط
                activeDuration   = duration,
            )
        }
        fetchPrayerTimesForLocation(city.lat, city.lng, isSafar = true)

        NotificationHelper.showTravelNotification(context, city.nameAr, city.distanceKm)

        travelPrefs.saveTravelState(
            isActive    = true,
            destination = city.nameAr,
            distanceKm  = city.distanceKm,
            cityLat     = city.lat,
            cityLng     = city.lng,
            duration    = duration,
            timeDiff    = timeDiff
        )
    }
    private fun restoreTravelState() {
        if (!travelPrefs.isActive) return

        _state.update {
            it.copy(
                isActive         = true,
                destination      = travelPrefs.destination,
                activeDistanceKm = travelPrefs.distanceKm,
                activeCityLat    = travelPrefs.cityLat,
                activeCityLng    = travelPrefs.cityLng,
                activeDuration   = travelPrefs.duration,
                timeDiff         = travelPrefs.timeDiff,
            )
        }

        // استعادة أوقات الصلاة بالقصر
        fetchPrayerTimesForLocation(
            lat     = travelPrefs.cityLat,
            lng     = travelPrefs.cityLng,
            isSafar = true
        )

        // استعادة الإشعار الثابت
        NotificationHelper.showTravelNotification(
            context     = context,
            destination = travelPrefs.destination,
            distance    = travelPrefs.distanceKm
        )
    }

    fun toggleTravelMode() {
        val wasActive = _state.value.isActive

        _state.update {
            it.copy(
                isActive         = false,
                destination      = "",
                activeDistanceKm = 0,
                activeCityLat    = 0.0,
                activeCityLng    = 0.0,
            )
        }
        if (wasActive) {
            // ✅ ارجع لمواقيت موقع المستخدم بدون قصر
            val userLat = _state.value.userLat
            val userLng = _state.value.userLng
            if (userLat != 0.0 || userLng != 0.0) {
                fetchPrayerTimesForLocation(userLat, userLng, isSafar = false)
            }

            NotificationHelper.cancelTravelNotification(context)

            travelPrefs.clearTravelState()

        }
    }

    fun showChangeDestinationDialog() {
        _state.update { it.copy(showChangeDestDialog = true) }
    }

    fun dismissChangeDestinationDialog() {
        _state.update { it.copy(showChangeDestDialog = false) }
    }

    fun changeDestination() {
        val city = _state.value.selectedCity ?: return
        val durationHrs = city.distanceKm / if (city.isLocalTransport) 80.0 else 800.0
        val duration    = formatDuration(durationHrs)
        val timeDiff    = calculateTimeDiff(_state.value.userLng, city.lng)

        _state.update {
            it.copy(
                destination          = city.nameAr,
                showChangeDestDialog = false,
                activeDistanceKm     = city.distanceKm,
                activeCityLat        = city.lat,
                activeCityLng        = city.lng,
                // ✅ بنحدث المدة النشطة هنا فقط
                activeDuration       = duration,
            )
        }
        fetchPrayerTimesForLocation(city.lat, city.lng, isSafar = true)

        NotificationHelper.showTravelNotification(context, city.nameAr, city.distanceKm)

        travelPrefs.saveTravelState(
            isActive    = true,
            destination = city.nameAr,
            distanceKm  = city.distanceKm,
            cityLat     = city.lat,
            cityLng     = city.lng,
            duration    = duration,
            timeDiff    = timeDiff
        )

    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRAYER TIMES — يجيب مواقيت حسب الإحداثيات المطلوبة
    // ══════════════════════════════════════════════════════════════════════════

    // ✅ الصلوات الخمس فقط — نفلتر أي وقت تاني بيجي من الـ API
    private val FIVE_PRAYERS = setOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    private fun fetchPrayerTimesForLocation(lat: Double, lng: Double, isSafar: Boolean) {
        viewModelScope.launch {
            try {
                val authority = getCurrentPrayerTimesAuthorityUseCase()
                    .firstOrNull()
                    ?: run {
                        Log.w("TravelVM", "No authority found, using default")
                        return@launch
                    }

                val timingsList = getPrayerTimesUseCase(
                    lat  = lat,
                    lng = lng,
                    date      = LocalDate.now(),
                    school    = DomainPrayerTimingSchool(authority.id, authority.name)
                ).firstOrNull() ?: emptyList()

                val today = LocalDate.now().toString()
                val todayPrayers = timingsList
                    .filter { it.date == today }
                    // ✅ خمس صلوات بس — بنفلتر كل حاجة تانية بيجيبها الـ API
                    .filter { it.prayer.name.lowercase() in FIVE_PRAYERS }
                    .sortedBy { parseTimeForSorting(it.time) }

                if (todayPrayers.isEmpty()) {
                    Log.w("TravelVM", "No prayers found for today at ($lat, $lng)")
                    return@launch
                }

                val uiPrayers = todayPrayers.mapNotNull { timing ->
                    try {
                        val name = mapPrayerName(timing.prayer.name)
                        val time = parseDisplayTime(timing.time)
                        val rakaat = if (isSafar) getQasrRakaat(timing.prayer.name)
                        else getFullRakaat(timing.prayer.name)
                        val isShortened = isSafar && isQasrPrayer(timing.prayer.name)
                        val canMerge    = isSafar && canMergeWithNext(timing.prayer.name)
                        val mergeWith   = if (isSafar) getMergeTarget(timing.prayer.name) else null

                        TravelPrayer(
                            name       = name,
                            time       = time,
                            rakaat     = rakaat,
                            icon       = getPrayerIcon(timing.prayer.name),
                            color      = getPrayerColor(timing.prayer.name),
                            isShortened = isShortened,
                            canMerge   = canMerge,
                            mergeWith  = mergeWith
                        )
                    } catch (e: Exception) {
                        Log.e("TravelVM", "Error mapping prayer: ${e.message}")
                        null
                    }
                }

                _state.update { it.copy(prayerTimes = uiPrayers) }

            } catch (e: Exception) {
                Log.e("TravelVM", "Error fetching prayer times: ${e.message}")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FIQH DETAILS
    // ══════════════════════════════════════════════════════════════════════════

    fun toggleFiqhDetails() {
        _state.update { it.copy(showFiqhDetails = !it.showFiqhDetails) }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private fun buildFiqhResult(city: CityResult): FiqhResult {
        val isSafar = city.distanceKm >= SHAFII_TRAVEL_KM
        val details = if (isSafar) listOf(
            "✅ المسافة ${city.distanceKm} كم ≥ الحد الشرعي ${SHAFII_TRAVEL_KM.toInt()} كم",
            "✂️ يجوز قصر الظهر والعصر والعشاء إلى ركعتين",
            "🔗 يجوز جمع الظهر مع العصر، والمغرب مع العشاء",
            "🌙 يجوز الفطر في رمضان مع وجوب القضاء",
            "💧 يجوز المسح على الخفين 3 أيام بلياليها",
            "⚠️ إذا نويت الإقامة 4 أيام أو أكثر → أتمّ الصلاة"
        ) else listOf(
            "❌ المسافة ${city.distanceKm} كم < الحد الشرعي ${SHAFII_TRAVEL_KM.toInt()} كم",
            "لا تنطبق أحكام السفر على هذه المسافة في المذهب الشافعي"
        )
        return FiqhResult(isSafar = isSafar, distanceKm = city.distanceKm, details = details)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun calculateTimeDiff(userLng: Double, destLng: Double): String {
        val diffHours = ((destLng - userLng) / 15).toInt()
        return if (diffHours == 0) "0" else diffHours.toString()
    }

    private fun formatDuration(hours: Double): String {
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()
        return if (h == 0) "${m}د" else if (m == 0) "${h}س" else "${h}س ${m}د"
    }

    private fun mapPrayerName(prayerName: String): String = when (prayerName.lowercase()) {
        "fajr"    -> "الفجر"
        "sunrise" -> "الشروق"
        "dhuhr"   -> "الظهر"
        "asr"     -> "العصر"
        "maghrib" -> "المغرب"
        "isha"    -> "العشاء"
        "imsak"   -> "الإمساك"
        "midnight" -> "منتصف الليل"
        else      -> prayerName
    }

    private fun getPrayerIcon(name: String): ImageVector = when (name.lowercase()) {
        "fajr", "imsak", "sunrise" -> Icons.Default.WbTwilight
        "maghrib"                  -> Icons.Default.WbTwilight
        "isha", "midnight"         -> Icons.Default.NightsStay
        else                       -> Icons.Default.WbSunny
    }

    private fun getPrayerColor(name: String): Color = when (name.lowercase()) {
        "fajr"    -> Color(0xFF5C6BC0)
        "sunrise" -> Color(0xFFFF7043)
        "dhuhr"   -> Color(0xFFFF8F00)
        "asr"     -> Color(0xFFEF6C00)
        "maghrib" -> Color(0xFFE53935)
        "isha"    -> Color(0xFF283593)
        else      -> Color(0xFF9E9E9E)
    }

    private fun getQasrRakaat(name: String): Int = when (name.lowercase()) {
        "fajr"    -> 2
        "dhuhr"   -> 2  // قصر
        "asr"     -> 2  // قصر
        "maghrib" -> 3  // لا تُقصَر
        "isha"    -> 2  // قصر
        else      -> 2
    }

    private fun getFullRakaat(name: String): Int = when (name.lowercase()) {
        "fajr"    -> 2
        "dhuhr"   -> 4
        "asr"     -> 4
        "maghrib" -> 3
        "isha"    -> 4
        else      -> 4
    }

    private fun isQasrPrayer(name: String) =
        name.lowercase() in listOf("dhuhr", "asr", "isha")

    private fun canMergeWithNext(name: String) =
        name.lowercase() in listOf("dhuhr", "maghrib")

    private fun getMergeTarget(name: String): String? = when (name.lowercase()) {
        "dhuhr"   -> "العصر"
        "maghrib" -> "العشاء"
        else      -> null
    }

    private fun parseTimeForSorting(inputTime: String): LocalTime {
        return try {
            val timeStartIndex = inputTime.indexOf(':') - 2
            val timeEndIndex   = inputTime.indexOf('(') - 1
            val extracted      = inputTime.substring(timeStartIndex, timeEndIndex)
            LocalTime.parse(extracted, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.MIDNIGHT
        }
    }

    private fun parseDisplayTime(inputTime: String): String {
        return try {
            val timeStartIndex = inputTime.indexOf(':') - 2
            val timeEndIndex   = inputTime.indexOf('(') - 1
            val extracted      = inputTime.substring(timeStartIndex, timeEndIndex)
            val time           = LocalTime.parse(extracted, DateTimeFormatter.ofPattern("HH:mm"))
            time.format(DateTimeFormatter.ofPattern("hh:mm a"))
        } catch (e: Exception) {
            "—"
        }
    }
}