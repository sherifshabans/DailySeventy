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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.domain.azan.prayersnotification.AzanPrayersUtil
import com.elsharif.dailyseventy.domain.data.shardprefernces.ThemePreferences
import com.elsharif.dailyseventy.domain.zekr.ZekkrAlarmUtil
import com.elsharif.dailyseventy.presentaion.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.ui.theme.DailySeventyTheme
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Navigation.AppNavHost
import com.elsharif.dailyseventy.util.Permissions.requestExactAlarmPermission
import com.elsharif.dailyseventy.util.Permissions.requestNotificationPermission
import com.elsharif.dailyseventy.util.setCurrentLanguage
import com.elsharif.dailyseventy.util.workmanager.LocationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val locationManager by lazy {
        LocationManager(applicationContext)
    }

    private val themeViewModel by viewModels<ThemeViewModel> {
        val prefs = ThemePreferences(applicationContext)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ThemeViewModel(prefs) as T
            }
        }
    }

    @Inject lateinit var appPreferences: AppPreferences

    // Friday ViewModel
    private val prayerTimeViewModel: PrayerTimeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )



    @SuppressLint("NewApi")
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
        requestExactAlarmPermission(this)
        requestNotificationPermission(this)

        setContent {
            DailySeventyTheme(
                userPrimary = themeViewModel.userColor.value
            ) {

                val navController = rememberNavController()

                val context = LocalContext.current

                AppNavHost(context = context, themeViewModel = themeViewModel,navController= navController)
            }
        }
    }

    private fun registerZekr(){

        ZekkrAlarmUtil.setRepeatingZekkrNotification(
            context = this,
            title = "وقت النبي ﷺ",
            content = "المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم، فإنّ المحبّ لا يغفل عن حبيبه...",
            iconResId = R.drawable.doaa // or R.mipmap.ic_launcher
        )
    }




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


    @SuppressLint("BatteryLife", "UseKtx", "ObsoleteSdkInt")
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


    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        registerPrayersAzan()
        registerZekr()

        prayerTimeViewModel.scheduleFridayRemindersFromPrayerTimes()

    }

}
