package com.elsharif.dailyseventy.domain.data.database.quran

import androidx.room.Database
import androidx.room.RoomDatabase
import com.elsharif.dailyseventy.domain.data.database.model.DatabaseAya


@Database(entities = [

    DatabaseAya::class], version = 1, exportSchema = false)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao

}