package com.elsharif.dailyseventy.presentation.home.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.getAdaptiveGradient
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    // متغيرات الأنيميشن
    var logoVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var companyVisible by remember { mutableStateOf(false) }
    var animateTitle by remember { mutableStateOf(false) }

    // أنيميشن الخلفية
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(1000), label = ""
    )

    // أنيميشن اللوجو
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (logoVisible) 0f else 360f,
        animationSpec = tween(1200, easing = EaseInOutQuart), label = ""
    )

    // أنيميشن العنوان - Typewriter effect
    val appName = stringResource(R.string.app_name) // اسم التطبيق
    var visibleCharCount by remember { mutableStateOf(0) }
    val displayedText = appName.take(visibleCharCount)
    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    LaunchedEffect(Unit) {
        // بداية الأنيميشن
        delay(300)
        logoVisible = true

        delay(800)
        titleVisible = true
        animateTitle = true

        // كتابة العنوان حرف بحرف
        if (titleVisible) {
            for (i in 0..appName.length) {
                visibleCharCount = i
                delay(150) // سرعة ظهور كل حرف
            }
        }

        delay(500)
        companyVisible = true

        // إنهاء الـ Splash بعد وقت مناسب
        delay(1000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = gradient
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // مساحة علوية
            Spacer(modifier = Modifier.weight(1f))

            // لوجو التطبيق مع أنيميشن
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(animationSpec = tween(800)) + scaleIn(animationSpec = tween(800))
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .rotate(logoRotation),
                    contentAlignment = Alignment.Center
                ) {
                    // دائرة خلفية للوجو
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Color.White.copy(alpha = 0.95f),
                                shape = CircleShape
                            )
                    )

                    // أيقونة بديلة للوجو (استبدلها بلوجو التطبيق الحقيقي)
                    Image(
                        painter = painterResource(id = R.drawable.doaa),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // اسم التطبيق مع Typewriter effect
            AnimatedVisibility(
                visible = titleVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        Text(
                            text = displayedText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        // مؤشر الكتابة (cursor)
                        if (visibleCharCount < appName.length && animateTitle) {
                            val infiniteTransition = rememberInfiniteTransition(label = "")
                            val cursorAlpha by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500),
                                    repeatMode = RepeatMode.Reverse
                                ), label = ""
                            )

                            Text(
                                text = "|",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.alpha(cursorAlpha)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.5f))

            // معلومات الشركة في الأسفل
            AnimatedVisibility(
                visible = companyVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {

                    // لوجو الشركة
                    Image(
                        painter = painterResource(id = R.drawable.companylogo), // لوجو الشركة
                        contentDescription = "Company Logo",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // نص الشركة
                    Text(
                        text = stringResource(R.string.company_version), // "إصدار شركة مدهامتان"
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
