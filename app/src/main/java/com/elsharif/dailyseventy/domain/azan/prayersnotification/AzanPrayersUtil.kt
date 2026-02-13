/*
package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

object AzanPrayersUtil {

    private const val TAG = "AzanPrayersUtil"
    private const val WORK_NAME = "REGISTER_PRAYERS"
    private const val WORK_TAG = "REGISTER_PRAYERS_TAG"

    fun registerPrayers(context: Context) {
        Log.d(TAG, "Registering prayer alarms for immediate scheduling")

        // 🔴 إلغاء الـ Workers المتعلقة بالصلوات فقط
        WorkManager.getInstance(context.applicationContext)
            .cancelAllWorkByTag(WORK_TAG)

        // 🔴 إنشاء OneTimeWorkRequest لتشغيل فوري
        val registerRequest =
            PeriodicWorkRequest.Builder(RegisterPrayerTimesWorker::class.java, 1, TimeUnit.DAYS)
            .addTag(WORK_TAG)
            .build()

        // 🔴 استخدام REPLACE لضمان استبدال أي Worker قديم
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                registerRequest
            )

        Log.d(TAG, "Enqueued unique one-time work for prayer alarms")
    }
}*/

package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object AzanPrayersUtil {

    private const val TAG = "AzanPrayersUtil"
    private const val WORK_NAME_IMMEDIATE = "REGISTER_PRAYERS_NOW"
    private const val WORK_NAME_DAILY = "REGISTER_PRAYERS_DAILY"
    private const val WORK_TAG = "REGISTER_PRAYERS_TAG"
    private const val DAILY_ALARM_REQUEST_CODE = 9999

    /**
     * 🔴 جدولة فورية - استخدمها عند تغيير الموقع أو فتح التطبيق
     */
    fun registerPrayersImmediately(context: Context) {
        Log.d(TAG, "🔄 Cancelling old alarms and scheduling new ones immediately")

        // 1️⃣ ألغِ كل الـ Workers القديمة
        cancelAllPrayerWorkers(context)

        // 2️⃣ جدولة فورية
        val immediateRequest = OneTimeWorkRequest.Builder(RegisterPrayerTimesWorker::class.java)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(
                WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )

        Log.d(TAG, "✅ Immediate prayer alarms scheduled")
    }

    /**
     * 🔵 جدولة يومية تلقائية - استخدمها مرة واحدة في Application.onCreate()
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setupDailyPrayerUpdates(context: Context) {
        Log.d(TAG, "📅 Setting up daily prayer updates")

        // 1️⃣ WorkManager (الطريقة الأساسية)
        setupWorkManagerDaily(context)

        // 2️⃣ AlarmManager (Backup للضمان)
        setupAlarmManagerDaily(context)
    }

    /**
     * WorkManager للجدولة اليومية
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupWorkManagerDaily(context: Context) {
        val dailyRequest = PeriodicWorkRequest.Builder(
            RegisterPrayerTimesWorker::class.java,
            1, TimeUnit.DAYS
        )
            .addTag(WORK_TAG)
            .setInitialDelay(calculateDelayUntilMidnight(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                WORK_NAME_DAILY,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )

        Log.d(TAG, "✅ WorkManager daily updates scheduled")
    }

    /**
     * ⚡ AlarmManager كـ Backup (أقوى من WorkManager)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAlarmManagerDaily(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ تحقق من الإذن (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "⚠️ Cannot schedule exact alarms, using WorkManager only")
            return
        }

        val intent = Intent(context, DailyPrayerUpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 🕛 حساب منتصف الليل القادم
        val midnight = calculateMidnightInMillis()

        // 🔁 جدولة يومية (كل 24 ساعة)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            midnight,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d(TAG, "✅ AlarmManager daily backup scheduled")
    }

    /**
     * إلغاء كل الـ Workers
     */
    private fun cancelAllPrayerWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(WORK_NAME_IMMEDIATE)
        workManager.cancelAllWorkByTag(WORK_TAG)
        Log.d(TAG, "❌ All prayer workers cancelled")
    }

    /**
     * 🔧 حساب الوقت المتبقي لمنتصف الليل
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDelayUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return Duration.between(now, midnight).toMillis()
    }

    /**
     * 🔧 حساب منتصف الليل بالـ milliseconds
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateMidnightInMillis(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return midnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}