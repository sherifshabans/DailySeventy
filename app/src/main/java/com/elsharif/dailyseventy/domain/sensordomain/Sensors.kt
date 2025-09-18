package com.elsharif.dailyseventy.domain.sensordomain

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor

/**
 * Light sensor implementation.
 * @param context The context in which the sensor operates.
 */
class LightSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_LIGHT,
    sensorType = Sensor.TYPE_LIGHT
)

/**
 * Step Counter sensor implementation.
 * @param context The context in which the sensor operates.
 */
class StepCounterSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_STEP_COUNTER,
    sensorType = Sensor.TYPE_STEP_COUNTER
)

/**
 * Step Detector sensor implementation (alternative for devices without step counter).
 * @param context The context in which the sensor operates.
 */
class StepDetectorSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_STEP_DETECTOR,
    sensorType = Sensor.TYPE_STEP_DETECTOR
)