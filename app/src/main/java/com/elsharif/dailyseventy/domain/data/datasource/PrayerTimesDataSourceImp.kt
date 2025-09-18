package com.elsharif.dailyseventy.domain.data.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.domain.azan.local.database.PrayerTimesDao
import com.elsharif.dailyseventy.domain.azan.local.toDomain
import com.elsharif.dailyseventy.domain.azan.local.toEntity
import com.elsharif.dailyseventy.domain.azan.prayermethods.PrayerTimingMethods
import com.elsharif.dailyseventy.domain.azan.prayertimes.PrayerTiming
import com.elsharif.dailyseventy.domain.azan.prayertimes.Timings
import com.elsharif.dailyseventy.domain.azan.remote.PrayerAPI
import com.example.core.data.datasource.PrayerTimesDatasource
import com.example.core.domain.prayertiming.DomainPrayer
import com.example.core.domain.prayertiming.DomainPrayerTiming
import com.example.core.domain.prayertiming.DomainPrayerTimingSchool
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

private const val TAG = "PrayerTimesDataSourceImp"

class PrayerTimesDataSourceImp @Inject constructor(
    private val preferences: AppPreferences,
    private val api: PrayerAPI,
    private val dao: PrayerTimesDao,
    @ApplicationContext private val context: Context,
) : PrayerTimesDatasource {

    override fun getUserLocation(): Flow<Pair<Double, Double>> = preferences.currentLocation

    override fun setUserLocation(lat: Double, lng: Double): Flow<Boolean> = flow {
        preferences.setLocation(Pair(lat, lng))
        emit(true)
    }

    override fun getPrayerTimeAuthorities(): Flow<List<DomainPrayerTimingSchool>> = flow {
        val response = api.prayerTimesMethods()
        val data = response.data ?: return@flow emit(emptyList())

        val prayerTimingMethods = PrayerTimingMethods(data)
        Log.i(TAG, "getPrayerTimeAuthorities: $data")

        val prayerTimingSchools = prayerTimingMethods.methods.filterNotNull().map {
            DomainPrayerTimingSchool(it.id, it.name ?: "")
        }

        emit(prayerTimingSchools)
    }

    override fun getCurrentPrayerTimesAuthority(): Flow<DomainPrayerTimingSchool> = preferences.method

    override fun setPrayerTimesAuthority(school: DomainPrayerTimingSchool): Flow<Boolean> = flow {
        preferences.setMethod(school)
        emit(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getPrayerTimes(
        lat: Double,
        lng: Double,
        date: LocalDate,
        school: DomainPrayerTimingSchool
    ): Flow<List<DomainPrayerTiming>> = flow {

        // دالة تحويل محسنة
        fun convertFromTimings(timings: Timings, dateString: String): List<DomainPrayerTiming> = listOf(
            // الصلوات الأساسية
            DomainPrayerTiming(
                DomainPrayer("fajr", "fajr"),
                timings.fajr,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("sunrise", "duha"),
                timings.sunrise,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("dhuhr", "dhuhr"),
                timings.dhuhr,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("asr", "asr"),
                timings.asr,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("maghrib", "maghrib"),
                timings.maghrib,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("isha", "isha"),
                timings.isha,
                dateString, lat, lng, school
            ),
            // الأوقات الإضافية
            DomainPrayerTiming(
                DomainPrayer("imsak", "doaa"),
                timings.imsak,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("sunset", "doaa"),
                timings.sunset,
                dateString, lat, lng, school
            ),
            DomainPrayerTiming(
                DomainPrayer("midnight", "doaa"),
                timings.midnight,
                dateString, lat, lng, school
            )
        ).filter { it.time.isNotBlank() } // فلترة الأوقات الفاضية

        val dateString = date.toString()

        // نحاول نجيب البيانات من الكاش أولاً
        val cached = dao.getPrayerTimesForDay(dateString, lat, lng, school.id)
        if (cached.isNotEmpty()) {
            emit(cached.distinctBy { "${it.date}_${it.name}" }.map { it.toDomain(school) })
            return@flow
        }

        // إذا مش موجود في الكاش، نجيب من API
        val response = api.getPrayerTimes(lat, lng, school.id, date.monthValue, date.year)
        val symbols = DecimalFormatSymbols(Locale.US)

        response.data?.forEach {
            Log.d(TAG, "API timings for ${it.date.gregorian?.date}: ${it.timings}")
        }

        val timingsList = response.data?.map {
            val gregorian = it.date.gregorian
            "${gregorian?.year}-${DecimalFormat("00", symbols).format(gregorian?.month?.number)}-${gregorian?.day}" to it.timings
        }

        // تحويل البيانات مباشرة
        val domainPrayers = timingsList?.flatMap { (dateStr, timings) ->
            convertFromTimings(timings, dateStr)
        } ?: listOf()

        Log.d(TAG, "Total prayers before deduplication: ${domainPrayers.size}")
        Log.d(TAG, "Prayer names: ${domainPrayers.map { it.prayer.name }.distinct()}")
        Log.d(TAG, "All prayer details: ${domainPrayers.map { "${it.prayer.name}: ${it.time}" }}")

        // إزالة التكرارات باستخدام مفتاح أوضح
        val domainPrayersUnique = domainPrayers.distinctBy { "${it.date}_${it.prayer.name}" }

        Log.d(TAG, "Total prayers after deduplication: ${domainPrayersUnique.size}")

        // نخزن النتائج في الكاش
        dao.insertAll(domainPrayersUnique.map { it.toEntity() })

        emit(domainPrayersUnique)
    }
}