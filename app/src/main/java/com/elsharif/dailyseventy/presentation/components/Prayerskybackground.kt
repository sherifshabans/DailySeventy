package com.elsharif.dailyseventy.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

// ══════════════════════════════════════════════════════════════════════════════
//  أنواع الأوقات الشرعية
// ══════════════════════════════════════════════════════════════════════════════

enum class PrayerSkyType {
    FAJR,    // الفجر   — فجر بنفسجي هادئ
    SUNRISE, // الشروق  — شفق دهبي ورديّ
    DHUHR,   // الظهر   — سماء زرقاء مشرقة
    ASR,     // العصر   — ذهبي دافئ
    MAGHRIB, // المغرب  — غروب برتقالي نارى
    ISHA     // العشاء  — ليل نجوم
}

// ══════════════════════════════════════════════════════════════════════════════
//  بيانات كل مقطع سماء
// ══════════════════════════════════════════════════════════════════════════════

private data class SkyPalette(
    val top       : Color,
    val mid       : Color,
    val horizon   : Color,
    val ground    : Color,
    val bodyColor : Color,   // لون الشمس/القمر
    val glowColor : Color,   // هالة الجسم
    val starAlpha : Float,   // ظهور النجوم (0 = بدون، 1 = كاملة)
    val hasRays   : Boolean, // خطوط ضوء الشمس
    val cloudAlpha: Float    // ظهور الغيوم
)

