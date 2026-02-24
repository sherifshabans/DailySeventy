package com.elsharif.dailyseventy.domain.data.preferences


import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("travel_prefs", Context.MODE_PRIVATE)

    // ── حفظ
    fun saveTravelState(
        isActive    : Boolean,
        destination : String,
        distanceKm  : Int,
        cityLat     : Double,
        cityLng     : Double,
        duration    : String,
        timeDiff    : String
    ) {
        prefs.edit()
            .putBoolean("is_active",    isActive)
            .putString ("destination",  destination)
            .putInt    ("distance_km",  distanceKm)
            .putFloat  ("city_lat",     cityLat.toFloat())
            .putFloat  ("city_lng",     cityLng.toFloat())
            .putString ("duration",     duration)
            .putString ("time_diff",    timeDiff)
            .apply()
    }

    // ── مسح
    fun clearTravelState() {
        prefs.edit().clear().apply()
    }

    // ── قراءة
    val isActive     get() = prefs.getBoolean("is_active",   false)
    val destination  get() = prefs.getString ("destination", "") ?: ""
    val distanceKm   get() = prefs.getInt    ("distance_km", 0)
    val cityLat      get() = prefs.getFloat  ("city_lat",    0f).toDouble()
    val cityLng      get() = prefs.getFloat  ("city_lng",    0f).toDouble()
    val duration     get() = prefs.getString ("duration",    "") ?: ""
    val timeDiff     get() = prefs.getString ("time_diff",   "") ?: ""
}