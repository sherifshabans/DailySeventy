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
import com.elsharif.dailyseventy.domain.preAzan.PreAzanAlarmReceiver
import com.example.core.usecase.GetPrayerTimesUseCase
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

class RegisterPrayerTimesWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
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

            val timingsFlow = combine(location, method) { loc, meth ->
                prayerTimes(loc.first, loc.second, LocalDate.now(), meth).single()
            }

            val prayerTimingsList = withContext(Dispatchers.IO) { timingsFlow.first() }

            prayerTimingsList.forEachIndexed { idx, prayerTiming ->
                val imgId = context.resources.getIdentifier(
                    prayerTiming.prayer.imageId, "drawable", context.packageName
                )

                val nameId = context.resources.getIdentifier(
                    prayerTiming.prayer.name, "string", context.packageName
                )

                val prayerTime = parseTime(prayerTiming.time)
                val prayerDate = prayerTiming.date
                val prayerName = if (nameId != 0) context.getString(nameId) else prayerTiming.prayer.name

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                val azanMillis = LocalDateTime
                    .from(formatter.parse("$prayerDate $prayerTime"))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // Main Azan
                if (azanMillis > System.currentTimeMillis()) {
                    setAlarm(
                        AzanAlarmReceiver::class.java,
                        idx,
                        azanMillis,
                        imgId,
                        "${context.getString(R.string.prayer)} $prayerName",
                        context.getString(R.string.prayer_is_now)
                    )
                }

                // Pre-Azan (10 min before)
                val preAzanMillis = azanMillis - (10 * 60 * 1000)
                if (preAzanMillis > System.currentTimeMillis()) {
                    setAlarm(
                        PreAzanAlarmReceiver::class.java,
                        1000 + idx,
                        preAzanMillis,
                        imgId,
                        "${context.getString(R.string.prayer)} $prayerName",
                        context.getString(R.string.prayer_is_in_10_minutes)
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("RegisterPrayerTimes", "Error: ${e.message}", e)
            Result.retry()
        }
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
        id: Int,
        time: Long,
        icon: Int,
        title: String,
        content: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, receiverClass).apply {
            putExtra("CONTENT", content)
            putExtra("TITLE", title)
            putExtra("ICON", icon)
            putExtra("ID", id)
        }

        val pi = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pi)
        }
    }
}
