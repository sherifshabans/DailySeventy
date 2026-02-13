package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.di.WorkerEntryPoint
import com.elsharif.dailyseventy.domain.AppPreferences
import com.example.core.usecase.GetPrayerTimesUseCase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RegisterPrayerTimesWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            // 🔴 فحص إذن SCHEDULE_EXACT_ALARM
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.e("TimePrayer", "Cannot schedule exact alarms; permission denied")
                return Result.failure()
            }

            // 🔴 جديد: ألغِ كل الإشعارات القديمة أول حاجة
            cancelAllExistingAlarms()

            delay(300)


            val preferences = AppPreferences(applicationContext)
            val location = preferences.currentLocation
            val method = preferences.method

            val prayerTimes: GetPrayerTimesUseCase by lazy {
                val appContext = applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    WorkerEntryPoint::class.java
                )
                entryPoint.getPrayerTimesUseCase()
            }

            val prayerTimingsList = withContext(Dispatchers.IO) {
                val locationData = location.first()
                val methodData = method.first()
                Log.d("TimePrayer", "Fetching prayer times for location: (${locationData.first}, ${locationData.second}), method: ${methodData.name}")

                prayerTimes(locationData.first, locationData.second, LocalDate.now(), methodData).first()
            }


            val today = LocalDate.now()



            // 🔴 فلترة لليوم الحالي فقط
            val todayPrayers = prayerTimingsList.filter { it.date == today.toString() }

            Log.d("TimePrayer", "Total prayers for today: ${todayPrayers.size}")
            Log.d("TimePrayer", "Prayer names for today: ${todayPrayers.map { it.prayer.name }}")

            // الصلوات الخمس الأساسية فقط (بالأسماء الصحيحة)
            val mainPrayers = setOf("fajr", "dhuhr", "asr", "maghrib", "isha")

            Log.d("TimePrayer", "Total prayers: ${prayerTimingsList.size}")
            Log.d("TimePrayer", "Prayer names: ${prayerTimingsList.map { it.prayer.name }}")

            val filteredPrayers = todayPrayers.filter { prayerTiming ->
                val cleanName = if (prayerTiming.prayer.name.contains(":string/")) {
                    prayerTiming.prayer.name.substringAfter(":string/")
                } else {
                    prayerTiming.prayer.name
                }
                mainPrayers.contains(cleanName.lowercase())
            }

            Log.d("TimePrayer", "Filtered prayers: ${filteredPrayers.size}")

            // 🔴 فحص إذا كانت الإشعارات موجودة بالفعل
            if (filteredPrayers.isEmpty()) {
                Log.w("TimePrayer", "No prayers to schedule for today")
                return Result.success()
            }

            filteredPrayers.forEachIndexed { idx, prayerTiming ->
                val cleanName = if (prayerTiming.prayer.name.contains(":string/")) {
                    prayerTiming.prayer.name.substringAfter(":string/")
                } else {
                    prayerTiming.prayer.name
                }

                val imgId = context.resources.getIdentifier(
                    prayerTiming.prayer.imageId, "drawable", context.packageName
                )

                val nameRes = getPrayerNameResource(cleanName)
                val prayerTime = parseTime(prayerTiming.time)
                val prayerDate = prayerTiming.date
                val prayerName = context.getString(nameRes)

                Log.d("TimePrayer", "Setting alarm for: $prayerName at $prayerTime on $prayerDate")

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                val azanMillis = LocalDateTime
                    .from(formatter.parse("$prayerDate $prayerTime"))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                if (!isAlarmScheduled(context, idx, AzanAlarmReceiver::class.java)) {
                // Main Azan
                if (azanMillis > System.currentTimeMillis()) {
                    setAlarm(
                        AzanAlarmReceiver::class.java,
                        idx,
                        azanMillis,
                        imgId,
                        "${context.getString(R.string.prayer)} $prayerName",
                        context.getString(R.string.prayer_is_now),
                        type = "MAIN",
                        prayerName = prayerName
                    )

                }
                    // Pre-Azan (10 min before)
                    val preAzanMillis = azanMillis - (10 * 60 * 1000)
                    if (preAzanMillis > System.currentTimeMillis()
                        && !isAlarmScheduled(context, 1000 + idx, PreAndPostAzanAlarmReceiver::class.java)) {
                        setAlarm(
                            PreAndPostAzanAlarmReceiver::class.java,
                            1000 + idx,
                            preAzanMillis,
                            imgId,
                            "${context.getString(R.string.prayer)} $prayerName",
                            context.getString(R.string.prayer_is_in_10_minutes),
                            type = "PRE",
                            prayerName = prayerName
                        )
                    }

                    // Post-Azan (5 min for Maghrib, 20 min otherwise)
                    val delayMinutes = if (cleanName.lowercase() == "maghrib") 5 else 10
                    val postAzanMillis = azanMillis + (delayMinutes * 60 * 1000)
                    if (postAzanMillis > System.currentTimeMillis()
                        && !isAlarmScheduled(context, 2000 + idx, PreAndPostAzanAlarmReceiver::class.java)
                        ) {
                        setAlarm(
                            PreAndPostAzanAlarmReceiver::class.java,
                            2000 + idx,
                            postAzanMillis,
                            imgId,
                            "${context.getString(R.string.prayer)} $prayerName",
                            context.getString(R.string.after_prayer_reminder),
                            type = "POST",
                            prayerName = prayerName
                        )
                    }
                } else {
                    Log.d("TimePrayer", "Skipping past prayer: $prayerName")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("RegisterPrayerTimes", "Error: ${e.message}", e)
            Result.retry()
        }
    }

    // 🔴 دالة جديدة للتحقق من وجود إشعار مجدول
    private fun isAlarmScheduled(context: Context, requestCode: Int, receiverClass: Class<*>): Boolean {
        val intent = Intent(context, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    // 🔴 دالة جديدة: إلغاء كل الإشعارات المجدولة
    private fun cancelAllExistingAlarms() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // قائمة بكل requestCodes الممكنة للصلوات الخمس (idx 0-4)
        val requestCodes = mutableListOf<Int>()
        for (idx in 0..4) {
            requestCodes.add(idx)       // MAIN
            requestCodes.add(1000 + idx) // PRE
            requestCodes.add(2000 + idx) // POST
        }

        var cancelledCount = 0
        // لكل requestCode، أنشئ PendingIntent وألغيه
        requestCodes.forEach { code ->
            val mainIntent = Intent(context, AzanAlarmReceiver::class.java)
            val prePostIntent = Intent(context, PreAndPostAzanAlarmReceiver::class.java)

            val mainPending = PendingIntent.getBroadcast(
                context, code, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val prePostPending = PendingIntent.getBroadcast(
                context, code, prePostIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (mainPending != null) {
                alarmManager.cancel(mainPending)
                mainPending.cancel()
                cancelledCount++
            }
            if (prePostPending != null) {
                alarmManager.cancel(prePostPending)
                prePostPending.cancel()
                cancelledCount++
            }
            Log.d("TimePrayer", "Cancelled existing alarm for requestCode: $code")
        }
    }

    // دالة مساعدة لتحويل أسماء الصلوات إلى Resource IDs
    private fun getPrayerNameResource(prayerName: String): Int = when (prayerName.lowercase()) {
        "fajr" -> R.string.fajr
        "dhuhr" -> R.string.dhuhr
        "asr" -> R.string.asr
        "maghrib" -> R.string.maghrib
        "isha" -> R.string.isha
        else -> R.string.fajr // قيمة افتراضية
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(inputTime: String): String {
        val timeStartIndex = inputTime.indexOf(':') - 2
        val timeEndIndex = inputTime.indexOf('(') - 1
        val extractedTime = inputTime.substring(timeStartIndex, timeEndIndex)
        val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
        val time = LocalTime.parse(extractedTime, inputFormat)
        val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
        return outputFormat.format(time)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setAlarm(
        receiverClass: Class<*>,
        requestCode: Int,
        triggerAtMillis: Long,
        icon: Int,
        title: String,
        content: String,
        type: String,
        prayerName: String
    ) {
        val intent = Intent(context, receiverClass).apply {
            putExtra("ICON", icon)
            putExtra("TITLE", title)
            putExtra("CONTENT", content)
            putExtra("ID", requestCode)
            putExtra("TYPE", type)
            putExtra("PRAYER", prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d("TimePrayer", "Alarm set for $prayerName ($type) at ${java.util.Date(triggerAtMillis)}")
    }
}