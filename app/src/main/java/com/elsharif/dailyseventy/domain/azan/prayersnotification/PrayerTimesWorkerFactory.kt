package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PrayerTimesWorkerFactory @AssistedInject constructor(
    private val registerPrayerTimesWorkerFactory: RegisterPrayerTimesWorker.Factory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RegisterPrayerTimesWorker::class.java.name ->
                registerPrayerTimesWorkerFactory.create(appContext, workerParameters)
            else -> null
        }
    }
} 