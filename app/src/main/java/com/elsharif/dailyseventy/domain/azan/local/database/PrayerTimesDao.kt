package com.elsharif.dailyseventy.domain.azan.local.database

import androidx.room.*
import com.elsharif.dailyseventy.domain.azan.local.model.PrayerTimesEntity

@Dao
interface PrayerTimesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prayers: List<PrayerTimesEntity>)

    @Query("SELECT * FROM prayer_times WHERE date = :date AND lat = :lat AND lng = :lng AND schoolId = :schoolId")
    suspend fun getPrayerTimesForDay(
        date: String,
        lat: Double,
        lng: Double,
        schoolId: Int
    ): List<PrayerTimesEntity>
}
