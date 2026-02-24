package com.elsharif.dailyseventy.presentation.zekr

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*


import kotlin.math.*

// ══════════════════════════════════════════════════════════════════════════════
//  نوع الخلفية حسب الكاتيجوري
// ══════════════════════════════════════════════════════════════════════════════

enum class ZekkrBgType {
    MORNING,   // أذكار الصباح — شمس وسماء فجر
    EVENING,   // أذكار المساء — غروب وشفق
    SLEEP,     // أذكار النوم  — ليل ونجوم وقمر
    WAKEUP,    // أذكار الاستيقاظ — فجر هادي
    QURAN,     // أذكار القرآن — خضرة وسكينة
    PRAYER,    // أذكار الصلاة — ذهبي روحاني
    DEFAULT    // باقي الكاتيجوريز — سماء زرقاء ناعمة
}

fun categoryToZekkrBg(category: String): ZekkrBgType = when {
    category.contains("صباح") || category.contains("morning", true) -> ZekkrBgType.MORNING
    category.contains("مساء") || category.contains("evening", true) -> ZekkrBgType.EVENING
    category.contains("نوم")  || category.contains("sleep",   true) -> ZekkrBgType.SLEEP
    category.contains("استيقاظ") || category.contains("wake", true) -> ZekkrBgType.WAKEUP
    category.contains("قرآن") || category.contains("quran",   true) -> ZekkrBgType.QURAN
    category.contains("صلاة") || category.contains("prayer",  true) -> ZekkrBgType.PRAYER
    else -> ZekkrBgType.DEFAULT
}

// ══════════════════════════════════════════════════════════════════════════════
//  الألوان لكل نوع
// ══════════════════════════════════════════════════════════════════════════════

data class ZekkrBgPalette(
    val skyTop    : Color,
    val skyMid    : Color,
    val skyBot    : Color,
    val bodyColor : Color,   // الشمس / القمر
    val glowColor : Color,
    val starAlpha : Float,   // 0 = لا نجوم، 1 = نجوم كاملة
    val isNight   : Boolean
)

