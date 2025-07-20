package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.di.WorkerEntryPoint
import com.elsharif.dailyseventy.domain.AppPreferences
import com.example.core.usecase.GetPrayerTimesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RegisterPrayerTimesWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted appContext: Context,
            @Assisted params: WorkerParameters
        ): RegisterPrayerTimesWorker
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "doWork: Starting prayer times registration")
            
            val preferences = AppPreferences(applicationContext)
            val location = preferences.currentLocation
            val method = preferences.method

            Log.d(TAG, "doWork: Got preferences, location: $location, method: $method")

            val prayerTimes: GetPrayerTimesUseCase by lazy {
                val appContext = applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(appContext, WorkerEntryPoint::class.java)
                entryPoint.getPrayerTimesUseCase()
            }

            val prayerTimings = combine(location, method) { locationValue, methodValue ->
                prayerTimes(
                    locationValue.first,
                    locationValue.second,
                    LocalDate.now(),
                    methodValue
                ).single()
            }

            val prayerTimingsList = withContext(Dispatchers.IO) {
                prayerTimings.first()
            }
            
            Log.d(TAG, "doWork: Got prayer timings: ${prayerTimingsList.size} prayers")

            prayerTimingsList.forEachIndexed { idx, prayerTiming ->
                Log.d(TAG, "Processing prayer: ${prayerTiming.prayer.name}, time: ${prayerTiming.time}, date: ${prayerTiming.date}")
                
                val imgId = context.resources.getIdentifier(
                    prayerTiming.prayer.imageId, "drawable",
                    context.packageName
                )

                val nameId = context.resources.getIdentifier(
                    prayerTiming.prayer.name, "string",
                    context.packageName
                )

                val prayerTime = parseTime(prayerTiming.time)
                val prayerDate = prayerTiming.date

                val prayerName = if (nameId != 0) {
                    context.getString(nameId)
                } else {
                    prayerTiming.prayer.name // Fallback
                }


                val prayerTag = "${prayerTiming.prayer.name} $prayerTime"
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                val dateTimeString = "$prayerDate $prayerTime"
                Log.d(TAG, "Parsing date time string: $dateTimeString")
                
                try {
                    val timeInMillis =
                        LocalDateTime
                            .from(formatter.parse(dateTimeString))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    Log.d(TAG, "Parsed time in millis: $timeInMillis")
                    Log.d(TAG, "Current time in millis: ${System.currentTimeMillis()}")

                    if (timeInMillis > System.currentTimeMillis()) {
                        Log.d(TAG, "Setting alarm for: $prayerTag at $timeInMillis")

                        setAlarm(
                            id = idx,
                            time = timeInMillis,
                            icon = imgId,
                            title = "${context.getString(R.string.prayer)} $prayerName " + context.getString(
                                R.string.prayer_is_now
                            ),
                            content = "${context.getString(R.string.prayer_is_now)} ${
                                context.getString(
                                    R.string.prayer_is_now
                                )
                            }"
                        )
                    } else {
                        Log.d(TAG, "Prayer time already passed: $prayerTag at $timeInMillis")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date time: ${e.message}")
                }
            }


            Log.d(TAG, "doWork: returning")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Error occurred: ${e.message}", e)
            Result.retry()
        }.also {
            Log.d(TAG, "doWork: $it")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(inputTime: String): String {
        Log.d(TAG, "parseTime: Input time string: $inputTime")
        
        val timeStartIndex = inputTime.indexOf(':') - 2
        val timeEndIndex = inputTime.indexOf('(') - 1
        
        Log.d(TAG, "parseTime: timeStartIndex: $timeStartIndex, timeEndIndex: $timeEndIndex")
        
        if (timeStartIndex < 0 || timeEndIndex <= timeStartIndex) {
            Log.e(TAG, "parseTime: Invalid time format, using fallback")
            return "12:00 PM"
        }
        
        val extractedTime = inputTime.substring(timeStartIndex, timeEndIndex)
        Log.d(TAG, "parseTime: Extracted time: $extractedTime")

        try {
            val inputFormat = DateTimeFormatter.ofPattern("HH:mm")
            val time = LocalTime.parse(extractedTime, inputFormat)

            val outputFormat = DateTimeFormatter.ofPattern("hh:mm a")
            val result = outputFormat.format(time)
            Log.d(TAG, "parseTime: Parsed result: $result")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "parseTime: Error parsing time: ${e.message}")
            return "12:00 PM"
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setAlarm(id: Int, time: Long, icon: Int, title: String, content: String) {
        Log.d(TAG, "setAlarm: Setting alarm for id=$id, time=$time, title=$title")
        runCatching {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
                putExtra("CONTENT", content)
                putExtra("TITLE", title)
                putExtra("ICON", icon)
                putExtra("ID", id)
            }


            val pi: PendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            Log.d(TAG, "setAlarm: id=$id, time=$time, icon=$icon, title=$title, content=$content")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
                Log.d(TAG, "setAlarm: Used setExactAndAllowWhileIdle for API ${Build.VERSION.SDK_INT}")
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pi)
                Log.d(TAG, "setAlarm: Used setExact for API ${Build.VERSION.SDK_INT}")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d(
                    TAG,
                    "registerPrayers:NEXT ${
                        ContextCompat.getSystemService(
                            context, AlarmManager::class.java
                        )?.nextAlarmClock?.triggerTime
                    } CAN ${alarmManager.canScheduleExactAlarms()}"
                )
            }
        }.onFailure {
            Log.d(TAG, "setAlarm: $it")
        }
    }

    companion object {
        private const val TAG = "RegisterPrayerTimes"
    }

    private fun Context.cancelAlarm(requestCode: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, requestCode,
            Intent(this, AzanAlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager?.cancel(pendingIntent)
    }
}
