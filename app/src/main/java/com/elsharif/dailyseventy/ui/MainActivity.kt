package com.elsharif.dailyseventy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.AppPreferences
import com.elsharif.dailyseventy.domain.azan.prayersnotification.AzanPrayersUtil
import com.elsharif.dailyseventy.domain.dailyazkar.AzkarWorker
import com.elsharif.dailyseventy.domain.data.sharedpreferences.FridayPrefs
import com.elsharif.dailyseventy.domain.data.sharedpreferences.IslamicReminderPreferences
import com.elsharif.dailyseventy.domain.data.sharedpreferences.NightThirdPrefs
import com.elsharif.dailyseventy.domain.data.sharedpreferences.ThemePreferences
import com.elsharif.dailyseventy.domain.islamicReminder.IslamicReminderManager
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
import java.util.Calendar
import java.util.Locale
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

    @Inject
    lateinit var appPreferences: AppPreferences

    private val prayerTimeViewModel: PrayerTimeViewModel by viewModels()

    private lateinit var reminderManager: IslamicReminderManager
    private lateinit var preferences: IslamicReminderPreferences



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { updateContextWithLanguage(it) })
    }

    private fun updateContextWithLanguage(context: Context): Context {
        val prefs = context.getSharedPreferences("PRAYERS_PREF", MODE_PRIVATE)
        val languageCode = prefs.getString("LANGUAGE_PREF", "ar") ?: "ar"

        Log.d("MainActivity", "Applying language: $languageCode")

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }




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

            reminderManager = IslamicReminderManager(this@MainActivity)
            preferences = IslamicReminderPreferences(this@MainActivity)
            reminderManager.setupPeriodicReminders()

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

                AppNavHost(
                    context = context,
                    themeViewModel = themeViewModel,
                    navController = navController,
                    prayerTimeViewModel = prayerTimeViewModel
                )
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

        WorkManager.Companion.getInstance(this).enqueueUniquePeriodicWork(
            "zekr_work", // اسم فريد
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }



    private fun scheduleAzkarWork() {
        val azkarTimes = listOf(
            Triple("morning", 9, 0),
            Triple("evening", 18, 30),
            Triple("night", 0, 0)
        )

        val workManager = WorkManager.Companion.getInstance(applicationContext)

        azkarTimes.forEach { (type, hour, minute) ->
            val now = Calendar.getInstance()
            val scheduleTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
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


    private fun applyCurrentLanguage() {
        try {
            val languageCode = appPreferences.getSavedLanguageCode()
            Log.d("MainActivity", "Applying current language: $languageCode")

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)

            // Also apply via AppCompatDelegate
            val locales = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(locales)

        } catch (e: Exception) {
            Log.e("MainActivity", "Error applying language: ${e.message}")
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

            // ✅ اشغل تذكيرات الأثلاث بس لو متفعلة
            if (NightThirdPrefs.isEnabled(this)) {
                val selection = NightThirdPrefs.getSelection(this)
                if (selection.isNotEmpty()) {
                    prayerTimeViewModel.scheduleNightThirdNotificationsFromPrayerTimes(
                        this, selection
                    )
                }
            }


            prayerTimeViewModel.scheduleSunriseAzkar(applicationContext)
        }

        // Check if language changed and recreate if needed
        val currentLanguage = Locale.getDefault().language
        val savedLanguage = appPreferences.getSavedLanguageCode()

        if (currentLanguage != savedLanguage) {
            Log.d("MainActivity", "Language mismatch detected. Recreating...")
            applyCurrentLanguage()
            recreate()
        }

    }

}