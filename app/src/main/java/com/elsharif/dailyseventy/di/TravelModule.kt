package com.elsharif.dailyseventy.di



import android.content.Context
import com.elsharif.dailyseventy.domain.data.travel.TravelDao
import com.elsharif.dailyseventy.domain.data.travel.TravelDatabase
import com.elsharif.dailyseventy.domain.repository.TravelRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TravelModule {

    @Provides
    @Singleton
    fun provideTravelDatabase(@ApplicationContext context: Context): TravelDatabase {
        return TravelDatabase.getInstance(context)
    }

    @Provides
    fun provideTravelDao(database: TravelDatabase): TravelDao {
        return database.travelDao()
    }

    @Provides
    @Singleton
    fun provideTravelRepository(travelDao: TravelDao): TravelRepository {
        return TravelRepository(travelDao)
    }
}