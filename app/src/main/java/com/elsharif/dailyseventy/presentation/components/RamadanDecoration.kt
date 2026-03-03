package com.elsharif.dailyseventy.presentation.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import androidx.compose.ui.unit.Dp

@Composable
fun RamadanDecoration(
    topPadding: Dp = 64.dp,
    modifier: Modifier = Modifier) {

    val inf = rememberInfiniteTransition(label = "rd")

    // تأرجح الفوانيس — بطيء وناعم جداً
    val swing by inf.animateFloat(
        -6f, 6f,
        infiniteRepeatable(tween(3200, easing = SwingEasing), RepeatMode.Reverse),
        label = "sw"
    )
    // نبض الضوء
    val pulse by inf.animateFloat(
        0.55f, 1f,
        infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pu"
    )
    // موجة الحبل
    val wave by inf.animateFloat(
        0f, TWO_PI,
        infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "wv"
    )
    // shimmer على الحبل
    val shimmer by inf.animateFloat(
        -0.2f, 1.2f,
        infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "sh"
    )
    // وميض النجوم
    val s1 by inf.animateFloat(0.3f, 1f,   infiniteRepeatable(tween(1300, easing = EaseInOutSine), RepeatMode.Reverse), label = "s1")
    val s2 by inf.animateFloat(1f,   0.25f, infiniteRepeatable(tween(1000, delayMillis = 400, easing = EaseInOutSine), RepeatMode.Reverse), label = "s2")
    val s3 by inf.animateFloat(0.4f, 1f,   infiniteRepeatable(tween(1600, delayMillis = 900, easing = EaseInOutSine), RepeatMode.Reverse), label = "s3")
    // دوران بطيء للنجوم الكبيرة
    val starSpin by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "ss"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val attachY = topPadding.toPx()
        val maxSag  = w * 0.155f

        // ── الحبل ────────────────────────────────────────────────────────────
        drawGoldenRope(w, attachY, maxSag, wave, shimmer)

        // ── الحبسات ───────────────────────────────────────────────────────────
        drawPremiumBeads(w, attachY, maxSag, wave, pulse)

        // ── الهلال (أهم عنصر — يسار) ─────────────────────────────────────────
        val crCx = w * 0.175f
        val crCy = attachY + maxSag * 0.22f + 18f
        val crR  = w * 0.062f
        drawPerfectCrescent(Offset(crCx, crCy), crR, starSpin, s1)

        // ── فانوس يمين كبير ──────────────────────────────────────────────────
        translate(w * 0.91f - 24f, attachY + ropeY(0.91f, maxSag, wave) - 4f) {
            drawMoroccanLantern(
                swing      = -swing * 0.88f,
                pulse      = pulse,
                scale      = 1.7f,
                bodyColor  = Color(0xFF074F44),
                glowColor  = Color(0xFF00C9A7)
            )
        }

        // ── فانوس يسار كبير ──────────────────────────────────────────────────
        translate(w * 0.09f - 24f, attachY + ropeY(0.09f, maxSag, wave) - 4f) {
            drawMoroccanLantern(
                swing      = swing,
                pulse      = pulse * 0.95f,
                scale      = 1.6f,
                bodyColor  = Color(0xFF074F44),
                glowColor  = Color(0xFF00C9A7)
            )
        }

        // ── فانوس وسط يمين (بوردو) ───────────────────────────────────────────
        translate(w * 0.68f - 16f, attachY + ropeY(0.68f, maxSag, wave) - 2f) {
            drawMoroccanLantern(
                swing      = -swing * 0.6f,
                pulse      = pulse * 0.88f,
                scale      = 1.15f,
                bodyColor  = Color(0xFF6B1515),
                glowColor  = Color(0xFFFF8C42)
            )
        }

        // ── فانوس وسط يسار (بوردو) ───────────────────────────────────────────
        translate(w * 0.32f - 16f, attachY + ropeY(0.32f, maxSag, wave) - 2f) {
            drawMoroccanLantern(
                swing      = swing * 0.5f,
                pulse      = pulse * 0.82f,
                scale      = 1.1f,
                bodyColor  = Color(0xFF6B1515),
                glowColor  = Color(0xFFFF8C42)
            )
        }

        // ── نجوم ─────────────────────────────────────────────────────────────
        drawSparkStar(Offset(w * 0.50f, attachY + maxSag + 62f),          5.5f, s2, starSpin)
        drawSparkStar(Offset(w * 0.74f, attachY + maxSag * 0.5f + 14f),   4.5f, s3, starSpin + 22f)
        drawSparkStar(Offset(w * 0.88f, attachY + maxSag * 0.25f + 10f),  3.5f, s1, starSpin + 45f)
        drawSparkStar(Offset(w * 0.04f, attachY + maxSag * 0.28f + 12f),  3f,   s2, starSpin + 90f)
        drawSparkStar(Offset(w * 0.96f, attachY + maxSag * 0.2f + 8f),    2.8f, s3, starSpin + 135f)
        drawSparkStar(Offset(w * 0.44f, attachY + maxSag * 0.7f + 44f),   3f,   s1, starSpin + 60f)
        drawSparkStar(Offset(w * 0.57f, attachY + maxSag * 0.58f + 30f),  2.5f, s2, starSpin + 180f)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Y موقع الحبل
// ─────────────────────────────────────────────────────────────────────────────
private fun ropeY(t: Float, maxSag: Float, wave: Float): Float {
    val cat    = sin(t * PI.toFloat()).pow(0.6f) * maxSag
    val wobble = sin(wave + t * 2.5f) * 1.8f
    return cat + wobble
}

// ─────────────────────────────────────────────────────────────────────────────
// الحبل الذهبي مع shimmer
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawGoldenRope(
    w: Float, attachY: Float, maxSag: Float, wave: Float, shimmer: Float
) {
    val pts = 120
    val path = Path().apply {
        for (i in 0..pts) {
            val t = i / pts.toFloat()
            val x = t * w
            val y = attachY + ropeY(t, maxSag, wave)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
    }
    val st = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // ظل
    drawPath(path, Color.Black.copy(0.28f), style = Stroke(5f, cap = StrokeCap.Round))
    // الحبل
    drawPath(path,
        Brush.horizontalGradient(listOf(
            Color.Transparent, GD.copy(0.7f), GL, GL, GD.copy(0.7f), Color.Transparent
        )), style = st
    )
    // shimmer
    val sx = shimmer * w
    if (sx in (-w * 0.2f)..(w * 1.2f)) {
        drawPath(path,
            Brush.horizontalGradient(
                listOf(Color.Transparent, Color.White.copy(0f), Color.White.copy(0.55f), Color.White.copy(0f), Color.Transparent),
                startX = sx - w * 0.12f, endX = sx + w * 0.12f
            ), style = st
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// الحبسات الفاخرة
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawPremiumBeads(
    w: Float, attachY: Float, maxSag: Float, wave: Float, pulse: Float
) {
    val count = 22
    for (i in 1 until count) {
        val t   = i / count.toFloat()
        val x   = t * w
        val y   = attachY + ropeY(t, maxSag, wave)
        val big = i % 6 == 0
        val r   = if (big) 7f else if (i % 3 == 0) 4.2f else 3f
        val col = when (i % 4) { 0 -> GL; 1 -> GD; 2 -> Color(0xFFCC3333); else -> Color(0xFF4AADAD) }
        val al  = (0.55f + sin(i * 0.9f + pulse * 3f).absoluteValue * 0.35f).coerceIn(0.5f, 1f)

        // ظل ناعم
        drawCircle(Color.Black.copy(0.18f), r + 2.5f, Offset(x + 1.2f, y + 2.5f))
        // الجسم
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(0.5f), col.copy(al), col.darken(0.2f).copy(al * 0.8f)),
                center = Offset(x - r * 0.3f, y - r * 0.3f),
                radius = r * 1.8f
            ),
            r, Offset(x, y)
        )
        // حافة ذهبية للكبيرة
        if (big) drawCircle(GD.copy(0.75f), r, Offset(x, y), style = Stroke(1f))
        // بريق
        drawCircle(Color.White.copy(0.7f), r * 0.25f, Offset(x - r * 0.35f, y - r * 0.35f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// الهلال المثالي — مع توهج ونجمة عثمانية
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawPerfectCrescent(
    center: Offset, radius: Float, spin: Float, pulse: Float
) {
    // ── طبقات التوهج ────────────────────────────────────────────────────────
    drawCircle(GL.copy(0.06f), radius * 4.5f, center)
    drawCircle(GL.copy(0.10f), radius * 3.0f, center)
    drawCircle(GL.copy(0.16f), radius * 2.0f, center)

    // ── الهلال نفسه ─────────────────────────────────────────────────────────
    // نرسمه بطريقة صحيحة: دائرة كاملة ناقص دائرة مزاحة (EvenOdd fill rule)
    val crescentPath = Path().apply {
        // الدائرة الخارجية (كامل)
        addOval(Rect(center, radius))
        // الدائرة الداخلية (الفراغ) — مزاحة يمين
        val cutCx = center.x + radius * 0.42f
        val cutCy = center.y - radius * 0.05f
        val cutR  = radius * 0.80f
        addOval(Rect(Offset(cutCx, cutCy), cutR))
    }

    clipPath(
        Path().apply {
            addRect(Rect(Offset.Zero, size))
        },
        clipOp = ClipOp.Intersect
    ) {
        drawPath(
            crescentPath,
            brush = Brush.linearGradient(
                colors = listOf(GL, GD, GL.copy(0.85f)),
                start = Offset(center.x - radius, center.y - radius),
                end   = Offset(center.x + radius * 0.3f, center.y + radius)
            )
        )

        // لمعة
        drawPath(
            crescentPath,
            brush = Brush.linearGradient(
                listOf(Color.White.copy(0.45f), Color.Transparent),
                Offset(center.x - radius * 0.8f, center.y - radius * 0.9f),
                Offset(center.x - radius * 0.1f, center.y + radius * 0.1f)
            )
        )
    }
    // حدود الهلال ناعمة
    drawPath(crescentPath, GL.copy(0.6f), style = Stroke(1.3f))

    // ── نجمة عثمانية بجانب الهلال ───────────────────────────────────────────
    val starR  = radius * 0.42f
    val starCx = center.x + radius * 1.70f
    val starCy = center.y - radius * 0.10f
    drawSparkStar(Offset(starCx, starCy), starR, pulse, spin)
}

// ─────────────────────────────────────────────────────────────────────────────
// الفانوس المغربي / العثماني الواقعي
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawMoroccanLantern(
    swing: Float,
    pulse: Float,
    scale: Float,
    bodyColor: Color,
    glowColor: Color
) {
    val ax = 22f * scale   // محور التأرجح (x)
    rotate(swing, Offset(ax, 0f)) {

        // ─ خيط علوي ────────────────────────────────────────────────────────
        val wireEnd = 12f * scale
        drawLine(GL.copy(0.7f), Offset(ax, 0f), Offset(ax, wireEnd), 1.4f)

        // ─ حلقة التعليق ────────────────────────────────────────────────────
        drawCircle(GD, 3.2f * scale, Offset(ax, wireEnd), style = Stroke(1.6f))
        drawCircle(GL.copy(0.35f), 2f * scale, Offset(ax, wireEnd))

        // ─ أبعاد الجسم ──────────────────────────────────────────────────────
        val top   = wireEnd + 3f * scale
        val bodyH = 46f * scale
        val bodyW = 28f * scale
        val cMid  = top + bodyH / 2f
        // نقطة الخصر (منتصف الجسم — أضيق)
        val waistY = top + bodyH * 0.50f
        val waistW = bodyW * 0.62f    // عرض الخصر

        // ─ halo ─────────────────────────────────────────────────────────────
        drawCircle(
            Brush.radialGradient(
                listOf(
                    glowColor.copy(pulse * 0.30f),
                    glowColor.copy(pulse * 0.10f),
                    Color.Transparent
                ),
                Offset(ax, cMid), bodyW * 2f
            ),
            bodyW * 2f, Offset(ax, cMid)
        )

        // ─ جسم الفانوس الواقعي (فانوس مغربي 6 ألواح) ───────────────────────
        // النصف الأعلى (من قمة لخصر)
        val topHalf = Path().apply {
            moveTo(ax, top)
            // الجانب الأيمن — ينتفخ ثم يضيق للخصر
            cubicTo(
                ax + bodyW * 0.75f, top + bodyH * 0.05f,
                ax + bodyW,          top + bodyH * 0.22f,
                ax + waistW,         waistY
            )
            lineTo(ax - waistW, waistY)
            cubicTo(
                ax - bodyW,          top + bodyH * 0.22f,
                ax - bodyW * 0.75f, top + bodyH * 0.05f,
                ax, top
            )
            close()
        }
        // النصف الأسفل (من خصر للقاع)
        val botHalf = Path().apply {
            moveTo(ax + waistW, waistY)
            cubicTo(
                ax + bodyW,          top + bodyH * 0.78f,
                ax + bodyW * 0.65f, top + bodyH * 0.96f,
                ax, top + bodyH
            )
            cubicTo(
                ax - bodyW * 0.65f, top + bodyH * 0.96f,
                ax - bodyW,          top + bodyH * 0.78f,
                ax - waistW,         waistY
            )
            lineTo(ax + waistW, waistY)
            close()
        }

        // تعبئة الجسم
        drawPath(topHalf, Brush.verticalGradient(
            listOf(bodyColor.lighten(0.18f), bodyColor, bodyColor.darken(0.1f)),
            top, waistY
        ))
        drawPath(botHalf, Brush.verticalGradient(
            listOf(bodyColor.darken(0.1f), bodyColor, bodyColor.lighten(0.12f)),
            waistY, top + bodyH
        ))

        // ─ ألواح الزجاج (اللون الشفاف الداخلي) ─────────────────────────────
        val glassAlpha = pulse * 0.75f
        // لوح زجاج أيمن
        val glR = Path().apply {
            moveTo(ax + waistW * 0.5f, waistY - 2f)
            cubicTo(ax + bodyW * 0.85f, top + bodyH * 0.28f, ax + bodyW * 0.85f, top + bodyH * 0.72f, ax + waistW * 0.5f, waistY + 2f)
            close()
        }
        drawPath(glR, glowColor.copy(glassAlpha * 0.55f))
        // لوح زجاج أيسر
        val glL = Path().apply {
            moveTo(ax - waistW * 0.5f, waistY - 2f)
            cubicTo(ax - bodyW * 0.85f, top + bodyH * 0.28f, ax - bodyW * 0.85f, top + bodyH * 0.72f, ax - waistW * 0.5f, waistY + 2f)
            close()
        }
        drawPath(glL, glowColor.copy(glassAlpha * 0.55f))
        // لوح أمامي
        val glFront = Path().apply {
            moveTo(ax, top + bodyH * 0.08f)
            cubicTo(ax + bodyW * 0.4f, top + bodyH * 0.18f, ax + bodyW * 0.4f, top + bodyH * 0.82f, ax, top + bodyH * 0.92f)
            cubicTo(ax - bodyW * 0.4f, top + bodyH * 0.82f, ax - bodyW * 0.4f, top + bodyH * 0.18f, ax, top + bodyH * 0.08f)
            close()
        }
        drawPath(glFront, glowColor.copy(glassAlpha * 0.35f))

        // ─ شبكة الفانوس (خطوط عثمانية) ─────────────────────────────────────
        val metalCol = GD.copy(0.55f)
        val metalW   = 0.9f
        // حدود الجسمين
        drawPath(topHalf, GD, style = Stroke(1.8f * scale))
        drawPath(botHalf, GD, style = Stroke(1.8f * scale))
        // خطوط عمودية
        for (d in listOf(-bodyW * 0.38f, 0f, bodyW * 0.38f)) {
            drawLine(metalCol, Offset(ax + d, top + bodyH * 0.06f), Offset(ax + d, top + bodyH * 0.94f), metalW)
        }
        // خطوط أفقية
        for (frac in listOf(0.25f, 0.5f, 0.75f)) {
            val fy = top + bodyH * frac
            val hw = if (frac == 0.5f) waistW else bodyW * 0.9f * sin(frac * PI.toFloat()).toFloat()
            drawLine(metalCol, Offset(ax - hw, fy), Offset(ax + hw, fy), metalW)
        }
        // خط الخصر
        drawLine(GD.copy(0.85f), Offset(ax - waistW, waistY), Offset(ax + waistW, waistY), 1.4f)

        // ─ التاج العثماني المزخرف ────────────────────────────────────────────
        val crownBot = top
        val crownH   = 11f * scale
        val crownW   = bodyW * 0.52f
        val crown    = Path().apply {
            moveTo(ax - crownW, crownBot)
            val points = 7
            for (p in 0..points) {
                val px = ax - crownW + p * (crownW * 2f / points)
                when {
                    p % 2 == 0 -> {
                        lineTo(px - crownW * 0.06f, crownBot - crownH * 0.3f)
                        // قوس الرأس
                        cubicTo(
                            px - crownW * 0.06f, crownBot - crownH,
                            px + crownW * 0.06f, crownBot - crownH,
                            px + crownW * 0.06f, crownBot - crownH * 0.3f
                        )
                    }
                    else -> lineTo(px, crownBot - crownH * 0.12f)
                }
            }
            lineTo(ax + crownW, crownBot)
            close()
        }
        drawPath(crown, Brush.verticalGradient(listOf(GL, GD, GD.copy(0.8f)), crownBot - crownH, crownBot))
        drawPath(crown, GD.copy(0.9f), style = Stroke(0.9f))

        // ─ القاعدة المنقوشة ──────────────────────────────────────────────────
        val baseY = top + bodyH
        val baseW = bodyW * 0.68f
        // جسم القاعدة
        val base = Path().apply {
            moveTo(ax - baseW, baseY)
            lineTo(ax - baseW * 0.7f, baseY + 5f * scale)
            lineTo(ax + baseW * 0.7f, baseY + 5f * scale)
            lineTo(ax + baseW, baseY)
            close()
        }
        drawPath(base, Brush.verticalGradient(listOf(GD, GD.copy(0.6f)), baseY, baseY + 5f * scale))
        // شريط ذهبي
        drawLine(
            Brush.horizontalGradient(listOf(Color.Transparent, GL.copy(0.7f), GL, GL.copy(0.7f), Color.Transparent)),
            Offset(ax - baseW * 0.85f, baseY + 2.5f * scale),
            Offset(ax + baseW * 0.85f, baseY + 2.5f * scale),
            1.4f
        )
        // شراريب 5
        for (ti in 0 until 5) {
            val tx = ax - baseW * 0.65f + ti * (baseW * 1.3f / 4f)
            val th = if (ti % 2 == 0) 13f * scale else 9f * scale
            val baseEnd = baseY + 5f * scale
            drawLine(
                Brush.verticalGradient(listOf(GD.copy(0.9f), GD.copy(0.15f)), baseEnd, baseEnd + th),
                Offset(tx, baseEnd), Offset(tx, baseEnd + th), 1.2f
            )
            drawCircle(
                Brush.radialGradient(listOf(GL, GD), Offset(tx, baseEnd + th), 2.8f * scale),
                2.8f * scale, Offset(tx, baseEnd + th)
            )
        }

        // ─ شرارة الضوء المركزية ──────────────────────────────────────────────
        val lightCy = cMid - bodyH * 0.02f
        drawCircle(glowColor.copy(pulse * 0.22f), 9f * scale, Offset(ax, lightCy))
        drawCircle(glowColor.copy(pulse * 0.55f), 5f * scale, Offset(ax, lightCy))
        drawCircle(GL.copy(pulse * 0.90f),         2.5f * scale, Offset(ax, lightCy))
        drawCircle(Color.White.copy(pulse * 0.95f), 1.2f * scale, Offset(ax, lightCy))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// نجمة 8 رؤوس مع وميض وتدوير
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawSparkStar(center: Offset, r: Float, alpha: Float, spin: Float) {
    val inner = r * 0.38f
    val path  = Path()
    for (i in 0 until 16) {
        val angle = Math.toRadians((spin * 0.5f + i * 22.5).toDouble()).toFloat()
        val rad   = if (i % 2 == 0) r else inner
        val x = center.x + rad * cos(angle)
        val y = center.y + rad * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    // طبقات التوهج
    drawCircle(GL.copy(alpha * 0.12f), r * 2.8f, center)
    drawCircle(GL.copy(alpha * 0.22f), r * 1.8f, center)
    // جسم النجمة
    drawPath(path, Brush.radialGradient(
        listOf(Color.White.copy(alpha * 0.95f), GL.copy(alpha), GD.copy(alpha * 0.85f)),
        center, r * 1.1f
    ))
    drawPath(path, GD.copy(alpha * 0.4f), style = Stroke(0.6f))
    // مركز لامع
    drawCircle(Color.White.copy(alpha * 0.9f), r * 0.18f, center)
}

// ─────────────────────────────────────────────────────────────────────────────
// مساعدات
// ─────────────────────────────────────────────────────────────────────────────
private fun Color.lighten(v: Float) = Color(
    (red   + v).coerceIn(0f, 1f),
    (green + v).coerceIn(0f, 1f),
    (blue  + v).coerceIn(0f, 1f),
    alpha
)
private fun Color.darken(v: Float)  = lighten(-v)

private val GD           = Color(0xFFD4A843)   // Gold
private val GL           = Color(0xFFF5D47A)   // Gold Light
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
private val SwingEasing   = CubicBezierEasing(0.40f, 0.02f, 0.60f, 0.98f)
private const val TWO_PI  = (PI * 2).toFloat()