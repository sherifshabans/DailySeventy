/* PrayerTimeViewModel.kt */

package com.elsharif.dailyseventy.presentaion.prayertimes

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsharif.dailyseventy.domain.friday.scheduleFridayReminders
import com.elsharif.dailyseventy.presentaion.prayertimes.model.UiPrayerTime
import com.elsharif.dailyseventy.presentaion.prayertimes.model.UiPrayerTimesAuthority
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

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

    val currentLocationFlow: Flow<GeoPoint> =
        getUserLocationUseCase().map { GeoPoint(it.first, it.second) }

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentDateFlow = MutableStateFlow(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val currentDateFlow = _currentDateFlow.asStateFlow()

    val prayerTimesAuthoritiesFlow: Flow<List<UiPrayerTimesAuthority>> =
        getPrayerTimesAuthoritiesUseCase().map { it.map { UiPrayerTimesAuthority(it.id, it.name) } }

    val currentPrayerAuthorityFlow: Flow<UiPrayerTimesAuthority> =
        getCurrentPrayerTimesAuthorityUseCase().map { UiPrayerTimesAuthority(it.id, it.name) }

    @RequiresApi(Build.VERSION_CODES.O)
    internal val prayerTimesFlow =
        combine(
            currentLocationFlow,
            currentDateFlow,
            currentPrayerAuthorityFlow
        ) { location, date, authority ->
            getPrayerTimesUseCase(
                location.latitude, location.longitude, date, DomainPrayerTimingSchool(
                    authority.idx, authority.name
                )
            ).single().filter { it.date == date.toString() }.map { prayerTiming: DomainPrayerTiming ->

                val imgId = context.resources.getIdentifier(
                    prayerTiming.prayer.imageId, "drawable",
                    context.packageName
                )

                val nameId = context.resources.getIdentifier(
                    prayerTiming.prayer.name, "string",
                    context.packageName
                )

                val extractedTime = parseTime(prayerTiming.time)
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

                UiPrayerTime(imgId, context.getString(nameId), extractedTime, formattedDuration)
            }
        }



    fun updateLocation(location: GeoPoint) = viewModelScope.launch {
        setUserLocationUseCase(location.latitude, location.longitude).collect()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDate(date: LocalDate) = viewModelScope.launch { _currentDateFlow.emit(date) }

    fun updateAuthority(prayerTimesAuthority: UiPrayerTimesAuthority) = viewModelScope.launch {
        setCurrentPrayerTimesAuthorityUseCase(
            DomainPrayerTimingSchool(prayerTimesAuthority.idx, prayerTimesAuthority.name)
        ).collect()
    }


    // 🔔 Schedule Friday Reminders using Dhuhr & Asr
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleFridayRemindersFromPrayerTimes() {
        viewModelScope.launch {
            prayerTimesFlow.collect { prayers ->
                val duhr = prayers.firstOrNull { it.name.contains("Dhuhr", true) }
                val asr = prayers.firstOrNull { it.name.contains("Asr", true) }

                if (duhr != null && asr != null) {
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val duhrTime = LocalTime.parse(duhr.time, formatter)
                    val asrTime = LocalTime.parse(asr.time, formatter)

                    scheduleFridayReminders(
                        context,
                        duhrTime.hour, duhrTime.minute,
                        asrTime.hour, asrTime.minute
                    )
                }
            }
        }
    }


    // for third times of night
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getNightPrayerTimes(): Pair<LocalTime, LocalTime>? {
        val prayersToday = runCatching {
            prayerTimesFlow.firstOrNull()
        }.getOrNull() ?: return null

        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        val maghribTime = prayersToday.firstOrNull { it.name.contains("Maghrib", true) }?.time
            ?.let { LocalTime.parse(it, formatter) }

        val fajrTime = prayersToday.firstOrNull { it.name.contains("Fajr", true) }?.time
            ?.let { LocalTime.parse(it, formatter) }

        if (maghribTime != null && fajrTime != null) {
            return Pair(maghribTime, fajrTime)
        }
        return null
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
}
