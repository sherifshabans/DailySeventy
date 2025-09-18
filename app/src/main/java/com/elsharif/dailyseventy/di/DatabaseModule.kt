package com.elsharif.dailyseventy.di

import android.content.Context
import androidx.room.Room
import com.elsharif.dailyseventy.domain.azan.local.database.AppDatabase
import com.elsharif.dailyseventy.domain.azan.local.database.PrayerTimesDao
import com.elsharif.dailyseventy.domain.data.database.quran.QuranDao
import com.elsharif.dailyseventy.domain.data.database.quran.QuranDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuranDatabase {
        return Room.databaseBuilder(
            context,
            QuranDatabase::class.java,
            "quran.db" // name used for internal storage
        )
            .createFromAsset("quran/databases/quran.db") // correct path inside assets folder
            .fallbackToDestructiveMigration() // optional
            .build()
    }

    @Provides
    fun provideQuranDao(database: QuranDatabase): QuranDao {
        return database.quranDao()
    }

    @Provides
    fun providePrayerDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prayer_times_db"
        )
            .fallbackToDestructiveMigration() // هذا يمسح البيانات القديمة ويعمل قاعدة جديدة
            .build()
    }

    @Provides
    fun providePrayerTimesDao(database: AppDatabase): PrayerTimesDao {
        return database.prayerTimesDao()
    }
}
