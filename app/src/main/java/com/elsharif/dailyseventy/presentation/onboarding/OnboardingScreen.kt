package com.elsharif.dailyseventy.presentation.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import com.elsharif.dailyseventy.R

// ─────────────────────────────────────────────────────────────────────────────
// Prefs helper
// ─────────────────────────────────────────────────────────────────────────────
object OnboardingPrefs {
    private const val PREF_NAME = "onboarding_prefs"
    private const val KEY_DONE  = "onboarding_done"

    fun isDone(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DONE, false)

    fun markDone(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DONE, true).apply()
}

// ─────────────────────────────────────────────────────────────────────────────
// OnboardingScreen
//
// الاستخدام في HomePage أو AppNavHost:
//
//   var showOnboarding by remember {
//       mutableStateOf(!OnboardingPrefs.isDone(context))
//   }
//   if (showOnboarding) {
//       OnboardingScreen(onFinish = { showOnboarding = false })
//   } else {
//       // الشاشة الرئيسية
//   }
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context    = LocalContext.current
    val steps      = onboardingSteps
    val pagerState = rememberPagerState { steps.size }
    val scope      = rememberCoroutineScope()
    val primary    = MaterialTheme.colorScheme.primary

    // animations
    val inf = rememberInfiniteTransition(label = "ob")
    val floatY by inf.animateFloat(
        -8f, 8f,
        infiniteRepeatable(tween(2400, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), RepeatMode.Reverse),
        label = "fy"
    )
    val glowAlpha by inf.animateFloat(
        0.4f, 0.9f,
        infiniteRepeatable(tween(1800, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), RepeatMode.Reverse),
        label = "ga"
    )
    val starRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "sr"
    )

    fun finish() {
        OnboardingPrefs.markDone(context)
        onFinish()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── خلفية متدرجة ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            primary.copy(alpha = 0.92f),
                            primary.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // ── نجوم خلفية ────────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stars = listOf(
                Offset(size.width * 0.1f,  size.height * 0.08f) to 3f,
                Offset(size.width * 0.9f,  size.height * 0.06f) to 2.5f,
                Offset(size.width * 0.25f, size.height * 0.12f) to 2f,
                Offset(size.width * 0.75f, size.height * 0.15f) to 3f,
                Offset(size.width * 0.5f,  size.height * 0.05f) to 2f,
                Offset(size.width * 0.85f, size.height * 0.22f) to 1.5f,
                Offset(size.width * 0.15f, size.height * 0.25f) to 1.5f,
            )
            stars.forEach { (pos, r) ->
                drawCircle(Color.White.copy(glowAlpha * 0.7f), r * 1.8f, pos)
                drawCircle(Color.White.copy(glowAlpha), r, pos)
            }
            // هلال صغير
            val cx = size.width * 0.88f
            val cy = size.height * 0.1f
            val cr = 22f
            val path = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(Offset(cx, cy), cr))
                addOval(androidx.compose.ui.geometry.Rect(Offset(cx + cr * 0.5f, cy), cr * 0.82f))
            }
            drawPath(path, Color(0xFFF5D47A).copy(0.85f))
        }

        // ── المحتوى الرئيسي ───────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── زر تخطي ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { finish() }) {
                    Text(
                        stringResource(R.string.skip),
                        color = Color.White.copy(0.85f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            // ── الـ Pager ──────────────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true  // RTL
            ) { page ->
                OnboardingPage(
                    step       = steps[page],
                    primary    = primary,
                    floatY     = floatY,
                    glowAlpha  = glowAlpha,
                    starRot    = starRot
                )
            }

            // ── Dots indicator ─────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.indices.forEach { i ->
                    val isActive = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(if (isActive) 10.dp else 7.dp)
                            .background(
                                if (isActive) Color.White
                                else Color.White.copy(0.4f)
                            )
                    )
                }
            }

            // ── أزرار التنقل ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر السابق
                AnimatedVisibility(visible = pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(0.6f))
                    ) {
                        Text(stringResource(R.string.previous), fontWeight = FontWeight.SemiBold)
                    }
                }

                // زر التالي / ابدأ
                Button(
                    onClick = {
                        if (pagerState.currentPage < steps.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            finish()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor   = primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < steps.size - 1)
                            stringResource(R.string.next)
                        else
                            stringResource(R.string.start_now),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// صفحة واحدة في الـ Onboarding
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OnboardingPage(
    step: OnboardingStep,
    primary: Color,
    floatY: Float,
    glowAlpha: Float,
    starRot: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // ── أيقونة مع توهج ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .offset(y = floatY.dp)
                .size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // دوائر توهج خلفية
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(glowAlpha * 0.25f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(0.18f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            // نجوم دائرية حول الأيقونة
            Canvas(modifier = Modifier.size(140.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val orbitR = size.width * 0.46f
                for (i in 0 until 8) {
                    val angle = Math.toRadians((starRot + i * 45.0)).toFloat()
                    val x = cx + orbitR * cos(angle)
                    val y = cy + orbitR * sin(angle)
                    val starR = if (i % 2 == 0) 3.5f else 2f
                    drawCircle(Color.White.copy(glowAlpha * 0.7f), starR, Offset(x, y))
                }
            }

            // الأيقونة
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter       = painterResource(step.iconRes),
                    contentDescription = null, // نص وصفي غير ضروري
                    colorFilter   = ColorFilter.tint(Color.White),
                    modifier      = Modifier.size(52.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── العنوان ────────────────────────────────────────────────────────
        Text(
            text       = stringResource(step.titleRes),
            style      = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize   = 26.sp
            ),
            color      = Color.White,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // ── العنوان الفرعي ─────────────────────────────────────────────────
        Text(
            text      = stringResource(step.subtitleRes),
            style     = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color     = Color.White.copy(0.85f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // ── كارت الوصف ────────────────────────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(24.dp),
            color  = Color.White.copy(0.15f),
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // الوصف
                Text(
                    text      = stringResource(step.descriptionRes),
                    style     = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 26.sp,
                        fontSize   = 14.sp
                    ),
                    color     = Color.White.copy(0.92f),
                    textAlign = TextAlign.Center
                )

                // خط فاصل
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(1.dp)
                        .background(Color.White.copy(0.3f))
                )

                // النصيحة
                Text(
                    text      = stringResource(step.tipRes),
                    style     = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize   = 12.sp
                    ),
                    color     = Color(0xFFF5D47A),  // ذهبي
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}