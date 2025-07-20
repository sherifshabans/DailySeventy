package com.elsharif.dailyseventy.domain.azan.prayersnotification

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object PrayerTimesTestUtil {
    private const val TAG = "PrayerTimesTestUtil"
    
    fun testPrayerTimesWorker(context: Context) {
        Log.d(TAG, "testPrayerTimesWorker: Manually triggering prayer times worker")
        
        val testWorkRequest = OneTimeWorkRequestBuilder<RegisterPrayerTimesWorker>()
            .addTag("TEST_PRAYER_TIMES")
            .build()
            
        WorkManager.getInstance(context.applicationContext)
            .enqueue(testWorkRequest)
            
        Log.d(TAG, "testPrayerTimesWorker: Test work request enqueued")
    }
    
    fun testAlarmManager(context: Context) {
        Log.d(TAG, "testAlarmManager: Testing alarm manager directly")
        
        // Test setting an alarm for 10 seconds from now
        val testTime = System.currentTimeMillis() + 10000 // 10 seconds from now
        
        val intent = android.content.Intent(context, AzanAlarmReceiver::class.java).apply {
            putExtra("CONTENT", "Test prayer notification")
            putExtra("TITLE", "Test Prayer Time")
            putExtra("ICON", android.R.drawable.ic_dialog_info)
            putExtra("ID", 999)
        }
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            999,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, testTime, pendingIntent)
            Log.d(TAG, "testAlarmManager: Set test alarm using setExactAndAllowWhileIdle")
        } else {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, testTime, pendingIntent)
            Log.d(TAG, "testAlarmManager: Set test alarm using setExact")
        }
    }
} 