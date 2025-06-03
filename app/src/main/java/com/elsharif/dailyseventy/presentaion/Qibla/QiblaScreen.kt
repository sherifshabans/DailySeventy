package com.elsharif.dailyseventy.presentaion.Qibla

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.core.presentationSensor.MainViewModel
import com.elsharif.dailyseventy.util.workmanager.LocationManager
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QiblaScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }

    val azimuth = viewModel.azimuth
    var qiblaDirection by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        locationManager.getLocation { latStr, lonStr ->
            val lat = latStr.toDoubleOrNull()
            val lon = lonStr.toDoubleOrNull()
            if (lat != null && lon != null) {
                qiblaDirection = viewModel.calculateQiblaDirection(lat, lon)
            }
        }
    }

    val angleToQibla = qiblaDirection?.let { (it - azimuth + 360) % 360 }
    val direction = angleToQibla?.let {
        when {
            it == 0f -> "You're facing the Qibla"
            it < 180 -> "Turn right ${it.toInt()}°"
            else -> "Turn left ${(360 - it).toInt()}°"
        }
    }
    val isFacingQibla = angleToQibla != null && (angleToQibla <= 5f || angleToQibla >= 355f)
    var feedbackTriggered by remember { mutableStateOf(false) }
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Trigger vibration/sound when facing Qibla
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !feedbackTriggered) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            feedbackTriggered = true
        } else if (!isFacingQibla) {
            feedbackTriggered = false
        }
    }
    val needleColor = if (isFacingQibla) Color(0xFF93000A) else Color(0xFFDAD4A6)


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("اتجاه الجهاز: ${azimuth.toInt()}°")
        qiblaDirection?.let {
            Text("اتجاه القبلة: ${it.toInt()}°")
            Text("الانحراف: ${angleToQibla?.toInt()}°")
            direction?.let {
                val dirArabic = when {
                    it == "You're facing the Qibla" -> "أنت تواجه القبلة"
                    it.contains("Turn right") -> "اتجه يمينًا ${angleToQibla?.toInt()}°"
                    it.contains("Turn left") -> "اتجه يسارًا ${(360 - angleToQibla!!).toInt()}°"
                    else -> ""
                }
                Text("الاتجاه: $dirArabic")
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(500.dp)
        ) {
            val compassSize = 350.dp
            val kabaaSize = 50.dp
            val radiusDp = compassSize / 2 - kabaaSize / 2
            val density = LocalDensity.current
            val radiusPx = with(density) { radiusDp.toPx() }
            val kabaaHalfSizePx = with(density) { (kabaaSize / 2).toPx() }

            // Compass image
            Image(
                painter = painterResource(id = R.drawable.compass1),
                contentDescription = "Compass Background",
                modifier = Modifier.size(compassSize)
            )

            // Cardinal directions
            Text(
                text = "N",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset { IntOffset(0, (-radiusPx).roundToInt()) },
                textAlign = TextAlign.Center
            )
            Text(
                text = "E",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset { IntOffset(radiusPx.roundToInt(), 0) },
                textAlign = TextAlign.Center
            )
            Text(
                text = "S",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset { IntOffset(0, radiusPx.roundToInt()) },
                textAlign = TextAlign.Center
            )
            Text(
                text = "W",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset { IntOffset((-radiusPx).roundToInt(), 0) },
                textAlign = TextAlign.Center
            )

            // Needle that rotates with azimuth
            Image(
                painter = painterResource(id = R.drawable.needle),
                contentDescription = "Qibla Needle",
                modifier = Modifier
                    .size(172.dp)
                    .graphicsLayer {
                        rotationZ = -azimuth
                    },
                colorFilter = ColorFilter.tint(needleColor)

            )

            // Kaaba at fixed Qibla direction
            qiblaDirection?.let { direction ->
                val angleRad = Math.toRadians(direction.toDouble())
                val xPx = (radiusPx * cos(angleRad)).toFloat()
                val yPx = (-radiusPx * sin(angleRad)).toFloat()

                Image(
                    painter = painterResource(id = R.drawable.kabaa),
                    contentDescription = "Kaaba",
                    modifier = Modifier
                        .size(kabaaSize)
                        .offset {
                            IntOffset(
                                xPx.roundToInt() - kabaaHalfSizePx.roundToInt(),
                                yPx.roundToInt() - kabaaHalfSizePx.roundToInt()
                            )
                        }
                )
            }
        }
    }
}