fun paletteFor(type: ZekkrBgType): ZekkrBgPalette = when (type) {
    ZekkrBgType.MORNING -> ZekkrBgPalette(
        skyTop    = Color(0xFF1A2A5E),
        skyMid    = Color(0xFFE8855A),
        skyBot    = Color(0xFFFFC87A),
        bodyColor = Color(0xFFFFE082),
        glowColor = Color(0xFFFFD54F),
        starAlpha = 0.25f,
        isNight   = false
    )
    ZekkrBgType.EVENING -> ZekkrBgPalette(
        skyTop    = Color(0xFF1A0E3A),
        skyMid    = Color(0xFFB84A20),
        skyBot    = Color(0xFFFF9966),
        bodyColor = Color(0xFFFF6B35),
        glowColor = Color(0xFFFF8C42),
        starAlpha = 0.40f,
        isNight   = false
    )
    ZekkrBgType.SLEEP -> ZekkrBgPalette(
        skyTop    = Color(0xFF030818),
        skyMid    = Color(0xFF0D1B3E),
        skyBot    = Color(0xFF162040),
        bodyColor = Color(0xFFD8E8F8),
        glowColor = Color(0xFFB0C8E8),
        starAlpha = 1.00f,
        isNight   = true
    )
    ZekkrBgType.WAKEUP -> ZekkrBgPalette(
        skyTop    = Color(0xFF1E1040),
        skyMid    = Color(0xFF7B4FA0),
        skyBot    = Color(0xFFFFB347),
        bodyColor = Color(0xFFFFF0A0),
        glowColor = Color(0xFFFFD700),
        starAlpha = 0.50f,
        isNight   = false
    )
    ZekkrBgType.QURAN -> ZekkrBgPalette(
        skyTop    = Color(0xFF0A2518),
        skyMid    = Color(0xFF1A4A30),
        skyBot    = Color(0xFF2D7A50),
        bodyColor = Color(0xFFD4AF37),
        glowColor = Color(0xFFDAA520),
        starAlpha = 0.30f,
        isNight   = false
    )
    ZekkrBgType.PRAYER -> ZekkrBgPalette(
        skyTop    = Color(0xFF1A1000),
        skyMid    = Color(0xFF4A3000),
        skyBot    = Color(0xFF8B6914),
        bodyColor = Color(0xFFFFD700),
        glowColor = Color(0xFFFFC107),
        starAlpha = 0.20f,
        isNight   = false
    )
    ZekkrBgType.DEFAULT -> ZekkrBgPalette(
        skyTop    = Color(0xFF0D2137),
        skyMid    = Color(0xFF1A4A7A),
        skyBot    = Color(0xFF4A90D9),
        bodyColor = Color(0xFFFFFAD0),
        glowColor = Color(0xFFE8F4FD),
        starAlpha = 0.15f,
        isNight   = false
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  النجوم — ثابتة بـ random seed
// ══════════════════════════════════════════════════════════════════════════════

private data class StarDot(val rx: Float, val ry: Float, val r: Float, val twinklePhase: Float)
private val STARS_DATA = List(120) {
    StarDot(
        rx           = (it * 7919 % 997).toFloat() / 997f,
        ry           = (it * 6271 % 991).toFloat() / 991f,
        r            = 0.3f + (it % 5) * 0.28f,
        twinklePhase = (it % 13).toFloat() / 13f
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  COMPOSABLE الرئيسي
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ZekkrDynamicBackground(
    bgType  : ZekkrBgType,
    modifier: Modifier = Modifier
) {
    val palette = paletteFor(bgType)

    val inf = rememberInfiniteTransition(label = "bg")

    // تلألأ النجوم
    val twinkle by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
        label = "tw"
    )

    // حركة الجسم (شمس/قمر) — طفيفة
    val bodyFloat by inf.animateFloat(
        -3f, 3f,
        infiniteRepeatable(tween(5500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bf"
    )

    // بريق الهالة
    val glowPulse by inf.animateFloat(
        0.6f, 1.0f,
        infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gp"
    )

    // حركة السحب (للصباح والمساء)
    val cloudDrift by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "cd"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val W = size.width
        val H = size.height

        // ── السماء ─────────────────────────────────────────────────────────────
        drawRect(
            Brush.verticalGradient(
                listOf(palette.skyTop, palette.skyMid, palette.skyBot),
                0f, H * 0.85f
            )
        )

        // ── الأرض / الأفق ──────────────────────────────────────────────────────
        drawRect(
            Brush.verticalGradient(
                listOf(
                    palette.skyBot.copy(alpha = 0.6f),
                    Color(0xFF050A05).copy(alpha = 0.9f)
                ),
                H * 0.75f, H
            ),
            topLeft = Offset(0f, H * 0.75f),
            size    = androidx.compose.ui.geometry.Size(W, H * 0.25f)
        )

        // ── النجوم ─────────────────────────────────────────────────────────────
        if (palette.starAlpha > 0f) {
            STARS_DATA.forEach { s ->
                val tPhase = (twinkle + s.twinklePhase) % 1f
                val tAlpha = (sin(tPhase * Math.PI.toFloat()) * 0.5f + 0.5f)
                    .coerceIn(0f, 1f) * palette.starAlpha
                if (tAlpha > 0.02f) {
                    drawCircle(
                        Color.White.copy(alpha = tAlpha * 0.08f),
                        s.r * 2.5f,
                        Offset(s.rx * W, s.ry * H * 0.70f)
                    )
                    drawCircle(
                        Color.White.copy(alpha = tAlpha),
                        s.r * 0.45f,
                        Offset(s.rx * W, s.ry * H * 0.70f)
                    )
                }
            }
        }

        // ── الشمس / القمر ──────────────────────────────────────────────────────
        val bodyX = W * 0.72f
        val bodyY = H * 0.22f + bodyFloat

        // هالة خارجية
        listOf(80f to 0.04f, 52f to 0.08f, 34f to 0.14f).forEach { (r, a) ->
            drawCircle(
                palette.glowColor.copy(alpha = a * glowPulse),
                r, Offset(bodyX, bodyY)
            )
        }

        if (palette.isNight) {
            // قمر — دائرة ناقصة
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFF0F4F8), Color(0xFFD0DCE8)),
                    Offset(bodyX, bodyY), 22f
                ),
                22f, Offset(bodyX, bodyY)
            )
            // الجزء المقطوع
            drawCircle(
                palette.skyMid.copy(alpha = 0.92f),
                18f, Offset(bodyX + 8f, bodyY - 4f)
            )
            // تفاصيل القمر
            listOf(-6f to -4f, 4f to 6f, -2f to 8f).forEach { (ox, oy) ->
                drawCircle(
                    Color(0xFF8090A0).copy(alpha = 0.18f),
                    3f, Offset(bodyX + ox, bodyY + oy)
                )
            }
        } else {
            // شمس
            drawCircle(
                Brush.radialGradient(
                    listOf(Color.White, palette.bodyColor, palette.bodyColor.copy(alpha = 0.6f)),
                    Offset(bodyX, bodyY), 22f
                ),
                22f, Offset(bodyX, bodyY)
            )
            // أشعة
            if (bgType != ZekkrBgType.QURAN) {
                repeat(8) { i ->
                    val ang = Math.toRadians(i * 45.0 + 15.0)
                    drawLine(
                        palette.bodyColor.copy(alpha = 0.25f * glowPulse),
                        Offset(bodyX, bodyY),
                        Offset(
                            bodyX + (cos(ang) * W * 0.4f).toFloat(),
                            bodyY + (sin(ang) * H * 0.25f).toFloat()
                        ),
                        strokeWidth = 1.8f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // ── سحب بسيطة (للصباح والمساء والاستيقاظ) ─────────────────────────────
        if (bgType in listOf(ZekkrBgType.MORNING, ZekkrBgType.EVENING, ZekkrBgType.WAKEUP)) {
            val cloudAlpha = if (bgType == ZekkrBgType.EVENING) 0.55f else 0.40f
            val cloudLit  = if (bgType == ZekkrBgType.EVENING)
                Color(0xFFFF9966).copy(alpha = cloudAlpha)
            else
                Color(0xFFF8F4F0).copy(alpha = cloudAlpha)

            listOf(0.15f to 0.28f, 0.55f to 0.18f, 0.80f to 0.32f).forEach { (rx, ry) ->
                val cx = ((rx + cloudDrift * 0.08f) % 1.1f) * W
                val cy = ry * H * 0.55f
                val cr = W * 0.055f
                drawCircle(cloudLit, cr * 1.0f, Offset(cx, cy))
                drawCircle(cloudLit, cr * 0.80f, Offset(cx + cr * 0.9f, cy + cr * 0.1f))
                drawCircle(cloudLit, cr * 0.72f, Offset(cx - cr * 0.85f, cy + cr * 0.12f))
                drawCircle(cloudLit, cr * 0.90f, Offset(cx + cr * 0.05f, cy - cr * 0.18f))
            }
        }

        // ── لمعة القرآن (ضوء ذهبي ناعم) ───────────────────────────────────────
        if (bgType == ZekkrBgType.QURAN) {
            drawCircle(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFD4AF37).copy(alpha = 0.12f * glowPulse),
                        Color.Transparent
                    ),
                    Offset(W * 0.5f, H * 0.35f),
                    W * 0.55f
                ),
                W * 0.55f,
                Offset(W * 0.5f, H * 0.35f)
            )
        }
    }
}