package com.elsharif.dailyseventy.presentation.qibla

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.Math.toDegrees

class QiblaViewModel(application: Application) : AndroidViewModel(application) {


    private val kaabaLocation = Location("service Provider").apply {
        latitude = 21.422487 //kaaba latitude setting
        longitude = 39.826206 //kaaba longitude setting
    }


    private var userLocation = Location("service Provider")


    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)

    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false


    private val sensorManager: SensorManager? by lazy {
        application.getSystemService(SENSOR_SERVICE) as SensorManager?
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor == accelerometer) {
                event?.values?.let { lowPass(it, lastAccelerometer) }
                lastAccelerometerSet = true
            } else if (event?.sensor == magnetometer) {
                event?.values?.let { lowPass(it, lastMagnetometer) }
                lastMagnetometerSet = true
            }

            if (lastAccelerometerSet && lastMagnetometerSet) {
                val r = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {

                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val degree = (toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360

                    val bearTo = userLocation.bearingTo(kaabaLocation)

                    viewModelScope.launch {
                        _currentHeading.emit(degree)
                    }
                    viewModelScope.launch {
                        _qiblaHeading.emit(bearTo)
                    }
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    }

    private val accelerometer: Sensor? by lazy { sensorManager?.getDefaultSensor(TYPE_ACCELEROMETER) }.also {
        sensorManager?.registerListener(sensorEventListener, it.value, SENSOR_DELAY_GAME)
    }

    private val magnetometer: Sensor? by lazy { sensorManager?.getDefaultSensor(TYPE_MAGNETIC_FIELD) }.also {
        sensorManager?.registerListener(sensorEventListener, it.value, SENSOR_DELAY_GAME)
    }

    private val _currentHeading = MutableStateFlow(0.0f)
    private val _qiblaHeading = MutableStateFlow(0.0f)

    val currentHeading = _currentHeading.asStateFlow()
    val qiblaHeading = _qiblaHeading.asStateFlow()
    val needleRotation = combine(_currentHeading, _qiblaHeading) { current, qibla -> qibla - current }


    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }
    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { userLocation = it }
            }
        }
    }
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
    }


    private fun lowPass(input: FloatArray, output: FloatArray) {
        val alpha = 0.05f

        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }


//    private fun lowPass(input: FloatArray, output: FloatArray) =
//        input.forEachIndexed { index, fl -> output[index] = output[index] + 0.05f * (fl - output[index]) }
//

    override fun onCleared() {
        sensorManager?.unregisterListener(sensorEventListener, accelerometer)
        sensorManager?.unregisterListener(sensorEventListener, magnetometer)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onCleared()
    }

    @SuppressLint("MissingPermission")
    fun onPermissionResult(result: Map<String, Boolean>) {
        val isPermissionsGranted = result.values.fold(true) { acc, b -> acc && b }
        if (isPermissionsGranted) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
}