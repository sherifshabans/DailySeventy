package com.elsharif.dailyseventy.presentation.qibla

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen
import kotlin.math.roundToInt


@Composable
fun QiblaPage(
    navController: NavController,
    viewModel: QiblaViewModel = hiltViewModel()) {
    val qiblaDirection by viewModel.qiblaHeading.collectAsState()
    val currentDirection by viewModel.currentHeading.collectAsState()
    val needleRotation by viewModel.needleRotation.collectAsState(initial = 0.0f)

    val context = LocalContext.current
    val locationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result -> viewModel.onPermissionResult(result) }
    )

    LaunchedEffect(
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
    ) {
        locationPermissionResultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.Qible.titleRes, navController =navController ) }
    ) { paddingValues ->


        QiblaPageViews(paddingValues,qiblaDirection, currentDirection, needleRotation)

    }
}

@Composable
private fun QiblaPageViews(
    paddingValues: PaddingValues,
    qiblaDirection: Float, currentDirection: Float, needleRotation: Float) {

    val context = LocalContext.current
    val rotationAngle by animateFloatAsState(targetValue = needleRotation, label = "NeedleAnimation")

    val circleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)


    val currentDirectionString = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)) { append("${stringResource(id = R.string.current_direction)}: ") }
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
            append(currentDirection.roundToInt().toString())
        }
    }

    val qiblaDirectionString = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)) { append("${stringResource(id = R.string.qibla_angel)}: ") }
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
            append(qiblaDirection.roundToInt().toString())
        }
    }

    val angleToQibla = (qiblaDirection - currentDirection + 360) % 360
    val isFacingQibla = angleToQibla <= 5f || angleToQibla >= 355f
    var feedbackTriggered by remember { mutableStateOf(false) }
    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator

    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !feedbackTriggered) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(300)
            }
            feedbackTriggered = true
        } else if (!isFacingQibla) {
            feedbackTriggered = false
        }
    }

    val needleColor = if (isFacingQibla) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary

    Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {

            Spacer(modifier = Modifier.height(45.dp))
            Row(Modifier.padding(25.dp)) {
                Text(currentDirectionString, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Text(qiblaDirectionString, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp) // مسافة بين العناصر
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp).padding(2.dp) // حجم الدائرة
                ) {
                    // Canvas يرسم دائرة
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        drawCircle(
                            color = circleColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                        )
                    }

                    // الاتجاهات
                    Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(1.dp), fontWeight = FontWeight.Bold)
                    Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(1.dp), fontWeight = FontWeight.Bold)
                    Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(1.dp), fontWeight = FontWeight.Bold)
                    Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(1.dp), fontWeight = FontWeight.Bold)

                    // الكعبة في النص
                    Image(
                        painter = painterResource(id = R.drawable.kaaba),
                        contentDescription = stringResource(R.string.kaaba),
                        modifier = Modifier.size(48.dp) // صغر الحجم شوية عشان تبان جوه الدائرة
                    )
                }
            }



            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(500.dp),
                contentAlignment = Alignment.Center
            ) {


                // Compass background
                Image(
                    painter = painterResource(id = R.drawable.compass),
                    contentDescription = stringResource(R.string.compass),
                    modifier = Modifier.size(350.dp)
                    .align (Alignment.Center),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )

                // Needle (Center)
                Image(
                    painter = painterResource(id = R.drawable.needle),
                    contentDescription = stringResource(R.string.needle),
                    modifier = Modifier
                        .size(172.dp)
                        .align(Alignment.Center) // keep in middle
                       .rotate(rotationAngle),
             //      //     .graphicsLayer { rotationZ = rotationAngle // يلف حوالين المركز //    transformOrigin = TransformOrigin(0.5f, 0.5f) // مركز الدوران هو النص },
                    colorFilter = ColorFilter.tint(needleColor)
                )
            }

        }
}
