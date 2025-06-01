package com.elsharif.dailyseventy

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.elsharif.dailyseventy.ui.theme.DailySeventyTheme
import com.elsharif.dailyseventy.util.Navigation.UnifiedNavigationScaffold
import com.elsharif.dailyseventy.util.workmanager.LocationManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val locationManager by lazy {
        LocationManager(applicationContext)
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this, permissions, 100
        )

        enableEdgeToEdge()
        setContent {
            DailySeventyTheme {

                UnifiedNavigationScaffold()
            }
        }
    }
}
