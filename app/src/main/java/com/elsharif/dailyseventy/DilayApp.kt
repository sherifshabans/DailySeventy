package com.elsharif.dailyseventy

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.util.UseCaseProvider
import com.elsharif.dailyseventy.domain.azan.prayersnotification.updateAzanChannel
import com.example.core.usecase.GetQuranPageAyaWithTafseerUseCase
import com.example.core.usecase.GetSoraByPageNumberUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import net.time4j.android.ApplicationStarter
import javax.inject.Inject

@HiltAndroidApp
class DilayApp : Application() {

    @Inject
    lateinit var appPreferences: AppPreferences


    override fun onCreate() {
        super.onCreate()


        // Initialize Time4J
        ApplicationStarter.initialize(this, true)


        // Create notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updateAzanChannel(this) // هنا بنربط القناة مع الصوت المحفوظ
        }

        // Use manual entry point if needed
        val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)
        UseCaseProvider.init(
            getSora = entryPoint.getSora(),
            getQuran = entryPoint.getQuran()
        )


        // Initialize language settings
        initializeLanguage()

        // Initialize dark mode settings
        initializeDarkMode()

    }

    private fun initializeLanguage() {
        try {
            Log.d("DilayApp", "Initializing language...")
            appPreferences.initializeLanguage()
            Log.d("DilayApp", "Language initialized successfully")
        } catch (e: Exception) {
            Log.e("DilayApp", "Error initializing language: ${e.message}")
        }
    }

    private fun initializeDarkMode() {
        try {
            val isDarkMode = appPreferences.isDarkModeEnabled()
            Log.d("DilayApp", "Dark mode enabled: $isDarkMode")
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        } catch (e: Exception) {
            Log.e("DilayApp", "Error initializing dark mode: ${e.message}")
        }
    }

}


@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun getSora(): GetSoraByPageNumberUseCase
    fun getQuran(): GetQuranPageAyaWithTafseerUseCase
}
