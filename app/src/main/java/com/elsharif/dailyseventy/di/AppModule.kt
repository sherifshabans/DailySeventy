package com.elsharif.dailyseventy.di

import android.content.Context
import com.elsharif.dailyseventy.domain.repository.ZekrRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAzkarRepository(
        @ApplicationContext context: Context
    ):ZekrRepository {
        return ZekrRepository(context)
    }


}