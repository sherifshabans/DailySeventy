package com.elsharif.dailyseventy.presentation.Qibla


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.getAdaptiveGradient
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaSplashScreen(
    onAnimationComplete: () -> Unit = {}
) {
    var animationStarted by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val background = MaterialTheme.colorScheme.background
    val gradient = getAdaptiveGradient(primary)

    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "PhoneAnimation")

    // Phone rotation animation (clockwise rotation)
    val phoneRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhoneRotation"
    )

    // Circle drawing animation (progressive circle)
    val circleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "CircleProgress"
    )

    // Text fade animation
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TextAlpha"
    )

    // Auto-complete animation after 6 seconds
    LaunchedEffect(Unit) {
        delay(6000)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Title
            Text(
                text = stringResource(R.string.qibla_calibration),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animation Container
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val circleRadius = with(density) { 120.dp.toPx() }
                val innerRadius = with(density) { 100.dp.toPx() }

                // Animated circles
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val center = center
                    val strokeWidth = 4.dp.toPx()

                    // Draw base circle (static)
                    drawCircle(
                        color = primary.copy(alpha = 0.2f),
                        radius = circleRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw progressive circle with glow effect
                    val sweepAngle = 360f * circleProgress

                    // Outer glowing arc
                    drawArc(
                        color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth + 4.dp.toPx())
                    )

                    // Main arc
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(15f, 8f)
                            )
                        )
                    )

                    // Draw rotating dots on the circle
                    for (i in 0..7) {
                        val dotAngle = Math.toRadians((i * 45 + phoneRotation).toDouble())
                        val dotX = center.x + innerRadius * cos(dotAngle).toFloat()
                        val dotY = center.y + innerRadius * sin(dotAngle).toFloat()

                        drawCircle(
                            color = secondary.copy(alpha = 0.6f),
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                        )
                    }

                    // Draw arrow at the end of the arc
                    if (circleProgress > 0.1f) {
                        val arrowAngle = Math.toRadians((sweepAngle - 90).toDouble())
                        val arrowX = center.x + circleRadius * cos(arrowAngle).toFloat()
                        val arrowY = center.y + circleRadius * sin(arrowAngle).toFloat()

                        withTransform({
                            translate(arrowX, arrowY)
                            rotate(sweepAngle)
                        }) {
                            // Arrow shadow
                            drawLine(
                                color = Color.Black.copy(alpha = 0.2f),
                                start = androidx.compose.ui.geometry.Offset(-16f, -9f),
                                end = androidx.compose.ui.geometry.Offset(1f, 1f),
                                strokeWidth = strokeWidth + 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.Black.copy(alpha = 0.2f),
                                start = androidx.compose.ui.geometry.Offset(-16f, 7f),
                                end = androidx.compose.ui.geometry.Offset(1f, -1f),
                                strokeWidth = strokeWidth + 1.dp.toPx()
                            )

                            // Arrow head
                            drawLine(
                                color = Color(0xFF4CAF50),
                                start = androidx.compose.ui.geometry.Offset(-15f, -8f),
                                end = androidx.compose.ui.geometry.Offset(0f, 0f),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = Color(0xFF4CAF50),
                                start = androidx.compose.ui.geometry.Offset(-15f, 8f),
                                end = androidx.compose.ui.geometry.Offset(0f, 0f),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                }

                // Phone icon in the center rotating
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Phone background circle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = primary.copy(alpha = 0.1f),
                            radius = size.minDimension / 2f
                        )
                    }

                    // Use a simple rectangle to represent phone if no icon available
                    Canvas(
                        modifier = Modifier
                            .size(32.dp, 56.dp)
                            .rotate(phoneRotation)
                    ) {
                        val phoneColor = primary

                        // Phone body
                        drawRoundRect(
                            color = phoneColor,
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )

                        // Phone screen
                        drawRoundRect(
                            color = background,
                            topLeft = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 8.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - 8.dp.toPx(),
                                size.height - 16.dp.toPx()
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )

                        // Home button
                        drawCircle(
                            color = phoneColor,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(
                                size.width / 2f,
                                size.height - 6.dp.toPx()
                            )
                        )
                    }
                }

                // Direction indicators
                Text(
                    "N",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "S",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "E",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "W",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instruction text with fade animation
            Text(
                text = stringResource(R.string.rotate_phone_instruction),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = textAlpha),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary instruction
            Text(
                text = stringResource(R.string.calibrating_sensors),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// إضافة الدوال دي لـ strings.xml
/*
<string name="qibla_calibration">معايرة البوصلة</string>
<string name="rotate_phone_instruction">حرك هاتفك في دائرة لمعايرة أجهزة الاستشعار</string>
<string name="calibrating_sensors">جاري معايرة أجهزة الاستشعار...</string>
*/

// استخدم الكومبوزيبل ده في QiblaPage
@Composable
fun QiblaPageWithSplash(
    navController: NavController,
    viewModel: QiblaViewModel = hiltViewModel()
) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        QiblaSplashScreen {
            showSplash = false
        }
    } else {
        QiblaPage(navController, viewModel)
    }
}