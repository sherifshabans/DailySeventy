package com.elsharif.dailyseventy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.ui.theme.DailySeventyTheme
import com.elsharif.dailyseventy.util.Navigation.UnifiedNavigationScaffold
import com.elsharif.dailyseventy.util.setCurrentLanguage
import com.elsharif.dailyseventy.util.workmanager.LocationManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.CustomInjection.inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.elsharif.dailyseventy.domain.azan.prayersnotification.AzanPrayersUtil
import com.elsharif.dailyseventy.domain.azan.prayersnotification.PrayerTimesTestUtil

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val locationManager by lazy {
        LocationManager(applicationContext)
    }


    @Inject lateinit var appPreferences: AppPreferences


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )





    private fun registerPrayersAzan() {

        AzanPrayersUtil.registerPrayers(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    this.startActivity(intent)
                }
            }
        }
    }


    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this, permissions, 100
        )
        MainScope().launch {
            appPreferences.isFirstTime.collect {
                if (it) {
                    setCurrentLanguage("ar")
                    AppPreferences(this@MainActivity).setIsFirstTime()
                }
            }
        }

        enableEdgeToEdge()
        requestIgnoreBatteryOptimization()
        registerPrayersAzan()

        setContent {
            DailySeventyTheme {

                val context = LocalContext.current

                UnifiedNavigationScaffold(context)
                
                // Add test buttons for debugging (remove in production)
                androidx.compose.material3.Button(
                    onClick = { 
                        PrayerTimesTestUtil.testPrayerTimesWorker(context)
                    }
                ) {
                    androidx.compose.material3.Text("Test Prayer Worker")
                }
                
                androidx.compose.material3.Button(
                    onClick = { 
                        PrayerTimesTestUtil.testAlarmManager(context)
                    }
                ) {
                    androidx.compose.material3.Text("Test Alarm Manager")
                }
            }
        }
    }

}
