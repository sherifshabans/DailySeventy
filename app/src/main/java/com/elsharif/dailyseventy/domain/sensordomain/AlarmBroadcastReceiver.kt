package com.elsharif.dailyseventy.domain.sensordomain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.elsharif.dailyseventy.ui.MainActivity
import com.elsharif.dailyseventy.domain.data.sharedpreferences.AlarmPreferences
import java.util.Calendar

class AlarmBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "DEBUG onReceive called; action=${intent.action}; extras=${intent.extras}")

        if (!AlarmPreferences.isAlarmEnabled(context)) {
            Log.d(TAG, "Alarm is disabled, ignoring")
            return
        }

        val alarmType = AlarmPreferences.getAlarmType(context)

        // فقط منبه الحركة يحتاج لـ BroadcastReceiver (المجدول بوقت محدد)
        // منبه الإضاءة يعمل مباشرة من خلال حساس الضوء
        if (alarmType != AlarmPreferences.ALARM_TYPE_MOVEMENT) {
            Log.d(TAG, "Current alarm type is $alarmType, BroadcastReceiver only handles movement alarms")
            return
        }

        try {
            // تشغيل الخدمة أولاً
            val serviceIntent = Intent(context, AlarmMusicService::class.java).apply {
                putExtra("step_alarm", "step_alarm")
                putExtra("from_alarm_receiver", true)
                putExtra("alarm_type", alarmType)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // فتح الـ Activity (مهم: FLAG_ACTIVITY_NEW_TASK)
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("step_alarm", "step_alarm")
                putExtra("from_alarm_receiver", true)
                putExtra("alarm_type", alarmType)
            }
            context.startActivity(activityIntent)

            // حفظ تاريخ آخر تشغيل وجدولة المنبه التالي (فقط لمنبه الحركة)
            val todayString = getTodayDateString()
            AlarmPreferences.setLastAlarmDate(context, todayString)
            AlarmScheduler.scheduleStepAlarm(context)

            Log.d(TAG, "Movement alarm handled successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error handling movement alarm: ${e.message}", e)
        }
    }

    private fun getTodayDateString(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(
            Calendar.DAY_OF_MONTH)}"
    }
}