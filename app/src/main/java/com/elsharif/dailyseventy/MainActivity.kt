package com.elsharif.dailyseventy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.domain.azan.prayersnotification.AzanPrayersUtil
import com.elsharif.dailyseventy.domain.dailyazkar.AzkarWorker
import com.elsharif.dailyseventy.domain.data.sharedpreferences.FridayPrefs
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThird
import com.elsharif.dailyseventy.domain.data.sharedpreferences.ThemePreferences
import com.elsharif.dailyseventy.domain.zekr.ZekrWorker
import com.elsharif.dailyseventy.presentation.prayertimes.PrayerTimeViewModel
import com.elsharif.dailyseventy.ui.theme.DailySeventyTheme
import com.elsharif.dailyseventy.ui.theme.ThemeViewModel
import com.elsharif.dailyseventy.util.Navigation.AppNavHost
import com.elsharif.dailyseventy.util.Permissions.requestExactAlarmPermission
import com.elsharif.dailyseventy.util.Permissions.requestNotificationPermission
import com.elsharif.dailyseventy.util.Permissions.requestSensorPermission
import com.elsharif.dailyseventy.util.setCurrentLanguage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {



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

    private val prayerTimeViewModel: PrayerTimeViewModel by viewModels()


    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var initialSteps = -1

    //  private val tasbeehViewModel : TasbeehViewModel by viewModels ()


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
/*


        /// For overlay
        // طلب إذن draw over other apps لو مش موجود
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent) // المستخدم يعطي الإذن يدوياً
        } else {
            // لو الإذن موجود وحالة OverlayPrefs مفعلة نبدأ الخدمة فوراً
            if (OverlayPrefs.isEnabled(this)) {
                val svcIntent = Intent(this, OverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(svcIntent)
                } else {
                    startService(svcIntent)
                }
            }
        }
*/

        val context = applicationContext

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        Log.d("StepSensor", "stepCounter = $stepCounter")

        enableEdgeToEdge()
        requestIgnoreBatteryOptimization()
        requestExactAlarmPermission(this)
        requestNotificationPermission(this)
        // للتأكد إننا فعلاً فعلنا المنبه في preferences (لأن scheduleStepAlarm يبدأ فقط لو enabled)
    //    AlarmPreferences.setAlarmEnabled(this, true)
        Log.d("MainActivityDebug", "DEBUG: AlarmPreferences.setAlarmEnabled -> true")

// اطبع حالة إمكانية exact alarms
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
        Log.d("MainActivityDebug", "DEBUG: canScheduleExactAlarms = $canExact")

// نفّذ اختبار سريع (سيجعل المنبه يرن بعد 30 ثانية)
        //AlarmScheduler.scheduleImmediateTest(this, 30_000L)
        Log.d("MainActivityDebug", "DEBUG: Called scheduleImmediateTest(30s)")
        setContent {
            DailySeventyTheme(
                userPrimary = themeViewModel.userColor.value
            ) {

                val navController = rememberNavController()

                val context = LocalContext.current

                AppNavHost(context = context, themeViewModel = themeViewModel,navController= navController, prayerTimeViewModel = prayerTimeViewModel)
            }
        }
        Log.d("PermissionCheck", "Requesting ACTIVITY_RECOGNITION permission...")
        requestSensorPermission(this)



    }

    private fun registerZekr() {
        val workRequest = PeriodicWorkRequestBuilder<ZekrWorker>(
            15, TimeUnit.MINUTES // ⏰ الحد الأدنى المسموح في WorkManager
        ).setInputData(
            workDataOf(
                "TITLE" to "وقت الصلاة على النبي ﷺ",
                "CONTENT" to "المُحبّ ينبغي أن لا يتركَ وردَ الصَّلاة والسّلام على سيدنا رسولِ الله صلى الله عليه وسلّم، فإنّ المحبّ لا يغفل عن حبيبه...",
                "ICON" to R.drawable.doaa,
                "ID" to 1001
            )
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "zekr_work", // اسم فريد
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }



    private fun scheduleAzkarWork() {
        val azkarTimes = listOf(
            Triple("morning", 9, 0),
            Triple("evening", 18, 30),
            Triple("night", 0, 0)
        )

        val workManager = WorkManager.getInstance(applicationContext)

        azkarTimes.forEach { (type, hour, minute) ->
            val now = java.util.Calendar.getInstance()
            val scheduleTime = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DAY_OF_MONTH, 1)
            }

            val initialDelay = scheduleTime.timeInMillis - now.timeInMillis

            val workRequest = OneTimeWorkRequestBuilder<AzkarWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("type" to type))
                .build()

            workManager.enqueueUniqueWork(
                "azkar_$type",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
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
        scheduleAzkarWork()

        val kahfEnabled = FridayPrefs.loadKahf(this)
        val asrEnabled = FridayPrefs.loadAsr(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prayerTimeViewModel.scheduleFridayReminders(
                context = this,
                kahfEnabled = kahfEnabled,
                asrEnabled = asrEnabled
            )

            prayerTimeViewModel.scheduleNightThirdNotificationsFromPrayerTimes(
                applicationContext,
                setOf(NightThird.FIRST, NightThird.SECOND, NightThird.THIRD)
            )

            prayerTimeViewModel.scheduleSunriseAzkar(applicationContext)
        }
    }

}
