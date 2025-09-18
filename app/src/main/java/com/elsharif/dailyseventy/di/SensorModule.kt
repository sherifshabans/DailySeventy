package com.elsharif.dailyseventy.di

import android.app.Application
import com.elsharif.dailyseventy.domain.sensordomain.LightSensor
import com.elsharif.dailyseventy.domain.sensordomain.StepCounterSensor
import com.elsharif.dailyseventy.domain.sensordomain.StepDetectorSensor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module providing dependencies related to sensors.
 */
@Module
@InstallIn(SingletonComponent::class)
object SensorModule {


    /**
     * Provides a singleton instance of LightSensor.
     * @param app The application context.
     * @return An instance of LightSensor.
     */
    @Provides
    @Singleton
    fun provideLightSensor(app: Application): LightSensor {
        return LightSensor(app)
    }

    @Provides
    @Singleton
    fun provideStepCounterSensor(app: Application): StepCounterSensor {
        return StepCounterSensor(app)
    }

    @Provides
    @Singleton
    fun provideDetectorSensor(app: Application): StepDetectorSensor {
        return StepDetectorSensor(app)
    }


}