private fun getPalette(sky: PrayerSkyType): SkyPalette = when (sky) {
    PrayerSkyType.FAJR -> SkyPalette(
        top        = Color(0xFF0D0A2E),
        mid        = Color(0xFF1A0A3E),
        horizon    = Color(0xFF7B2D8B),
        ground     = Color(0xFF0D0820),
        bodyColor  = Color(0xFFFFEECC),
        glowColor  = Color(0xFFB39DDB),
        starAlpha  = 0.55f,
        hasRays    = false,
        cloudAlpha = 0.2f
    )
    PrayerSkyType.SUNRISE -> SkyPalette(
        top        = Color(0xFF0A1A3E),
        mid        = Color(0xFFE8722A),
        horizon    = Color(0xFFFFD54F),
        ground     = Color(0xFF0D1020),
        bodyColor  = Color(0xFFFFE082),
        glowColor  = Color(0xFFFF8A65),
        starAlpha  = 0.05f,
        hasRays    = true,
        cloudAlpha = 0.45f
    )
    PrayerSkyType.DHUHR -> SkyPalette(
        top        = Color(0xFF0D47A1),
        mid        = Color(0xFF1565C0),
        horizon    = Color(0xFF42A5F5),
        ground     = Color(0xFF0A2040),
        bodyColor  = Color(0xFFFFF9C4),
        glowColor  = Color(0xFFFFEE58),
        starAlpha  = 0f,
        hasRays    = true,
        cloudAlpha = 0.60f
    )
    PrayerSkyType.ASR -> SkyPalette(
        top        = Color(0xFF0D2A5E),
        mid        = Color(0xFF1976D2),
        horizon    = Color(0xFFFF8F00),
        ground     = Color(0xFF0A1A30),
        bodyColor  = Color(0xFFFFCC02),
        glowColor  = Color(0xFFFFB300),
        starAlpha  = 0f,
        hasRays    = true,
        cloudAlpha = 0.50f
    )
    PrayerSkyType.MAGHRIB -> SkyPalette(
        top        = Color(0xFF0A0820),
        mid        = Color(0xFF6A1B12),
        horizon    = Color(0xFFFF6D00),
        ground     = Color(0xFF0A0510),
        bodyColor  = Color(0xFFFF8A50),
        glowColor  = Color(0xFFFF3D00),
        starAlpha  = 0.3f,
        hasRays    = true,
        cloudAlpha = 0.55f
    )
    PrayerSkyType.ISHA -> SkyPalette(
        top        = Color(0xFF020510),
        mid        = Color(0xFF050D1E),
        horizon    = Color(0xFF0A1428),
        ground     = Color(0xFF020408),
        bodyColor  = Color(0xFFE8E8F0),
        glowColor  = Color(0xFF90CAF9),
        starAlpha  = 1f,
        hasRays    = false,
        cloudAlpha = 0.08f
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  تحديد نوع السماء من اسم الصلاة
// ══════════════════════════════════════════════════════════════════════════════

fun prayerNameToSkyType(prayerName: String): PrayerSkyType = when {
    prayerName.contains("فجر") || prayerName.contains("Fajr", ignoreCase = true)
        -> PrayerSkyType.FAJR
    prayerName.contains("شروق") || prayerName.contains("Sunrise", ignoreCase = true)
        -> PrayerSkyType.SUNRISE
    prayerName.contains("ظهر") || prayerName.contains("Dhuhr", ignoreCase = true)
        -> PrayerSkyType.DHUHR
    prayerName.contains("عصر") || prayerName.contains("Asr", ignoreCase = true)
        -> PrayerSkyType.ASR
    prayerName.contains("مغرب") || prayerName.contains("Maghrib", ignoreCase = true)
        -> PrayerSkyType.MAGHRIB
    prayerName.contains("عشاء") || prayerName.contains("Isha", ignoreCase = true)
        -> PrayerSkyType.ISHA
    else -> PrayerSkyType.DHUHR
}

// ══════════════════════════════════════════════════════════════════════════════
//  النجوم (بيانات عشوائية ثابتة)
// ══════════════════════════════════════════════════════════════════════════════

private data class Star(val x: Float, val y: Float, val size: Float, val phase: Float)

private val STARS: List<Star> = List(80) {
    Star(
        x     = Random.nextFloat(),
        y     = Random.nextFloat() * 0.75f,
        size  = Random.nextFloat() * 1.8f + 0.5f,
        phase = Random.nextFloat() * 2 * PI.toFloat()
    )
}

// نجوم كبيرة مميزة (نجم الشمال + نجوم بارزة)
private val BRIGHT_STARS: List<Triple<Float, Float, Float>> = listOf(
    Triple(0.15f, 0.08f, 3.5f),
    Triple(0.42f, 0.05f, 2.8f),
    Triple(0.72f, 0.12f, 3.2f),
    Triple(0.88f, 0.04f, 2.5f),
    Triple(0.28f, 0.20f, 2.2f),
    Triple(0.60f, 0.18f, 2.6f),
)

// ══════════════════════════════════════════════════════════════════════════════
//  السُّحُب (بيانات ثابتة)
// ══════════════════════════════════════════════════════════════════════════════

private data class Cloud(
    val baseX : Float,
    val y     : Float,
    val scale : Float,
    val speed : Float   // سرعة الحركة الأفقية
)

private val CLOUDS: List<Cloud> = listOf(
    Cloud(0.15f, 0.22f, 0.9f,  0.00012f),
    Cloud(0.55f, 0.15f, 1.2f,  0.00008f),
    Cloud(0.82f, 0.28f, 0.75f, 0.00015f),
    Cloud(0.35f, 0.32f, 0.6f,  0.00010f),
)

// ══════════════════════════════════════════════════════════════════════════════
//  Composable الرئيسي
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun PrayerSkyBackground(
    skyType : PrayerSkyType,
    modifier: Modifier = Modifier
) {
    val palette = getPalette(skyType)
    val isMoon  = skyType == PrayerSkyType.FAJR || skyType == PrayerSkyType.ISHA

    // ── انتقال سلس بين الألوان ────────────────────────────────────────────────
    val animSpec = tween<Color>(durationMillis = 1800, easing = FastOutSlowInEasing)
    val animTop     by animateColorAsState(palette.top,      animSpec, label = "top")
    val animMid     by animateColorAsState(palette.mid,      animSpec, label = "mid")
    val animHorizon by animateColorAsState(palette.horizon,  animSpec, label = "hor")
    val animGround  by animateColorAsState(palette.ground,   animSpec, label = "gnd")
    val animBody    by animateColorAsState(palette.bodyColor, animSpec, label = "body")
    val animGlow    by animateColorAsState(palette.glowColor, animSpec, label = "glow")

    val animStarAlpha  by animateFloatAsState(palette.starAlpha,  tween(1800), label = "stars")
    val animCloudAlpha by animateFloatAsState(palette.cloudAlpha, tween(1800), label = "clouds")

    // ── أنيميشن لا نهائي ──────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "sky")

    // نبض النجوم
    val starPulse by infiniteTransition.animateFloat(
        0f, 2 * PI.toFloat(),
        infiniteRepeatable(tween(3500, easing = LinearEasing)), label = "sp"
    )
    // حركة الغيوم
    val cloudDrift by infiniteTransition.animateFloat(
        0f, 1000f,
        infiniteRepeatable(tween(60_000, easing = LinearEasing)), label = "cd"
    )
    // نبض هالة الشمس/القمر
    val bodyPulse by infiniteTransition.animateFloat(
        0f, 2 * PI.toFloat(),
        infiniteRepeatable(tween(2800, easing = LinearEasing)), label = "bp"
    )
    // خطوط الأشعة تدور ببطء
    val rayRotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(40_000, easing = LinearEasing)), label = "rr"
    )
    // موج خط الأفق
    val horizonWave by infiniteTransition.animateFloat(
        0f, 2 * PI.toFloat(),
        infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "hw"
    )
    // وميض القمر
    val moonShimmer by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ms"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val W = size.width
        val H = size.height

        // ══════════════════════════════════════════════════════════════════════
        //  1. السماء المتدرجة
        // ══════════════════════════════════════════════════════════════════════
        drawRect(
            Brush.verticalGradient(
                0.00f to animTop,
                0.45f to animMid,
                0.78f to animHorizon,
                1.00f to animGround
            )
        )

        // ══════════════════════════════════════════════════════════════════════
        //  2. هالة الأفق الإضافية (فجر / غروب)
        // ══════════════════════════════════════════════════════════════════════
        if (skyType == PrayerSkyType.FAJR || skyType == PrayerSkyType.MAGHRIB ||
            skyType == PrayerSkyType.SUNRISE) {
            val horizonAlpha = 0.35f + sin(horizonWave) * 0.08f
            drawOval(
                brush = Brush.radialGradient(
                    listOf(animHorizon.copy(alpha = horizonAlpha), Color.Transparent),
                    Offset(W * 0.5f, H * 0.85f),
                    W * 0.75f
                ),
                topLeft = Offset(0f, H * 0.55f),
                size    = androidx.compose.ui.geometry.Size(W, H * 0.5f)
            )
        }

        // ══════════════════════════════════════════════════════════════════════
        //  3. النجوم
        // ══════════════════════════════════════════════════════════════════════
        if (animStarAlpha > 0.01f) {
            // نجوم عادية صغيرة
            STARS.forEach { star ->
                val twinkle = 0.4f + 0.6f * sin(starPulse + star.phase).absoluteValue
                val alpha   = animStarAlpha * twinkle
                drawCircle(
                    color  = Color.White.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = star.size,
                    center = Offset(star.x * W, star.y * H)
                )
            }
            // نجوم كبيرة مميزة
            BRIGHT_STARS.forEach { (rx, ry, r) ->
                val shimmerAlpha = animStarAlpha * (0.7f + 0.3f * sin(starPulse * 0.7f + rx * 10f).absoluteValue)
                // هالة خفيفة
                drawCircle(
                    brush  = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = shimmerAlpha * 0.4f), Color.Transparent),
                        Offset(rx * W, ry * H), r * 6f
                    ),
                    radius = r * 6f,
                    center = Offset(rx * W, ry * H)
                )
                drawCircle(
                    color  = Color.White.copy(alpha = shimmerAlpha),
                    radius = r,
                    center = Offset(rx * W, ry * H)
                )
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        //  4. الشمس أو القمر
        // ══════════════════════════════════════════════════════════════════════
        val bodyY = when (skyType) {
            PrayerSkyType.DHUHR   -> H * 0.18f
            PrayerSkyType.ASR     -> H * 0.30f
            PrayerSkyType.SUNRISE -> H * 0.55f
            PrayerSkyType.MAGHRIB -> H * 0.58f
            PrayerSkyType.FAJR    -> H * 0.25f
            PrayerSkyType.ISHA    -> H * 0.20f
        }
        val bodyX = when (skyType) {
            PrayerSkyType.SUNRISE -> W * 0.25f
            PrayerSkyType.MAGHRIB -> W * 0.78f
            PrayerSkyType.DHUHR   -> W * 0.50f
            else                  -> W * 0.72f
        }
        val bodyR  = if (isMoon) 14f else 18f
        val glowR1 = bodyR * (3.5f + 0.5f * sin(bodyPulse).absoluteValue)
        val glowR2 = bodyR * (6.0f + 0.8f * sin(bodyPulse + 1f).absoluteValue)

        // هالة خارجية كبيرة
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(animGlow.copy(alpha = 0.18f), Color.Transparent),
                Offset(bodyX, bodyY), glowR2
            ),
            radius = glowR2, center = Offset(bodyX, bodyY)
        )
        // هالة داخلية
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(animGlow.copy(alpha = 0.38f), Color.Transparent),
                Offset(bodyX, bodyY), glowR1
            ),
            radius = glowR1, center = Offset(bodyX, bodyY)
        )

        if (isMoon) {
            // ── القمر: دائرة ناقصة ────────────────────────────────────────────
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(animBody, animBody.copy(alpha = 0.85f)),
                    Offset(bodyX * 0.97f, bodyY * 0.97f), bodyR * 1.3f
                ),
                radius = bodyR, center = Offset(bodyX, bodyY)
            )
            // "عضّة" القمر
            drawCircle(
                color  = animTop.copy(alpha = 0.92f),
                radius = bodyR * 0.72f,
                center = Offset(bodyX + bodyR * 0.52f, bodyY - bodyR * 0.18f)
            )
            // بريق القمر
            drawCircle(
                color  = Color.White.copy(alpha = 0.3f + moonShimmer * 0.2f),
                radius = bodyR * 0.22f,
                center = Offset(bodyX - bodyR * 0.28f, bodyY - bodyR * 0.28f)
            )
        } else {
            // ── الشمس ─────────────────────────────────────────────────────────
            // أشعة دوّارة (خلف الشمس)
            if (palette.hasRays) {
                val rayCount = 12
                val rayLen   = bodyR * 2.8f
                repeat(rayCount) { i ->
                    val angle = Math.toRadians(
                        (i * 360f / rayCount + rayRotation).toDouble()
                    )
                    val cos = cos(angle).toFloat()
                    val sin = sin(angle).toFloat()
                    val rAlpha = if (i % 2 == 0) 0.22f else 0.12f
                    drawLine(
                        brush       = Brush.linearGradient(
                            listOf(animGlow.copy(alpha = rAlpha), Color.Transparent),
                            Offset(bodyX, bodyY),
                            Offset(bodyX + cos * (bodyR + rayLen), bodyY + sin * (bodyR + rayLen))
                        ),
                        start       = Offset(bodyX + cos * bodyR * 1.1f, bodyY + sin * bodyR * 1.1f),
                        end         = Offset(bodyX + cos * (bodyR + rayLen), bodyY + sin * (bodyR + rayLen)),
                        strokeWidth = if (i % 2 == 0) 3.5f else 1.8f
                    )
                }
            }
            // جسم الشمس
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(Color.White, animBody, animGlow),
                    Offset(bodyX * 0.97f, bodyY * 0.97f), bodyR * 1.3f
                ),
                radius = bodyR, center = Offset(bodyX, bodyY)
            )
            // بريق داخلي
            drawCircle(
                color  = Color.White.copy(alpha = 0.65f),
                radius = bodyR * 0.38f,
                center = Offset(bodyX - bodyR * 0.22f, bodyY - bodyR * 0.22f)
            )
        }

        // ══════════════════════════════════════════════════════════════════════
        //  5. الغيوم
        // ══════════════════════════════════════════════════════════════════════
        if (animCloudAlpha > 0.02f) {
            CLOUDS.forEach { cloud ->
                val drift  = ((cloud.baseX + cloudDrift * cloud.speed) % 1.2f) - 0.1f
                val cx     = drift * W
                val cy     = cloud.y * H
                val sc     = cloud.scale
                val cAlpha = animCloudAlpha * 0.85f
                val cColor = when (skyType) {
                    PrayerSkyType.MAGHRIB, PrayerSkyType.SUNRISE ->
                        lerp(animHorizon, Color.White, 0.45f).copy(alpha = cAlpha)
                    PrayerSkyType.DHUHR, PrayerSkyType.ASR ->
                        Color.White.copy(alpha = cAlpha)
                    else ->
                        Color(0xFFB0BEC5).copy(alpha = cAlpha * 0.55f)
                }
                drawCloud(cx, cy, sc * W * 0.18f, cColor)
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        //  6. طبقة أرض خفيفة (مسجد مبسّط بخطوط)
        // ══════════════════════════════════════════════════════════════════════
        drawSilhouette(W, H, animGround, animTop)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  رسم غيمة
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawCloud(cx: Float, cy: Float, r: Float, color: Color) {
    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
    }
    // ثلاث دوائر متداخلة تشكّل الغيمة
    listOf(
        Pair(Offset(cx - r * 0.55f, cy + r * 0.15f), r * 0.70f),
        Pair(Offset(cx,             cy               ), r * 1.00f),
        Pair(Offset(cx + r * 0.60f, cy + r * 0.10f), r * 0.75f),
        Pair(Offset(cx - r * 0.20f, cy - r * 0.40f), r * 0.65f),
        Pair(Offset(cx + r * 0.25f, cy - r * 0.38f), r * 0.60f),
    ).forEach { (center, radius) ->
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(color, color.copy(alpha = 0f)),
                center, radius * 1.25f
            ),
            radius = radius,
            center = center
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  صورة ظلية للمسجد (silhouette)
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSilhouette(W: Float, H: Float, groundColor: Color, skyTop: Color) {
    val silColor = groundColor.copy(alpha = 0.92f)
    val path = Path().apply {
        // قاعدة الأرض
        moveTo(0f, H)
        lineTo(0f, H * 0.82f)

        // مبنى أيسر صغير
        lineTo(W * 0.05f, H * 0.82f)
        lineTo(W * 0.05f, H * 0.78f)
        lineTo(W * 0.10f, H * 0.78f)
        lineTo(W * 0.10f, H * 0.82f)

        // المسجد الرئيسي
        lineTo(W * 0.28f, H * 0.82f)
        lineTo(W * 0.28f, H * 0.72f)
        // قبة صغيرة يسار
        quadraticBezierTo(W * 0.30f, H * 0.67f, W * 0.32f, H * 0.72f)
        lineTo(W * 0.32f, H * 0.72f)
        lineTo(W * 0.38f, H * 0.72f)
        // القبة الكبيرة
        lineTo(W * 0.38f, H * 0.68f)
        quadraticBezierTo(W * 0.45f, H * 0.54f, W * 0.52f, H * 0.68f)
        lineTo(W * 0.52f, H * 0.68f)
        lineTo(W * 0.58f, H * 0.72f)
        // قبة صغيرة يمين
        lineTo(W * 0.64f, H * 0.72f)
        quadraticBezierTo(W * 0.66f, H * 0.67f, W * 0.68f, H * 0.72f)
        lineTo(W * 0.72f, H * 0.72f)
        lineTo(W * 0.72f, H * 0.82f)

        // المئذنة اليمين
        lineTo(W * 0.78f, H * 0.82f)
        lineTo(W * 0.78f, H * 0.62f)
        lineTo(W * 0.765f, H * 0.60f)
        lineTo(W * 0.765f, H * 0.57f)
        lineTo(W * 0.785f, H * 0.55f)
        lineTo(W * 0.79f,  H * 0.50f)   // رأس المئذنة
        lineTo(W * 0.795f, H * 0.55f)
        lineTo(W * 0.815f, H * 0.57f)
        lineTo(W * 0.815f, H * 0.60f)
        lineTo(W * 0.80f,  H * 0.62f)
        lineTo(W * 0.80f,  H * 0.82f)

        // مبنى أيمن صغير
        lineTo(W * 0.90f, H * 0.82f)
        lineTo(W * 0.90f, H * 0.78f)
        lineTo(W * 0.95f, H * 0.78f)
        lineTo(W * 0.95f, H * 0.82f)

        lineTo(W, H * 0.82f)
        lineTo(W, H)
        close()
    }
    drawPath(path, silColor)

    // نوافذ مضيئة عند الليل / الفجر / المغرب
    val windowAlpha = when {
        groundColor.blue > 0.05f -> 0f
        else -> 0.6f
    }
    if (windowAlpha > 0f) {
        val winColor = Color(0xFFFFE082).copy(alpha = windowAlpha)
        listOf(
            Pair(W * 0.34f, H * 0.76f),
            Pair(W * 0.42f, H * 0.76f),
            Pair(W * 0.50f, H * 0.76f),
            Pair(W * 0.58f, H * 0.76f),
            Pair(W * 0.66f, H * 0.76f),
        ).forEach { (wx, wy) ->
            drawRoundRect(
                color   = winColor,
                topLeft = Offset(wx - 4f, wy - 5f),
                size    = androidx.compose.ui.geometry.Size(8f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  lerp لون
// ══════════════════════════════════════════════════════════════════════════════

private fun lerp(a: Color, b: Color, t: Float) = Color(
    a.red   + (b.red   - a.red)   * t,
    a.green + (b.green - a.green) * t,
    a.blue  + (b.blue  - a.blue)  * t,
    a.alpha + (b.alpha - a.alpha) * t
)