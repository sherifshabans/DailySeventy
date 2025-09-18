package com.elsharif.dailyseventy.domain.azan.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.elsharif.dailyseventy.domain.azan.local.model.PrayerTimesEntity

@Database(
    entities = [PrayerTimesEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerTimesDao(): PrayerTimesDao
}
