package com.elsharif.dailyseventy.presentation.sensor

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.elsharif.dailyseventy.domain.sensordomain.LightSensor
import com.elsharif.dailyseventy.domain.sensordomain.StepCounterSensor
import com.elsharif.dailyseventy.domain.sensordomain.StepDetectorSensor
import com.elsharif.dailyseventy.domain.data.preferences.AlarmPreferences
import com.elsharif.dailyseventy.domain.sensordomain.AlarmMusicService
import com.elsharif.dailyseventy.domain.sensordomain.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

@SuppressLint("AutoboxingStateCreation")
@HiltViewModel
class StepAlarmViewModel @Inject constructor(
    private val stepCounterSensor: StepCounterSensor,
    private val stepDetectorSensor: StepDetectorSensor,
    private val lightSensor: LightSensor
) : ViewModel() {

    var targetSteps by mutableStateOf(50)
    var isAlarmActive by mutableStateOf(false)
    var alarmHour by mutableStateOf(6)
    var alarmMinute by mutableStateOf(0)
    var isMusicPlaying by mutableStateOf(false)
    var isAlarmCompleted by mutableStateOf(false)
    var stepsTaken by mutableStateOf(0)
    var currentAlarmType by mutableStateOf(AlarmPreferences.ALARM_TYPE_MOVEMENT)
    var isDark by mutableStateOf(false)

    @SuppressLint("StaticFieldLeak")
    private var musicService: AlarmMusicService? = null
    private var isBound = false
    private var isMovementListenerSetup = false
    private var isLightListenerSetup = false

    private val serviceConnection = object : ServiceConnection {


        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as AlarmMusicService.MusicBinder
                musicService = binder.getService()
                isBound = true
                isMusicPlaying = musicService?.isMusicPlaying() ?: false
                Log.d("StepAlarmViewModel", "Service connected successfully")
            } catch (e: Exception) {
                Log.e("StepAlarmViewModel", "Error connecting to service: ${e.message}", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
            isMusicPlaying = false
            Log.d("StepAlarmViewModel", "Service disconnected")
        }
    }

    fun initializeAlarm(context: Context) {
        Log.d("StepAlarmViewModel", "Initializing alarm...")

        loadSavedSettings(context)
        bindMusicService(context)

        // إعداد الحساسات حسب نوع المنبه
        when (currentAlarmType) {
            AlarmPreferences.ALARM_TYPE_MOVEMENT -> {
                if (!isMovementListenerSetup) {
                    setupStepDetector()
                }
                checkAndHandleMovementAlarmState(context)
            }
            AlarmPreferences.ALARM_TYPE_LIGHT -> {
                if (!isLightListenerSetup) {
                    setupLightSensor(context)
                }
                checkAndHandleLightAlarmState(context)
            }
        }
    }

    private fun loadSavedSettings(context: Context) {
        alarmHour = AlarmPreferences.getAlarmHour(context)
        alarmMinute = AlarmPreferences.getAlarmMinute(context)
        targetSteps = AlarmPreferences.getRequiredSteps(context)
        currentAlarmType = AlarmPreferences.getAlarmType(context)

        Log.d("StepAlarmViewModel", "Settings loaded: $alarmHour:$alarmMinute, $targetSteps steps, type: $currentAlarmType")
    }

    private fun bindMusicService(context: Context) {
        try {
            val intent = Intent(context, AlarmMusicService::class.java)
            val result = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d("StepAlarmViewModel", "Bind service result: $result")
        } catch (e: Exception) {
            Log.e("StepAlarmViewModel", "Error binding service: ${e.message}", e)
        }
    }

    private fun checkAndHandleMovementAlarmState(context: Context) {
        if (!AlarmPreferences.isAlarmEnabled(context) ||
            currentAlarmType != AlarmPreferences.ALARM_TYPE_MOVEMENT) {
            Log.d("StepAlarmViewModel", "Movement alarm is disabled or wrong type")
            return
        }

        Log.d("StepAlarmViewModel", "Starting movement alarm automatically")
        startAlarm(context)
    }

    private fun checkAndHandleLightAlarmState(context: Context) {
        if (!AlarmPreferences.isAlarmEnabled(context) ||
            currentAlarmType != AlarmPreferences.ALARM_TYPE_LIGHT) {
            Log.d("StepAlarmViewModel", "Light alarm is disabled or wrong type")
            return
        }

        Log.d("StepAlarmViewModel", "Light alarm is enabled, monitoring light sensor")
        // منبه الإضاءة يعمل تلقائيًا عند الظلام
        monitorLightChanges(context)
    }

    private fun getTodayDateString(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
    private fun vibratePhone(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500, // المدة بالمللي ثانية
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (e: Exception) {
            Log.e("StepAlarmViewModel", "Error vibrating phone: ${e.message}", e)
        }
    }

    private fun setupStepDetector() {
        Log.d("StepAlarmViewModel", "Setting up Step Detector sensor...")

        if (stepDetectorSensor.doesSensorExist) {
            stepDetectorSensor.startListening()
            stepDetectorSensor.setOnSensorValuesChangedListener { _ ->
                stepsTaken++
                Log.d("StepAlarmViewModel", "STEP DETECTED! Count: $stepsTaken / $targetSteps")

                if (isAlarmActive && stepsTaken >= targetSteps && !isAlarmCompleted) {
                    Log.d("StepAlarmViewModel", "Goal reached! Completing alarm...")
                    completeAlarm()
                }
            }
            isMovementListenerSetup = true
            Log.d("StepAlarmViewModel", "StepDetector sensor setup completed")
        } else {
            Log.w("StepAlarmViewModel", "StepDetector sensor not available!")
            if (stepCounterSensor.doesSensorExist) {
                Log.d("StepAlarmViewModel", "Trying StepCounter as fallback")
                setupStepCounterFallback()
            } else {
                Log.e("StepAlarmViewModel", "No step sensors available on this device!")
            }
        }
    }

    private fun setupLightSensor(context: Context) {
        Log.d("StepAlarmViewModel", "Setting up Light sensor...")

        if (lightSensor.doesSensorExist) {
            lightSensor.startListening()
            lightSensor.setOnSensorValuesChangedListener { values ->
                if (values.isNotEmpty()) {
                    val lux = values[0]
                    val wasLight = !isDark
                    isDark = lux < 25f

                    Log.d("StepAlarmViewModel", "Light level: $lux lux, isDark: $isDark")

                    // إذا تغيرت الحالة من ضوء لظلام وكان منبه الإضاءة مفعل
                    if (isDark && wasLight && AlarmPreferences.isAlarmEnabled(context) &&
                        currentAlarmType == AlarmPreferences.ALARM_TYPE_LIGHT) {
                        Log.d("StepAlarmViewModel", "Light alarm triggered - it's getting dark!")
                        // بدء تشغيل المنبه
                        // نحتاج لتمرير context هنا - سنستخدم callback
//                        startLightAlarm(context)
                        completeAlarm()
                    }

                    // إذا تغيرت الحالة من ظلام لضوء وكان المنبه يعمل
                    if (!isDark && !wasLight && isAlarmActive &&
                        currentAlarmType == AlarmPreferences.ALARM_TYPE_LIGHT) {
                        Log.d("StepAlarmViewModel", "Light detected! Completing light alarm...")
                        completeAlarm()
                    }
                }
            }
            isLightListenerSetup = true
            Log.d("StepAlarmViewModel", "Light sensor setup completed")
        } else {
            Log.e("StepAlarmViewModel", "Light sensor not available on this device!")
        }
    }

    private fun monitorLightChanges(context: Context) {
        // هذه الوظيفة تراقب تغيرات الإضاءة وتشغل المنبه عند الحاجة
        if (isDark && !isAlarmActive) {
            Log.d("StepAlarmViewModel", "It's dark and light alarm is enabled - starting alarm")
            startLightAlarm(context)
        }
    }

    private var lastStepCount = 0
    private fun setupStepCounterFallback() {
        stepCounterSensor.startListening()
        stepCounterSensor.setOnSensorValuesChangedListener { values ->
            if (values.isNotEmpty()) {
                val totalSteps = values[0].toInt()
                Log.d("StepAlarmViewModel", "StepCounter reading: $totalSteps")

                if (lastStepCount == 0) {
                    lastStepCount = totalSteps
                    Log.d("StepAlarmViewModel", "StepCounter baseline: $lastStepCount")
                } else if (totalSteps > lastStepCount) {
                    val newSteps = totalSteps - lastStepCount
                    stepsTaken += newSteps
                    lastStepCount = totalSteps
                    Log.d("StepAlarmViewModel", "StepCounter detected $newSteps new steps. Total: $stepsTaken / $targetSteps")

                    if (isAlarmActive && stepsTaken >= targetSteps && !isAlarmCompleted) {
                        Log.d("StepAlarmViewModel", "Goal reached! Completing alarm...")
                        completeAlarm()
                    }
                }
            }
        }
    }

    fun resetStepCountOnScreenOpen() {
        Log.d("StepAlarmViewModel", "Screen opened - resetting step count")
        forceResetStepCount()
    }

    fun forceResetStepCount() {
        stepsTaken = 0
        lastStepCount = 0
        Log.d("StepAlarmViewModel", "Step count reset to 0")
    }

    fun startAlarm(context: Context) {
        if (currentAlarmType != AlarmPreferences.ALARM_TYPE_MOVEMENT) {
            Log.w("StepAlarmViewModel", "Trying to start movement alarm but current type is $currentAlarmType")
            return
        }

        Log.d("StepAlarmViewModel", "Starting movement alarm...")

        isAlarmActive = true
        isAlarmCompleted = false
        forceResetStepCount()
        vibratePhone(context)

        startMusicService(context)

        AlarmPreferences.setLastAlarmDate(context, getTodayDateString())
        AlarmScheduler.scheduleStepAlarm(context)

        Log.d("StepAlarmViewModel", "Movement alarm started successfully - steps reset to 0")
    }

    fun startLightAlarm(context: Context) {
        if (currentAlarmType != AlarmPreferences.ALARM_TYPE_LIGHT) {
            Log.w("StepAlarmViewModel", "Trying to start light alarm but current type is $currentAlarmType")
            return
        }

        Log.d("StepAlarmViewModel", "Starting light alarm...")

        isAlarmActive = true
        isAlarmCompleted = false
        vibratePhone(context)

        startMusicService(context)

        Log.d("StepAlarmViewModel", "Light alarm started successfully")
    }

    private fun startMusicService(context: Context) {
        try {
            val serviceIntent = Intent(context, AlarmMusicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                musicService?.startAlarmMusic()
                isMusicPlaying = true
            }, 1000)

        } catch (e: Exception) {
            Log.e("StepAlarmViewModel", "Error starting alarm service: ${e.message}", e)
        }
    }

    private fun completeAlarm() {
        Log.d("StepAlarmViewModel", "Completing alarm...")

        isAlarmCompleted = true
        isAlarmActive = false
        isMusicPlaying = false

        try {
            musicService?.stopAlarmMusic()
        } catch (e: Exception) {
            Log.e("StepAlarmViewModel", "Error stopping music: ${e.message}", e)
        }

        Log.d("StepAlarmViewModel", "Alarm completed successfully")
    }

    fun enableDailyAlarm(context: Context, enabled: Boolean) {
        Log.d("StepAlarmViewModel", "Setting movement alarm enabled: $enabled")

        if (currentAlarmType != AlarmPreferences.ALARM_TYPE_MOVEMENT) {
            Log.w("StepAlarmViewModel", "Cannot enable daily alarm for non-movement type")
            return
        }

        AlarmPreferences.setAlarmEnabled(context, enabled)

        if (enabled) {
            AlarmScheduler.scheduleStepAlarm(context)
            Log.d("StepAlarmViewModel", "Daily movement alarm enabled and scheduled")
        } else {
            AlarmScheduler.cancelStepAlarm(context)
            stopAlarm(context)
            Log.d("StepAlarmViewModel", "Daily movement alarm disabled and cancelled")
        }
    }

    fun enableLightAlarm(context: Context, enabled: Boolean) {
        Log.d("StepAlarmViewModel", "Setting light alarm enabled: $enabled")

        if (currentAlarmType != AlarmPreferences.ALARM_TYPE_LIGHT) {
            Log.w("StepAlarmViewModel", "Cannot enable light alarm for non-light type")
            return
        }

        AlarmPreferences.setAlarmEnabled(context, enabled)

        if (enabled) {
            // منبه الإضاءة يعمل فورًا إذا كان الجو مظلم
            if (isDark) {
                startLightAlarm(context)
            }
            Log.d("StepAlarmViewModel", "Light alarm enabled")
        } else {
            completeAlarm()
            stopAlarm(context)
            Log.d("StepAlarmViewModel", "Light alarm disabled")
        }
    }

    fun stopAlarm(context: Context) {
        Log.d("StepAlarmViewModel", "Stopping alarm...")

        isAlarmActive = false
        isAlarmCompleted = false
        isMusicPlaying = false

        try {
            musicService?.stopAlarmMusic()
            val serviceIntent = Intent(context, AlarmMusicService::class.java)
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            Log.e("StepAlarmViewModel", "Error stopping alarm: ${e.message}", e)
        }

        Log.d("StepAlarmViewModel", "Alarm stopped")
    }

    fun setAlarmTime(context: Context, hour: Int, minute: Int) {
        alarmHour = hour
        alarmMinute = minute
        AlarmPreferences.saveAlarmTime(context, hour, minute)
        Log.d("StepAlarmViewModel", "Alarm time set to: $hour:$minute")

        if (AlarmPreferences.isAlarmEnabled(context) &&
            currentAlarmType == AlarmPreferences.ALARM_TYPE_MOVEMENT) {
            AlarmScheduler.scheduleStepAlarm(context)
        }
    }

    fun setTargetSteps(context: Context, steps: Int) {
        targetSteps = steps
        AlarmPreferences.saveRequiredSteps(context, steps)
        Log.d("StepAlarmViewModel", "Target steps set to: $steps")
    }

    fun pauseMusic() {
        if (!isAlarmActive) {
            musicService?.pauseMusic()
            isMusicPlaying = false
        }
    }

    fun resumeMusic() {
        if (!isAlarmActive) {
            musicService?.resumeMusic()
            isMusicPlaying = true
        }
    }

    // وظيفة للتعامل مع تغيير نوع المنبه
    fun switchAlarmType(context: Context, newType: String) {
        Log.d("StepAlarmViewModel", "Switching alarm type from $currentAlarmType to $newType")

        // إيقاف المنبه الحالي إن كان يعمل
        if (isAlarmActive) {
            stopAlarm(context)
        }

        // إيقاف كل الحساسات
        stopAllSensors()

        // تحديث النوع
        currentAlarmType = newType
        AlarmPreferences.saveAlarmType(context, newType)

        // إعادة تهيئة الحساسات الجديدة
        when (newType) {
            AlarmPreferences.ALARM_TYPE_MOVEMENT -> {
                if (!isMovementListenerSetup) {
                    setupStepDetector()
                }
            }
            AlarmPreferences.ALARM_TYPE_LIGHT -> {
                if (!isLightListenerSetup) {
                    setupLightSensor(context)
                }
            }
        }

        Log.d("StepAlarmViewModel", "Alarm type switched successfully")
    }

    private fun stopAllSensors() {
        try {
            if (stepCounterSensor.doesSensorExist) stepCounterSensor.stopListening()
            if (stepDetectorSensor.doesSensorExist) stepDetectorSensor.stopListening()
            if (lightSensor.doesSensorExist) lightSensor.stopListening()

            isMovementListenerSetup = false
            isLightListenerSetup = false

            Log.d("StepAlarmViewModel", "All sensors stopped")
        } catch (e: Exception) {
            Log.e("StepAlarmViewModel", "Error stopping sensors: ${e.message}", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("StepAlarmViewModel", "ViewModel cleared, stopping sensors")
        stopAllSensors()
    }

    fun unbindService(context: Context) {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
                isBound = false
                Log.d("StepAlarmViewModel", "Service unbound")
            } catch (e: Exception) {
                Log.e("StepAlarmViewModel", "Error unbinding service: ${e.message}", e)
            }
        }
    }
}