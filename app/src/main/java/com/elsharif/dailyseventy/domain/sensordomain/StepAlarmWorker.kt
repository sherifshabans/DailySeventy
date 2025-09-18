package com.elsharif.dailyseventy.domain.sensordomain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.elsharif.dailyseventy.domain.data.sharedpreferences.AlarmPreferences
import java.util.Calendar


object AlarmScheduler {
    private const val ALARM_REQUEST_CODE = 1001
    private const val TAG = "AlarmScheduler"
    private const val ACTION_STEP_ALARM = "com.elsharif.dailyseventy.ACTION_STEP_ALARM"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleStepAlarm(context: Context) {
        try {
            if (!AlarmPreferences.isAlarmEnabled(context)) {
                Log.d(TAG, "Alarm is disabled, not scheduling")
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmHour = AlarmPreferences.getAlarmHour(context)
            val alarmMinute = AlarmPreferences.getAlarmMinute(context)

            // تحقق من صلاحية جدولة exact alarms على Android S+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "App cannot schedule exact alarms (canScheduleExactAlarms() == false). Requesting permission.")
                // افتح واجهة النظام لطلب السماح للمستخدم (يجب أن تكون Activity context أو تضع FLAG_ACTIVITY_NEW_TASK)
                try {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    }
                    context.startActivity(settingsIntent)
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to open schedule-exact-alarm settings: ${ex.message}", ex)
                }
                return
            }

            // إنشاء Intent للـ BroadcastReceiver مع action ثابت
            val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
                action = ACTION_STEP_ALARM
                putExtra("step_alarm", "step_alarm")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // تحديد وقت المنبه
            val calendar = Calendar.getInstance()
            val now = Calendar.getInstance()

            calendar.set(Calendar.HOUR_OF_DAY, alarmHour)
            calendar.set(Calendar.MINUTE, alarmMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (calendar.timeInMillis <= now.timeInMillis) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                Log.d(TAG, "Alarm time has passed today, scheduling for tomorrow")
            }

            // جدولة المنبه حسب إصدار الأندرويد
            try {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        // Android 6.0+ - استخدم setExactAndAllowWhileIdle (قد يرمي SecurityException إذا لم تُمنح صلاحية exact alarms)
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                    else -> {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
                Log.d(TAG, "Alarm scheduled successfully for: ${calendar.time}")
                Log.d(TAG, "Time: $alarmHour:$alarmMinute")
            } catch (se: SecurityException) {
                Log.e(TAG, "SecurityException scheduling exact alarm: ${se.message}", se)
                // محاولة إرسال المستخدم لصفحة السماح كـ fallback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(settingsIntent)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to open schedule-exact-alarm settings after SecurityException: ${ex.message}", ex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }


    fun cancelStepAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
                action = ACTION_STEP_ALARM
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d(TAG, "Alarm cancelled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm: ${e.message}", e)
        }
    }

    fun isAlarmSet(context: Context): Boolean {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = ACTION_STEP_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}
