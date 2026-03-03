package com.elsharif.dailyseventy.presentation.prayertimes

// ─────────────────────────────────────────────────────────────────────────────
// ملف منفصل: FastLocationHelper.kt
// بيحل مشكلة بطء تحديد الموقع بـ 3-layer approach:
//   1. lastLocation فوري لو موجود (< 3 دقائق)
//   2. BALANCED_POWER (network-based) سريع جداً ~2-3 ثواني
//   3. HIGH_ACCURACY (GPS) كـ fallback نهائي مع timeout
// ─────────────────────────────────────────────────────────────────────────────

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.elsharif.dailyseventy.R
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.util.GeoPoint

// ─────────────────────────────────────────────────────────────────────────────
// Composable
// ─────────────────────────────────────────────────────────────────────────────
@SuppressLint("MissingPermission")
@Composable
fun LocationButton(
    enabled: Boolean = true,
    context: Context,
    viewModel: PrayerTimeViewModel,
    modifier: Modifier = Modifier
) {
    var isGettingLocation by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val hasLocationPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    val isLocationEnabled = remember(context) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                getFastLocation(context, viewModel,
                    onLoading = { isGettingLocation = it },
                    onError   = { errorMessage = it }
                )
            } else {
                showLocationSettingsDialog = true
            }
        } else {
            errorMessage = "يجب السماح بصلاحية الموقع"
        }
    }

    Button(
        onClick = {
            errorMessage = null
            when {
                !hasLocationPermission -> permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
                !isLocationEnabled -> showLocationSettingsDialog = true
                else -> getFastLocation(context, viewModel,
                    onLoading = { isGettingLocation = it },
                    onError   = { errorMessage = it }
                )
            }
        },
        enabled = enabled && !isGettingLocation,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        AnimatedContent(
            targetState = isGettingLocation,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "location_btn"
        ) { loading ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("جاري التحديد...", fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.GetMyLocation), fontSize = 13.sp)
                }
            }
        }
    }

    errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
            textAlign = TextAlign.Center
        )
    }

    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            title = { Text("تفعيل خدمة الموقع") },
            text  = { Text("يرجى تفعيل خدمة الموقع في الإعدادات") },
            confirmButton = {
                TextButton(onClick = {
                    showLocationSettingsDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("فتح الإعدادات") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationSettingsDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ✅ getFastLocation — حل مشكلة البطء بـ 3 layers
// ─────────────────────────────────────────────────────────────────────────────
@SuppressLint("MissingPermission")
private fun getFastLocation(
    context: Context,
    viewModel: PrayerTimeViewModel,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    onLoading(true)
    onError(null)

    val fused = LocationServices.getFusedLocationProviderClient(context)
    val mainHandler = Handler(Looper.getMainLooper())

    // ── Layer 1: lastLocation فوري ────────────────────────────────────────────
    fused.lastLocation.addOnCompleteListener { task ->
        if (task.isSuccessful && task.result != null) {
            val loc = task.result
            val age = System.currentTimeMillis() - loc.time

            // لو الموقع أقل من 3 دقائق — استخدمه فوراً بدون انتظار
            if (age < 3 * 60 * 1000) {
                onLoading(false)
                val point = GeoPoint(loc.latitude, loc.longitude)
                viewModel.updateLocation(point)
                viewModel.updateAddressFromGeoPoint(point)
                return@addOnCompleteListener
            }
        }

        // ── Layer 2: BALANCED (network-based) — سريع جداً ~2-3 ثواني ──────────
        val balancedRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 2000L)
            .setMaxUpdates(1)
            .setWaitForAccurateLocation(false)
            .build()

        var gotResult = false

        val balancedCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (gotResult) return
                gotResult = true
                fused.removeLocationUpdates(this)
                onLoading(false)
                result.lastLocation?.let { loc ->
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    viewModel.updateLocation(point)
                    viewModel.updateAddressFromGeoPoint(point)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                // لو الـ network مش متاح — سيشتغل الـ Layer 3 بعد الـ timeout
            }
        }

        try {
            fused.requestLocationUpdates(balancedRequest, balancedCallback, Looper.getMainLooper())

            // Timeout بعد 4 ثواني — لو مجاش نتيجة من Layer 2 جرب Layer 3
            mainHandler.postDelayed({
                if (!gotResult) {
                    fused.removeLocationUpdates(balancedCallback)
                    // ── Layer 3: HIGH_ACCURACY (GPS) مع timeout ────────────────
                    requestHighAccuracyLocation(context, viewModel, onLoading, onError)
                }
            }, 4000)

        } catch (e: SecurityException) {
            onLoading(false)
            onError("خطأ في الصلاحيات")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Layer 3: HIGH_ACCURACY fallback مع timeout واضح
// ─────────────────────────────────────────────────────────────────────────────
@SuppressLint("MissingPermission")
private fun requestHighAccuracyLocation(
    context: Context,
    viewModel: PrayerTimeViewModel,
    onLoading: (Boolean) -> Unit,
    onError: (String?) -> Unit
) {
    val fused = LocationServices.getFusedLocationProviderClient(context)
    val mainHandler = Handler(Looper.getMainLooper())

    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMaxUpdates(1)
        .setWaitForAccurateLocation(false) // ← مهم: بيرجع أسرع بدما يستنى دقة عالية
        .build()

    var gotResult = false

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (gotResult) return
            gotResult = true
            fused.removeLocationUpdates(this)
            onLoading(false)
            result.lastLocation?.let { loc ->
                val point = GeoPoint(loc.latitude, loc.longitude)
                viewModel.updateLocation(point)
                viewModel.updateAddressFromGeoPoint(point)
            }
        }
    }

    try {
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Timeout نهائي بعد 10 ثواني
        mainHandler.postDelayed({
            if (!gotResult) {
                gotResult = true
                fused.removeLocationUpdates(callback)
                onLoading(false)
                onError("تعذر تحديد موقعك، جرب مرة أخرى")
            }
        }, 10000)
    } catch (e: SecurityException) {
        onLoading(false)
        onError("خطأ في الصلاحيات")
    }
}