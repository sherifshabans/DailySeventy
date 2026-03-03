package com.elsharif.dailyseventy.presentation.tree

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

// ══════════════════════════════════════════════════════════════════════════════
//  TIME OF DAY
// ══════════════════════════════════════════════════════════════════════════════

private enum class TimeOfDay { FAJR, MORNING, AFTERNOON, MAGHRIB, NIGHT }

data class SkyTheme(
    val skyTop: Color, val skyMid: Color, val skyBottom: Color,
    val groundTop: Color, val groundBot: Color, val fogColor: Color,
    val greetingRes: Int,
    val icon: String,
    val hasSun: Boolean, val sunPosX: Float, val sunPosY: Float,
    val sunColor: Color, val sunGlow: Color,
    val hasMoon: Boolean, val moonPosX: Float, val moonPosY: Float,
    val hasStars: Boolean, val starAlpha: Float, val leafTint: Float,
    val ambientColor: Color,
    val mtn1: Color, val mtn2: Color, val mtn3: Color,
    val godRayColor: Color, val fogDensity: Float,
    val lightIntensity: Float,
    val hasAurora: Boolean = false,
    val auroraAlpha: Float = 0f,
    val hasMilkyWay: Boolean = false,
    val milkyWayAlpha: Float = 0f,
    val hasFireflies: Boolean = false,
    val fireflyAlpha: Float = 0f,
    val hasBirds: Boolean = false,
    val hasPollen: Boolean = false,
    val dewIntensity: Float = 0f,
    val heatHaze: Float = 0f,
    val horizonBloom: Float = 0f,
    val horizonColor: Color = Color.Transparent,
    val hasCloudShadows: Boolean = false,
    val cloudShadowAlpha: Float = 0f
)

private fun getTimeOfDay(): TimeOfDay {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) {
        in 4..5   -> TimeOfDay.FAJR
        in 6..11  -> TimeOfDay.MORNING
        in 12..16 -> TimeOfDay.AFTERNOON
        in 17..19 -> TimeOfDay.MAGHRIB
        else      -> TimeOfDay.NIGHT
    }
}

private fun buildSkyTheme(tod: TimeOfDay) = when (tod) {
    TimeOfDay.FAJR -> SkyTheme(
        skyTop = Color(0xFF0D1B4B), skyMid = Color(0xFF5C2070), skyBottom = Color(0xFFFF8A65),
        groundTop = Color(0xFF2E7D32), groundBot = Color(0xFF0A1A0A),
        fogColor = Color(0xFFFF7043),
        greetingRes = R.string.tree_greeting_fajr,
        icon = "🌅",
        hasSun = true, sunPosX = 0.50f, sunPosY = 0.88f,
        sunColor = Color(0xFFFF7043), sunGlow = Color(0xFFFFCCBC),
        hasMoon = true, moonPosX = 0.18f, moonPosY = 0.13f,
        hasStars = true, starAlpha = 0.60f, leafTint = 0.40f,
        ambientColor = Color(0xFFFF7043),
        mtn1 = Color(0xFF1A0D35), mtn2 = Color(0xFF2E1060), mtn3 = Color(0xFF3D1A6A),
        godRayColor = Color(0xFFFF8A65), fogDensity = 0.20f, lightIntensity = 0.35f,
        hasAurora = true, auroraAlpha = 0.55f,
        hasFireflies = true, fireflyAlpha = 0.60f,
        dewIntensity = 0.70f,
        horizonBloom = 0.80f, horizonColor = Color(0xFFFF8A65)
    )
    TimeOfDay.MORNING -> SkyTheme(
        skyTop = Color(0xFF1565C0), skyMid = Color(0xFF1E88E5), skyBottom = Color(0xFF90CAF9),
        groundTop = Color(0xFF4CAF50), groundBot = Color(0xFF1B5E20),
        fogColor = Color(0xFF90CAF9),
        greetingRes = R.string.tree_greeting_morning,
        icon = "☀️",
        hasSun = true, sunPosX = 0.76f, sunPosY = 0.18f,
        sunColor = Color(0xFFFDD835), sunGlow = Color(0xFFFFF9C4),
        hasMoon = false, moonPosX = 0f, moonPosY = 0f,
        hasStars = false, starAlpha = 0f, leafTint = 0f,
        ambientColor = Color(0xFFFFF9C4),
        mtn1 = Color(0xFF1A237E), mtn2 = Color(0xFF283593), mtn3 = Color(0xFF3949AB),
        godRayColor = Color(0xFFFFF9C4), fogDensity = 0.04f, lightIntensity = 0.85f,
        hasBirds = true, hasPollen = true,
        dewIntensity = 0.90f,
        horizonBloom = 0.40f, horizonColor = Color(0xFFFFE082),
        hasCloudShadows = true, cloudShadowAlpha = 0.06f
    )
    TimeOfDay.AFTERNOON -> SkyTheme(
        skyTop = Color(0xFF0277BD), skyMid = Color(0xFF039BE5), skyBottom = Color(0xFF4FC3F7),
        groundTop = Color(0xFF558B2F), groundBot = Color(0xFF1B5E20),
        fogColor = Color(0xFF4FC3F7),
        greetingRes = R.string.tree_greeting_afternoon,
        icon = "🌤️",
        hasSun = true, sunPosX = 0.82f, sunPosY = 0.10f,
        sunColor = Color(0xFFFFF59D), sunGlow = Color(0xFFFFFDE7),
        hasMoon = false, moonPosX = 0f, moonPosY = 0f,
        hasStars = false, starAlpha = 0f, leafTint = 0f,
        ambientColor = Color(0xFFFFFDE7),
        mtn1 = Color(0xFF0D47A1), mtn2 = Color(0xFF1565C0), mtn3 = Color(0xFF1976D2),
        godRayColor = Color(0xFFFFFDE7), fogDensity = 0.03f, lightIntensity = 1.0f,
        hasBirds = true, hasPollen = true,
        heatHaze = 0.75f,
        hasCloudShadows = true, cloudShadowAlpha = 0.09f
    )
    TimeOfDay.MAGHRIB -> SkyTheme(
        skyTop = Color(0xFF4A148C), skyMid = Color(0xFFBF360C), skyBottom = Color(0xFFFF8F00),
        groundTop = Color(0xFF2E7D32), groundBot = Color(0xFF061206),
        fogColor = Color(0xFFFF6F00),
        greetingRes = R.string.tree_greeting_maghrib,
        icon = "🌇",
        hasSun = true, sunPosX = 0.10f, sunPosY = 0.82f,
        sunColor = Color(0xFFFF6D00), sunGlow = Color(0xFFFFAB40),
        hasMoon = false, moonPosX = 0f, moonPosY = 0f,
        hasStars = true, starAlpha = 0.28f, leafTint = 0.30f,
        ambientColor = Color(0xFFFF6F00),
        mtn1 = Color(0xFF1A0028), mtn2 = Color(0xFF2D0040), mtn3 = Color(0xFF3D004E),
        godRayColor = Color(0xFFFF6D00), fogDensity = 0.16f, lightIntensity = 0.30f,
        hasFireflies = true, fireflyAlpha = 0.35f,
        horizonBloom = 1.0f, horizonColor = Color(0xFFFF6D00)
    )
    TimeOfDay.NIGHT -> SkyTheme(
        skyTop = Color(0xFF020408), skyMid = Color(0xFF060A18), skyBottom = Color(0xFF0A1428),
        groundTop = Color(0xFF1B5E20), groundBot = Color(0xFF020802),
        fogColor = Color(0xFF5C6BC0),
        greetingRes = R.string.tree_greeting_night,
        icon = "🌙",
        hasSun = false, sunPosX = 0f, sunPosY = 0f,
        sunColor = Color.Transparent, sunGlow = Color.Transparent,
        hasMoon = true, moonPosX = 0.68f, moonPosY = 0.12f,
        hasStars = true, starAlpha = 1f, leafTint = 0.65f,
        ambientColor = Color(0xFF3949AB),
        mtn1 = Color(0xFF04081A), mtn2 = Color(0xFF070C20), mtn3 = Color(0xFF091026),
        godRayColor = Color(0xFFE8EAF6), fogDensity = 0.07f, lightIntensity = 0.08f,
        hasMilkyWay = true, milkyWayAlpha = 0.90f,
        hasFireflies = true, fireflyAlpha = 1.0f
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  PARTICLE SYSTEMS
// ══════════════════════════════════════════════════════════════════════════════

private class FallingLeaf(maxW: Float, maxH: Float) {
    var x = Random.nextFloat() * maxW
    var y = Random.nextFloat() * maxH
    val size = Random.nextFloat() * 7f + 3.5f
    var vy = Random.nextFloat() * 0.7f + 0.25f
    var vx = Random.nextFloat() * 0.4f - 0.2f
    var rot = Random.nextFloat() * 360f
    val rotV = Random.nextFloat() * 2.5f - 1.25f
    val alpha = Random.nextFloat() * 0.55f + 0.30f
    val hue = Random.nextInt(4)
    var wobble = Random.nextFloat() * PI.toFloat() * 2f
    val wobbleSpeed = Random.nextFloat() * 0.025f + 0.01f
    val wobbleAmp = Random.nextFloat() * 0.5f + 0.15f
    fun update(sway: Float) {
        wobble += wobbleSpeed
        y += vy; x += vx + sway * 0.35f + sin(wobble) * wobbleAmp; rot += rotV
        if (y > 2000f) { y = -20f; x = Random.nextFloat() * 2000f }
    }
}

private class WindParticle {
    var x = Random.nextFloat(); var y = Random.nextFloat() * 0.6f
    val speed = Random.nextFloat() * 0.0008f + 0.0004f
    val alpha = Random.nextFloat() * 0.06f + 0.02f
    val length = Random.nextFloat() * 40f + 15f
    val yDrift = Random.nextFloat() * 0.0002f - 0.0001f
    fun update(sway: Float) {
        x += speed * (1f + sway * 0.3f); y += yDrift
        if (x > 1.05f) { x = -0.05f; y = Random.nextFloat() * 0.6f }
    }
}

private class FireflyParticle {
    var x = Random.nextFloat()
    var y = Random.nextFloat() * 0.22f + 0.60f
    private var vx = Random.nextFloat() * 0.00055f - 0.000275f
    private var vy = Random.nextFloat() * 0.00040f - 0.000200f
    var pulsePhase = Random.nextFloat() * PI.toFloat() * 2f
    private val pulseSpeed = Random.nextFloat() * 0.06f + 0.03f
    val maxGlow = Random.nextFloat() * 0.70f + 0.40f
    val glowColor = if (Random.nextBoolean()) Color(0xFFCCFF90) else Color(0xFFFFFF8D)
    val coreColor = if (glowColor == Color(0xFFCCFF90)) Color(0xFFF1F8E9) else Color(0xFFFFFEF0)
    val size = Random.nextFloat() * 2.2f + 1.4f
    private val baseY = y
    fun update(sway: Float) {
        pulsePhase += pulseSpeed
        x += vx + sway * 0.00025f + sin(pulsePhase * 0.28f) * 0.00018f
        y = baseY + sin(pulsePhase * 0.41f) * 0.018f + cos(pulsePhase * 0.22f) * 0.009f
        if (x < 0f) x = 1f; if (x > 1f) x = 0f
    }
    fun glowAlpha() = (sin(pulsePhase) * 0.5f + 0.5f).coerceIn(0f, 1f) * maxGlow
}

private class BirdParticle {
    var x = Random.nextFloat()
    val yBase = Random.nextFloat() * 0.32f + 0.04f
    var y = yBase
    val speed = Random.nextFloat() * 0.00035f + 0.00018f
    val size = Random.nextFloat() * 2.8f + 3.5f
    var wingPhase = Random.nextFloat() * PI.toFloat() * 2f
    private val wingSpeed = Random.nextFloat() * 0.14f + 0.09f
    val direction = if (Random.nextBoolean()) 1f else -1f
    private val yWaveAmp = Random.nextFloat() * 0.012f + 0.004f
    private val yWaveFreq = Random.nextFloat() * 0.18f + 0.08f
    fun update() {
        wingPhase += wingSpeed
        x += speed * direction
        y = yBase + sin(wingPhase * yWaveFreq * 10f) * yWaveAmp
        if (x > 1.12f) x = -0.12f
        if (x < -0.12f) x = 1.12f
    }
    fun wingLift() = sin(wingPhase)
}

private class PollenParticle {
    var x = Random.nextFloat()
    var y = Random.nextFloat() * 0.55f
    private val baseSpeed = Random.nextFloat() * 0.00025f + 0.00008f
    private var driftPhase = Random.nextFloat() * PI.toFloat() * 2f
    val alpha = Random.nextFloat() * 0.50f + 0.18f
    val size = Random.nextFloat() * 1.6f + 0.7f
    val color = if (Random.nextBoolean()) Color(0xFFFFF59D) else Color(0xFFFFECB3)
    fun update(sway: Float) {
        driftPhase += 0.018f
        x += baseSpeed + sway * 0.00025f + sin(driftPhase) * 0.000085f
        y -= 0.000065f + cos(driftPhase * 0.62f) * 0.000040f
        if (x > 1.06f) { x = -0.06f; y = Random.nextFloat() * 0.55f }
        if (y < -0.03f) { y = 0.58f; x = Random.nextFloat() }
    }
}

private val STAR_SEEDS = List(90) {
    Triple(Random.nextFloat(), Random.nextFloat() * 0.55f, Random.nextFloat() * 2.4f + 0.5f)
}
private val BRIGHT_STARS = listOf(
    Triple(0.12f, 0.08f, 3.8f), Triple(0.18f, 0.14f, 3.2f), Triple(0.25f, 0.07f, 4.1f),
    Triple(0.34f, 0.19f, 3.5f), Triple(0.42f, 0.05f, 4.4f), Triple(0.51f, 0.22f, 3.0f),
    Triple(0.60f, 0.10f, 3.8f), Triple(0.68f, 0.28f, 4.2f), Triple(0.76f, 0.06f, 3.5f),
    Triple(0.84f, 0.18f, 4.0f), Triple(0.91f, 0.08f, 3.3f), Triple(0.96f, 0.24f, 3.7f),
    Triple(0.08f, 0.32f, 3.2f), Triple(0.22f, 0.40f, 3.6f), Triple(0.48f, 0.36f, 4.0f),
    Triple(0.65f, 0.44f, 3.4f), Triple(0.78f, 0.35f, 3.8f), Triple(0.90f, 0.42f, 4.3f)
)

private val ISLAMIC_QUOTES = listOf(
    "«سبحان الله وبحمده، سبحان الله العظيم»",
    "«من قال سبحان الله وبحمده مائة مرة حُطَّت خطاياه وإن كانت مثل زبد البحر»",
    "«الباقيات الصالحات: سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر»",
    "«أحب الكلام إلى الله أربع: سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر»",
    "«إن الذاكرين الله كثيراً والذاكرات أعدَّ الله لهم مغفرةً وأجراً عظيماً»",
    "«ذكر الله شفاء القلوب ونور الأرواح»",
    "«لا يزال لسانك رطباً من ذكر الله»"
)

// ══════════════════════════════════════════════════════════════════════════════
//  BRANCH SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

private data class Br(
    val x1: Float, val y1: Float, val x2: Float, val y2: Float,
    val w: Float, val depth: Int, val angle: Float
)

private fun buildBranches(
    x: Float, y: Float, angle: Float, len: Float, w: Float,
    depth: Int, maxD: Int, sway: Float, phase: Float
): List<Br> {
    val list = mutableListOf<Br>()
    val windMag = if (depth > 0)
        sway * depth * 1.2f + sin(phase + depth * 0.8f + angle * 0.05f) * depth * 0.7f
    else 0f
    val actualAngle = angle + windMag
    val rad = Math.toRadians(actualAngle.toDouble())
    val ex = x + (cos(rad) * len).toFloat()
    val ey = y - (sin(rad) * len).toFloat()
    list.add(Br(x, y, ex, ey, w, depth, actualAngle))
    if (depth < maxD) {
        val childLen = len * (0.62f + sin(depth * 0.4f + angle * 0.01f) * 0.04f)
        val childW   = (w * 0.56f).coerceAtLeast(0.9f)
        val spread   = 20f + depth * 4f
        val asymL    = spread  + sin(depth * 1.3f) * 3f
        val asymR    = -spread + cos(depth * 1.1f) * 2f
        list.addAll(buildBranches(ex, ey, actualAngle + asymL, childLen, childW, depth+1, maxD, sway, phase))
        list.addAll(buildBranches(ex, ey, actualAngle + asymR, childLen, childW, depth+1, maxD, sway, phase))
        if (depth < maxD - 2 && depth % 2 == 0)
            list.addAll(buildBranches(ex, ey, actualAngle + 6f, childLen * 0.68f, childW * 0.58f, depth+1, maxD, sway, phase))
    }
    return list
}
// ══════════════════════════════════════════════════════════════════════════════
//  DRAWING HELPERS (same as original — kept intact)
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawMilkyWay(W: Float, gY: Float, alpha: Float) {
    if (alpha < 0.02f) return
    drawRect(
        Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.25f to Color(0xFF7986CB).copy(alpha = alpha * 0.045f),
                0.50f to Color(0xFFCE93D8).copy(alpha = alpha * 0.075f),
                0.75f to Color(0xFF7986CB).copy(alpha = alpha * 0.045f),
                1.00f to Color.Transparent
            ),
            start = Offset(W * 0.10f, 0f), end = Offset(W * 0.90f, gY * 0.65f)
        ), size = Size(W, gY * 0.65f)
    )
    repeat(100) { i ->
        val t  = i.toFloat() / 100f
        val bx = t * W + sin(i * 0.77f + 1.2f) * W * 0.18f
        val by = t * gY * 0.58f + cos(i * 0.45f) * gY * 0.10f
        if (by > 0 && by < gY * 0.60f) {
            val a  = alpha * (0.08f + (i % 5) * 0.035f) * (0.45f + abs(sin(i * 1.77f)) * 0.55f)
            val sz = 0.5f + (i % 4) * 0.3f
            drawCircle(Color.White.copy(alpha = a), sz, Offset(bx, by))
        }
    }
}

private fun DrawScope.drawAurora(W: Float, gY: Float, alpha: Float, phase: Float) {
    if (alpha < 0.01f) return
    val auroraColors = listOf(
        Color(0xFF69F0AE), Color(0xFF40C4FF), Color(0xFFCE93D8),
        Color(0xFF80DEEA), Color(0xFFA5D6A7)
    )
    repeat(5) { i ->
        val yBase = gY * (0.10f + i * 0.055f)
        val path  = Path()
        val steps = 24
        for (xi in 0..steps) {
            val px = xi.toFloat() / steps * W
            val wave =
                sin(xi * 0.36f + phase * 0.8f + i * 1.28f) * gY * 0.055f +
                        cos(xi * 0.22f + phase * 0.55f + i * 0.82f) * gY * 0.032f +
                        sin(xi * 0.10f + phase * 0.30f) * gY * 0.018f
            if (xi == 0) path.moveTo(px, yBase + wave) else path.lineTo(px, yBase + wave)
        }
        val col    = auroraColors[i % auroraColors.size]
        val gAlpha = alpha * (0.055f + i * 0.018f)
        drawPath(path, col.copy(alpha = gAlpha * 0.30f), style = Stroke(28f + i * 10f, cap = StrokeCap.Round))
        drawPath(path, col.copy(alpha = gAlpha * 0.55f), style = Stroke(12f + i * 4f,  cap = StrokeCap.Round))
        drawPath(path, col.copy(alpha = gAlpha),         style = Stroke(2.5f,           cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawSky(
    W: Float, gY: Float, sky: SkyTheme,
    lightX: Float, lightY: Float, celestial: Float, rayAlpha: Float, rayShift: Float,
    twA: Float, twB: Float, twC: Float, twD: Float,
    cDrift: Float, cDrift2: Float, auroraPhase: Float
) {
    drawRect(
        Brush.verticalGradient(listOf(sky.skyTop, sky.skyMid, sky.skyMid, sky.skyBottom), 0f, gY),
        size = Size(W, gY)
    )
    drawRect(
        Brush.verticalGradient(listOf(Color.Transparent, sky.skyBottom.copy(alpha = 0.60f)), gY * 0.72f, gY),
        Offset(0f, gY * 0.72f), Size(W, gY * 0.28f)
    )
    if (sky.horizonBloom > 0.01f) {
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, sky.horizonColor.copy(alpha = sky.horizonBloom * 0.45f), Color.Transparent),
                gY * 0.60f, gY + 20f
            ),
            Offset(0f, gY * 0.60f), Size(W, gY * 0.45f)
        )
        val cX = lightX.coerceIn(0f, W)
        drawCircle(
            Brush.radialGradient(
                listOf(sky.horizonColor.copy(alpha = sky.horizonBloom * 0.28f), Color.Transparent),
                Offset(cX, gY), W * 0.70f
            ), W * 0.70f, Offset(cX, gY)
        )
    }
    if (sky.hasMilkyWay) drawMilkyWay(W, gY, sky.milkyWayAlpha)
    if (sky.hasAurora)   drawAurora(W, gY, sky.auroraAlpha, auroraPhase)
    if (sky.hasStars && sky.starAlpha > 0f) {
        val tws = listOf(twA, twB, twC, twD)
        STAR_SEEDS.forEachIndexed { i, (rx, ry, r) ->
            val al = (sky.starAlpha * tws[i % 4] * (0.38f + (i % 6) * 0.10f)).coerceIn(0f, 1f)
            val sx = rx * W; val sy = ry * gY
            drawCircle(Color.White.copy(alpha = al), r, Offset(sx, sy))
            if (r > 1.6f) {
                drawCircle(Color.White.copy(alpha = al * 0.13f), r * 4.0f, Offset(sx, sy))
                drawLine(Color.White.copy(alpha = al * 0.38f), Offset(sx - r*3.2f, sy), Offset(sx + r*3.2f, sy), r * 0.25f)
                drawLine(Color.White.copy(alpha = al * 0.38f), Offset(sx, sy - r*3.2f), Offset(sx, sy + r*3.2f), r * 0.25f)
            }
        }
        BRIGHT_STARS.forEachIndexed { i, (rx, ry, r) ->
            val al = sky.starAlpha * tws[i % 4] * 0.90f
            val sx = rx * W; val sy = ry * gY
            drawCircle(Color(0xFFE8EAF6).copy(alpha = al * 0.12f), r * 6f, Offset(sx, sy))
            drawCircle(Color.White.copy(alpha = al * 0.55f), r * 1.5f, Offset(sx, sy))
            drawCircle(Color.White.copy(alpha = al), r * 0.55f, Offset(sx, sy))
            repeat(4) { d ->
                val ang = Math.toRadians(d * 45.0)
                val len = r * (3.5f + (i % 3) * 1.5f)
                drawLine(Color.White.copy(alpha = al * 0.30f),
                    Offset(sx + (cos(ang) * r).toFloat(), sy + (sin(ang) * r).toFloat()),
                    Offset(sx + (cos(ang) * len).toFloat(), sy + (sin(ang) * len).toFloat()),
                    0.5f, cap = StrokeCap.Round)
            }
        }
    }
    if (sky.hasSun || sky.hasMoon) {
        repeat(10) { i ->
            val laneX = (i.toFloat() / 9f) * 0.75f + 0.125f
            val ang   = (-PI / 2.0 + (laneX - 0.5) * PI * 0.80).toFloat()
            val dist  = gY * 2.0f
            val ex    = lightX + cos(ang) * dist
            val ey    = lightY + sin(ang) * dist
            val anim  = (0.013f + (i % 3) * 0.007f) * rayAlpha * sky.lightIntensity
            drawLine(sky.godRayColor.copy(alpha = anim), Offset(lightX, lightY), Offset(ex, ey), 16f + (i % 4) * 14f)
        }
    }
    if (sky.hasSun) {
        val sx = sky.sunPosX * W; val sy = sky.sunPosY * gY; val sr = 26f * celestial
        drawCircle(sky.sunGlow.copy(alpha = 0.05f * sky.lightIntensity), sr * 7.5f, Offset(sx, sy))
        drawCircle(sky.sunGlow.copy(alpha = 0.11f * sky.lightIntensity), sr * 4.5f, Offset(sx, sy))
        drawCircle(sky.sunGlow.copy(alpha = 0.20f * sky.lightIntensity), sr * 2.5f, Offset(sx, sy))
        drawCircle(
            Brush.radialGradient(listOf(Color.White, sky.sunColor.copy(alpha = 0.88f), sky.sunColor.copy(alpha = 0f)), Offset(sx, sy), sr * 1.5f),
            sr * 1.2f, Offset(sx, sy)
        )
        drawCircle(
            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.98f), sky.sunColor.copy(alpha = 0.92f)), Offset(sx - sr * 0.15f, sy - sr * 0.15f), sr * 1.2f),
            sr, Offset(sx, sy)
        )
        drawCircle(Color.White.copy(alpha = 0.58f), sr * 0.20f, Offset(sx - sr*0.22f, sy - sr*0.25f))
        if (sky.sunPosY < 0.5f) {
            repeat(14) { i ->
                val rad   = Math.toRadians(i * 25.71 + rayShift * 360.0)
                val inner = sr + 2f; val outer = sr + 9f + (i % 3) * 7f
                drawLine(sky.sunColor.copy(alpha = 0.32f),
                    Offset(sx + (cos(rad)*inner).toFloat(), sy + (sin(rad)*inner).toFloat()),
                    Offset(sx + (cos(rad)*outer).toFloat(), sy + (sin(rad)*outer).toFloat()),
                    if (i%3==0) 1.8f else 1.0f, cap = StrokeCap.Round)
            }
        }
    }
    if (sky.hasMoon) {
        val mx = sky.moonPosX * W; val my = sky.moonPosY * gY; val mr = 21f * celestial
        drawCircle(Color(0xFFE8EAF6).copy(alpha = 0.06f), mr * 4.0f, Offset(mx, my))
        drawCircle(Color(0xFFE8EAF6).copy(alpha = 0.04f), mr * 5.8f, Offset(mx, my))
        drawCircle(
            Brush.radialGradient(listOf(Color(0xFFF5F5F5), Color(0xFFE8E8E8), Color(0xFFBDBDBD)), Offset(mx, my), mr),
            mr, Offset(mx, my)
        )
        listOf(Offset(-0.28f, 0.20f) to 0.22f, Offset(0.30f, -0.12f) to 0.14f,
            Offset(0.06f,  0.38f) to 0.11f, Offset(-0.10f,-0.40f) to 0.08f).forEach { (p, s) ->
            drawCircle(Color(0xFF9E9E9E).copy(alpha = 0.22f), mr*s, Offset(mx + p.x*mr, my + p.y*mr))
            drawCircle(Color(0xFFEEEEEE).copy(alpha = 0.16f), mr*s*0.42f, Offset(mx + p.x*mr - mr*s*0.2f, my + p.y*mr - mr*s*0.2f))
        }
        drawCircle(sky.skyTop.copy(alpha = 0.96f), mr * 0.78f, Offset(mx + mr*0.42f, my - mr*0.08f))
    }
    drawMountainRange(W, gY, sky.mtn1, 0.70f, W * 0.00f, 6, sky.fogDensity * 0.55f)
    drawMountainRange(W, gY, sky.mtn2, 0.84f, W * 0.04f, 4, sky.fogDensity * 0.28f)
    drawMountainRange(W, gY, sky.mtn3, 0.93f, W * (-0.03f), 3, 0f)
    drawDistantForest(W, gY, sky)
    if (!sky.hasStars || sky.lightIntensity > 0.25f) {
        val d1 = cDrift * W * 0.18f; val d2 = cDrift2 * W * 0.14f
        drawNaturalCloud(W*0.09f+d1,       gY*0.09f,  40f, 0.82f, sky)
        drawNaturalCloud(W*0.60f+d2,       gY*0.06f,  29f, 0.76f, sky)
        drawNaturalCloud(W*0.37f+d1*0.55f, gY*0.18f,  23f, 0.70f, sky)
        drawNaturalCloud(W*0.83f-d2*0.42f, gY*0.13f,  25f, 0.73f, sky)
        drawNaturalCloud(W*0.20f+d2*0.35f, gY*0.26f,  18f, 0.65f, sky)
    }
}

private fun DrawScope.drawDistantForest(W: Float, gY: Float, sky: SkyTheme) {
    val baseY   = gY * 0.930f
    val treeCol = lerp(sky.mtn3, sky.groundTop, 0.22f)
    val fogCol  = Color(treeCol.red*0.38f+sky.fogColor.red*0.62f, treeCol.green*0.38f+sky.fogColor.green*0.62f, treeCol.blue*0.38f+sky.fogColor.blue*0.62f, 0.72f)
    val path = Path()
    path.moveTo(0f, gY); path.lineTo(0f, baseY)
    var tx = -24f
    while (tx < W + 24f) {
        val hRaw = gY * (0.048f + abs(sin(tx * 0.068f + 1.4f)) * 0.048f)
        val tw   = 5.5f + abs(sin(tx * 0.11f)) * 4.5f
        path.lineTo(tx - tw * 0.5f, baseY)
        path.cubicTo(tx - tw*0.5f, baseY - hRaw*0.3f, tx, baseY - hRaw, tx, baseY - hRaw)
        path.cubicTo(tx, baseY - hRaw, tx + tw*0.5f, baseY - hRaw*0.3f, tx + tw*0.5f, baseY)
        tx += tw + 3.5f + abs(sin(tx * 0.05f + 0.9f)) * 6.5f
    }
    path.lineTo(W, baseY); path.lineTo(W, gY); path.close()
    drawPath(path, Brush.verticalGradient(listOf(fogCol.copy(alpha = 0.52f), fogCol), baseY - gY * 0.055f, gY))
}

private fun DrawScope.drawMountainRange(W: Float, gY: Float, color: Color, scale: Float, offsetX: Float, peaks: Int, fogBlend: Float) {
    val path  = Path()
    val baseY = gY * (0.97f - scale * 0.20f)
    path.moveTo(0f, gY); path.lineTo(0f, baseY)
    val total = peaks + 2
    for (i in 0..total) {
        val px    = offsetX + (i.toFloat() / total) * (W * 1.18f) - W * 0.09f
        val peakH = gY * 0.10f + abs(sin(i * 2.4f + scale * 4.8f)) * gY * 0.20f * scale
        val ph    = baseY - peakH
        val np    = offsetX + ((i+1f) / total) * (W * 1.18f) - W * 0.09f
        path.cubicTo(px - 18f, ph + 5f, px + 10f, ph, px, ph)
        path.cubicTo(px + 20f, ph, np - 18f, if (i==total) gY else ph + gY*0.06f, np, if (i==total) gY else ph + gY*0.08f)
    }
    path.lineTo(W, gY); path.close()
    val foggedColor = if (fogBlend > 0f) Color(
        color.red   + fogBlend * (0.50f - color.red),
        color.green + fogBlend * (0.55f - color.green),
        color.blue  + fogBlend * (0.65f - color.blue), 1f
    ) else color
    drawPath(path, Brush.verticalGradient(listOf(foggedColor.copy(alpha = 0.78f), foggedColor.copy(alpha = 0.94f)), gY * 0.38f, gY))
    if (scale < 0.75f) {
        val sp = Path(); var started = false
        for (i in 0..total) {
            val px = offsetX + (i.toFloat() / total) * (W * 1.18f) - W * 0.09f
            val ph = baseY - (gY * 0.10f + abs(sin(i * 2.4f + scale * 4.8f)) * gY * 0.20f * scale)
            val cap = ph + gY * 0.028f
            if (!started) { sp.moveTo(px, cap); started = true } else sp.lineTo(px, cap)
        }
        drawPath(sp, Color.White.copy(alpha = 0.18f), style = Stroke(2.0f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawNaturalCloud(cx: Float, cy: Float, r: Float, alpha: Float, sky: SkyTheme) {
    val shadowCol = if (sky.lightIntensity > 0.5f) Color(0xFF607D8B) else Color(0xFF37474F)
    drawCircle(shadowCol.copy(alpha = alpha * 0.24f), r * 0.92f, Offset(cx + r*0.07f, cy + r*0.60f))
    drawCircle(Color.White.copy(alpha = alpha * 0.60f), r * 1.08f, Offset(cx - r*0.04f, cy + r*0.09f))
    drawCircle(Color.White.copy(alpha = alpha),          r,          Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = alpha * 0.90f), r * 0.80f, Offset(cx + r*0.94f, cy + r*0.18f))
    drawCircle(Color.White.copy(alpha = alpha * 0.86f), r * 0.70f, Offset(cx - r*0.84f, cy + r*0.22f))
    drawCircle(Color.White.copy(alpha = alpha * 0.92f), r * 0.58f, Offset(cx + r*0.32f, cy - r*0.34f))
    drawCircle(Color.White.copy(alpha = alpha * 0.88f), r * 0.50f, Offset(cx - r*0.38f, cy - r*0.26f))
    drawCircle(Color.White.copy(alpha = alpha * 0.78f), r * 0.42f, Offset(cx + r*0.62f, cy - r*0.18f))
    drawCircle(Color.White.copy(alpha = 0.40f), r*0.28f, Offset(cx - r*0.22f, cy - r*0.44f))
    drawCircle(Color.White.copy(alpha = 0.22f), r*0.12f, Offset(cx + r*0.14f, cy - r*0.52f))
}

private fun DrawScope.drawGround(W: Float, gY: Float, H: Float, sky: SkyTheme, shimmer: Float, sway: Float, dewIntensity: Float, heatHaze: Float) {
    drawRect(Brush.verticalGradient(listOf(sky.groundTop, sky.groundBot), gY, H), Offset(0f, gY), Size(W, H - gY))
    val patches = listOf(
        Offset(W*0.08f, gY+10f) to 18f, Offset(W*0.25f, gY+6f)  to 14f,
        Offset(W*0.45f, gY+14f) to 24f, Offset(W*0.68f, gY+8f)  to 17f,
        Offset(W*0.85f, gY+12f) to 20f, Offset(W*0.15f, gY+22f) to 12f,
        Offset(W*0.58f, gY+25f) to 16f, Offset(W*0.78f, gY+20f) to 13f
    )
    patches.forEach { (pos, r) ->
        drawCircle(Color(0xFF061206).copy(alpha = 0.26f), r, pos)
        drawCircle(Color(0xFF0A1A08).copy(alpha = 0.13f), r * 0.55f, pos)
    }
    val grassDefs = listOf(
        Triple(Color(0xFF1A5C1A), 22f, 30f), Triple(Color(0xFF2E7D32), 18f, 22f),
        Triple(Color(0xFF388E3C), 14f, 17f), Triple(Color(0xFF4CAF50), 11f, 14f),
        Triple(Color(0xFF81C784),  8f, 11f)
    )
    grassDefs.forEachIndexed { li, (col, maxH, spacing) ->
        val yBase    = gY + li * 2.8f
        val windBend = sway * (2.5f - li * 0.4f)
        var gx = -(spacing * 0.5f)
        while (gx < W + spacing) {
            val ht   = maxH * (0.60f + sin(gx * 0.08f + li * 1.3f) * 0.22f + cos(gx * 0.13f + li * 0.7f) * 0.18f)
            val tipX = gx + windBend + sin(gx * 0.05f) * 1.5f
            val tipY = yBase - ht
            drawLine(Color(0xFF061206).copy(alpha = 0.28f), Offset(gx + 1.2f, yBase), Offset(tipX + 1.0f, tipY + 1.5f), 1.0f + li*0.15f, cap = StrokeCap.Round)
            drawLine(col.copy(alpha = 0.90f - li * 0.04f), Offset(gx, yBase), Offset(tipX, tipY), 1.2f + li*0.20f, cap = StrokeCap.Round)
            if (li >= 2) drawLine(Color(0xFFC8E6C9).copy(alpha = 0.30f - li * 0.05f), Offset(tipX - windBend*0.25f, tipY + ht*0.25f), Offset(tipX, tipY), 0.5f, cap = StrokeCap.Round)
            gx += spacing * (0.85f + sin(gx * 0.04f) * 0.15f)
        }
    }
    val shimmerA = 0.04f + 0.025f * sin(shimmer)
    drawRect(Brush.radialGradient(listOf(Color(0xFF81C784).copy(alpha = shimmerA), Color.Transparent), Offset(W * 0.5f, gY + 5f), W * 0.55f), Offset(0f, gY), Size(W, (H - gY) * 0.35f))
    if (dewIntensity > 0.01f) drawDewSparkles(W, gY, dewIntensity)
    if (heatHaze > 0.01f)     drawHeatHaze(W, gY, heatHaze)
    if (sky.hasCloudShadows && sky.cloudShadowAlpha > 0.01f) {
        drawRect(Brush.radialGradient(listOf(Color.Black.copy(alpha = sky.cloudShadowAlpha * 0.8f), Color.Transparent), Offset(W * 0.62f, gY + 8f), W * 0.30f), Offset(0f, gY), Size(W, (H - gY) * 0.12f))
    }
    if (sky.fogDensity > 0.03f) {
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, sky.fogColor.copy(alpha = sky.fogDensity * 0.95f), Color.Transparent), gY - 80f, gY + 55f), Offset(0f, gY - 80f), Size(W, 135f))
        repeat(6) { i ->
            val tx = W * (0.08f + i * 0.16f)
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, sky.fogColor.copy(alpha = sky.fogDensity * 0.25f), Color.Transparent), gY - 40f, gY + 30f), Offset(tx - 18f, gY - 40f), Size(36f, 70f))
        }
    }
}

private fun DrawScope.drawDewSparkles(W: Float, gY: Float, intensity: Float) {
    repeat(35) { i ->
        val dx = (sin(i * 2.67f + 0.8f) * 0.5f + 0.5f) * W
        val dy = gY + 3f + (i % 9) * 3.5f
        val sparkA = intensity * (0.50f + abs(sin(i * 1.85f + 0.5f)) * 0.50f)
        drawCircle(Color.White.copy(alpha = sparkA * 0.85f), 1.4f + (i % 3) * 0.5f, Offset(dx, dy))
        if (i % 5 == 0) {
            drawLine(Color.White.copy(alpha = sparkA * 0.45f), Offset(dx - 3.5f, dy), Offset(dx + 3.5f, dy), 0.5f)
            drawLine(Color.White.copy(alpha = sparkA * 0.45f), Offset(dx, dy - 3.5f), Offset(dx, dy + 3.5f), 0.5f)
        }
    }
}

private fun DrawScope.drawHeatHaze(W: Float, gY: Float, intensity: Float) {
    repeat(3) { i ->
        val bandY = gY + i * 5f
        drawRect(
            Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.3f to Color(0xFFFFECB3).copy(alpha = intensity * 0.04f),
                    0.7f to Color(0xFF4FC3F7).copy(alpha = intensity * 0.03f),
                    1.0f to Color.Transparent
                ),
                start = Offset(0f, bandY), end = Offset(W, bandY + 4f)
            ),
            Offset(0f, bandY), Size(W, 4f)
        )
    }
}

private fun DrawScope.drawRocks(cx: Float, gY: Float) {
    listOf(Triple(cx-40f,gY+6f,9f), Triple(cx+46f,gY+5f,7.2f), Triple(cx-24f,gY+9f,5.5f),
        Triple(cx+20f,gY+8f,6f), Triple(cx-60f,gY+7f,8f),  Triple(cx+62f,gY+6f,5.8f)).forEach { (rx, ry, rs) ->
        drawOval(Color.Black.copy(alpha = 0.18f), Offset(rx - rs + 2.5f, ry - rs*0.3f + 2.5f), Size(rs*2f, rs*0.65f))
        drawCircle(Brush.radialGradient(listOf(Color(0xFF78909C), Color(0xFF546E7A), Color(0xFF37474F)), Offset(rx - rs*0.20f, ry - rs*0.30f), rs * 1.25f), rs, Offset(rx, ry))
        drawCircle(Color(0xFF90A4AE).copy(alpha = 0.42f), rs * 0.26f, Offset(rx - rs*0.28f, ry - rs*0.30f))
        drawCircle(Color(0xFF558B2F).copy(alpha = 0.30f), rs * 0.55f, Offset(rx + rs*0.15f, ry + rs*0.05f))
    }
}

private fun DrawScope.drawMushrooms(cx: Float, gY: Float) {
    listOf(Triple(cx-52f,gY+2f,5.8f), Triple(cx+58f,gY+3f,4.2f), Triple(cx-72f,gY+4f,3.5f)).forEach { (mx, my, ms) ->
        drawRect(Brush.linearGradient(listOf(Color(0xFFEFEBE9), Color(0xFFD7CCC8)), Offset(mx - ms*0.28f, my - ms*1.5f), Offset(mx + ms*0.28f, my)), Offset(mx - ms*0.28f, my - ms*1.5f), Size(ms*0.56f, ms*1.5f))
        drawOval(Color.Black.copy(alpha = 0.18f), Offset(mx - ms*1.22f + 2f, my - ms*2.1f + 2f), Size(ms*2.44f, ms*0.82f))
        val capPath = Path()
        capPath.moveTo(mx - ms*1.22f, my - ms*1.48f)
        capPath.cubicTo(mx - ms*1.35f, my - ms*2.90f, mx + ms*1.35f, my - ms*2.90f, mx + ms*1.22f, my - ms*1.48f)
        capPath.quadraticBezierTo(mx, my - ms*1.20f, mx - ms*1.22f, my - ms*1.48f)
        capPath.close()
        drawPath(capPath, Brush.radialGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828), Color(0xFF8B0000)), Offset(mx, my - ms*2.4f), ms*1.6f))
        listOf(-0.42f to -2.0f, 0.38f to -1.9f, 0.0f to -2.5f, -0.18f to -2.3f).forEach { (ox, oy) ->
            drawCircle(Color.White.copy(alpha = 0.78f), ms * 0.17f, Offset(mx + ox*ms, my + oy*ms))
        }
    }
}

private fun DrawScope.drawWildflowers(W: Float, gY: Float, flowerCount: Int) {
    if (flowerCount == 0) return
    val colors = listOf(Color(0xFFFF80AB), Color(0xFFFFF176), Color(0xFFFF6E40), Color(0xFFE040FB), Color(0xFF40C4FF), Color(0xFFFF8A65))
    repeat((flowerCount * 9).coerceAtMost(50)) { i ->
        val fx = (sin(i * 2.39f + 1.31f) * 0.5f + 0.5f) * W
        val fy = gY + 5f + (i % 6) * 3.8f
        if (abs(fx - W * 0.5f) < 28f) return@repeat
        val col = colors[i % colors.size]
        val sz  = 2.2f + (i % 4) * 0.8f
        val stemBend = sin(i * 0.7f) * 2.5f
        drawLine(Color(0xFF388E3C), Offset(fx, fy), Offset(fx + stemBend, fy - 9f - sz), 0.9f, cap = StrokeCap.Round)
        val petalCount = if (i % 3 == 0) 6 else 5
        repeat(petalCount) { p ->
            val ang = Math.toRadians(p * (360.0 / petalCount) + i * 14.0)
            drawCircle(col.copy(alpha = 0.82f), sz * 0.68f, Offset(fx + stemBend + (cos(ang) * sz * 1.18f).toFloat(), (fy - 9f - sz) + (sin(ang) * sz * 1.18f).toFloat()))
        }
        drawCircle(Color(0xFFFFF9C4), sz * 0.46f, Offset(fx + stemBend, fy - 9f - sz))
    }
}

private fun DrawScope.drawFallenLeafPile(cx: Float, gY: Float, W: Float, leafTint: Float) {
    if (leafTint < 0.15f) return
    val leafColors = listOf(Color(0xFFD84315), Color(0xFFF57F17), Color(0xFF827717), Color(0xFF2E7D32))
    repeat(16) { i ->
        val angle = Math.toRadians(i * 22.5)
        val dist  = 12f + (i % 5) * 9f
        val lx    = cx + (cos(angle) * dist).toFloat()
        val ly    = gY + 2f + (i % 4) * 1.8f
        val lSize = 4f + (i % 3) * 2.5f
        withTransform({ rotate(i * 22.5f + 10f, Offset(lx, ly)) }) {
            drawOval(leafColors[i % leafColors.size].copy(alpha = 0.55f * leafTint), Offset(lx - lSize, ly - lSize * 0.38f), Size(lSize * 2f, lSize * 0.75f))
        }
    }
}

private fun DrawScope.drawBark(x1: Float, y1: Float, x2: Float, y2: Float, w: Float, lightDir: Offset, depth: Int) {
    val len = hypot(x2 - x1, y2 - y1).coerceAtLeast(1f)
    val dx = x2 - x1; val dy = y2 - y1
    val nx = -dy / len; val ny = dx / len
    val cShadow    = lerp(Color(0xFF120700), Color(0xFF2A1005), depth / 6f)
    val cBase      = lerp(Color(0xFF3B1A06), Color(0xFF5C2E0A), depth / 6f)
    val cMid       = lerp(Color(0xFF6B3A10), Color(0xFF8C5020), depth / 6f)
    val cLight     = lerp(Color(0xFF9A6228), Color(0xFFB87C3C), depth / 6f)
    val cHighlight = Color(0xFFD4A060).copy(alpha = (0.52f - depth * 0.08f).coerceAtLeast(0f))
    val litFactor  = ((lightDir.x * nx + lightDir.y * ny) * 0.5f + 0.5f).coerceIn(0f, 1f)
    drawLine(cShadow, Offset(x1, y1), Offset(x2, y2), w, cap = StrokeCap.Round)
    drawLine(cBase.copy(alpha = 0.90f), Offset(x1, y1), Offset(x2, y2), w * 0.85f, cap = StrokeCap.Round)
    if (w > 2.5f) {
        val shift = (litFactor * 2f - 1f) * w * 0.22f
        drawLine(cMid.copy(alpha = 0.78f), Offset(x1 + nx * shift, y1 + ny * shift), Offset(x2 + nx * shift, y2 + ny * shift), w * 0.55f, cap = StrokeCap.Round)
    }
    if (w > 4.5f) drawLine(cLight.copy(alpha = 0.55f * litFactor + 0.10f), Offset(x1 + nx * w * 0.30f, y1 + ny * w * 0.30f), Offset(x2 + nx * w * 0.30f, y2 + ny * w * 0.30f), w * 0.24f, cap = StrokeCap.Round)
    if (w > 7f)   drawLine(cHighlight, Offset(x1 + nx * w * 0.42f, y1 + ny * w * 0.42f), Offset(x2 + nx * w * 0.42f, y2 + ny * w * 0.42f), w * 0.08f, cap = StrokeCap.Round)
    if (w > 9f && depth <= 1) {
        val steps = (len / 11f).toInt().coerceIn(3, 16)
        repeat(steps) { i ->
            val t    = (i + 0.5f) / steps
            val mx2  = x1 + dx * t; val my2 = y1 + dy * t
            val halfW = w * (0.30f + (1f - t) * 0.10f)
            val gA    = 0.10f + (i * 7 % 5) * 0.04f
            drawLine(cShadow.copy(alpha = gA * 1.5f), Offset(mx2 - nx * halfW, my2 - ny * halfW), Offset(mx2 + nx * halfW * 0.55f, my2 + ny * halfW * 0.55f), if (i % 3 == 0) 1.3f else 0.8f, cap = StrokeCap.Round)
            drawLine(cLight.copy(alpha = gA * 0.60f), Offset(mx2 - nx * halfW * 0.2f, my2 - ny * halfW * 0.2f), Offset(mx2 + nx * halfW * 0.42f, my2 + ny * halfW * 0.42f), 0.5f, cap = StrokeCap.Round)
        }
    }
    if (depth == 0 && w > 16f) {
        val kX = x1 + dx * 0.36f; val kY = y1 + dy * 0.36f
        drawOval(cShadow.copy(alpha = 0.62f), Offset(kX - w*0.24f, kY - w*0.14f), Size(w*0.48f, w*0.28f))
        drawOval(cBase.copy(alpha = 0.46f),   Offset(kX - w*0.16f, kY - w*0.09f), Size(w*0.32f, w*0.18f))
        drawOval(cMid.copy(alpha = 0.28f),    Offset(kX - w*0.08f, kY - w*0.04f), Size(w*0.16f, w*0.08f))
    }
}

private fun lerp(a: Color, b: Color, t: Float): Color {
    val tc = t.coerceIn(0f, 1f)
    return Color(a.red+(b.red-a.red)*tc, a.green+(b.green-a.green)*tc, a.blue+(b.blue-a.blue)*tc, 1f)
}

private fun DrawScope.drawRoots(cx: Float, gY: Float, trunkW: Float, lightDir: Offset) {
    listOf(Triple(-44f, trunkW*2.1f, 0.52f), Triple(-23f, trunkW*1.7f, 0.62f), Triple(2f, trunkW*1.3f, 0.48f), Triple(25f, trunkW*1.8f, 0.60f), Triple(46f, trunkW*1.9f, 0.50f)).forEach { (angleDeg, len, wFactor) ->
        val rad = Math.toRadians(angleDeg.toDouble())
        val ex  = cx + (cos(rad) * len).toFloat()
        val ey  = gY + (abs(sin(rad)) * len * 0.42f).toFloat()
        val rW  = trunkW * wFactor
        drawLine(Color.Black.copy(alpha = 0.20f), Offset(cx, gY+5f), Offset(ex+4f, ey+6f), rW*1.12f, cap = StrokeCap.Round)
        drawLine(Color(0xFF120700), Offset(cx, gY), Offset(ex, ey), rW, cap = StrokeCap.Round)
        drawLine(Color(0xFF5C2E0A).copy(alpha = 0.80f), Offset(cx, gY), Offset(ex, ey), rW*0.52f, cap = StrokeCap.Round)
        val lFactor = ((lightDir.x * cos(rad) + lightDir.y * sin(rad)).toFloat() * 0.5f + 0.5f)
        drawLine(Color(0xFF9A6228).copy(alpha = 0.30f * lFactor), Offset(cx, gY), Offset(ex, ey), rW*0.20f, cap = StrokeCap.Round)
        val rad2 = rad + (if (angleDeg < 0) 0.35 else -0.35)
        val sx   = ex + (cos(rad2) * len * 0.38f).toFloat()
        val sy   = ey + (abs(sin(rad2)) * len * 0.22f).toFloat()
        drawLine(Color(0xFF3B1A06).copy(alpha = 0.75f), Offset(ex, ey), Offset(sx, sy), rW*0.32f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawFoliage(cx: Float, cy: Float, radius: Float, breath: Float, idx: Int, total: Int, leaves: Int, flowers: Int, fruits: Int, nightTint: Float, sway: Float, lightDir: Offset, lightIntensity: Float) {
    val s     = idx.toFloat()
    val r     = radius * breath * (0.80f + sin(s * 2.39f) * 0.11f + cos(s * 1.71f) * 0.06f)
    val dim   = 1f - nightTint * 0.32f
    val depthFade = 0.65f + (idx.toFloat() / total.coerceAtLeast(1)) * 0.35f
    val windX = sway * (1.0f + idx.toFloat() / total.coerceAtLeast(1) * 1.2f)
    drawCircle(Color(0xFF020A02).copy(alpha = 0.28f * depthFade), r * 0.82f, Offset(cx + r*0.08f + windX*0.6f, cy + r*0.16f))
    data class FBlob(val ox: Float, val oy: Float, val rScale: Float, val dark: Float)
    val blobDefs = listOf(
        FBlob(sin(s*0.1f)*0.06f,   cos(s*0.13f)*0.06f,  1.00f, 0.00f),
        FBlob(-0.48f+sin(s*0.3f)*0.08f, -0.25f+cos(s*0.2f)*0.06f, 0.82f, 0.15f),
        FBlob( 0.46f+sin(s*0.4f)*0.08f, -0.23f+cos(s*0.5f)*0.06f, 0.80f, 0.15f),
        FBlob(-0.26f+sin(s*0.6f)*0.07f,  0.44f+cos(s*0.7f)*0.05f, 0.76f, 0.20f),
        FBlob( 0.32f+sin(s*0.8f)*0.07f,  0.42f+cos(s*0.9f)*0.05f, 0.74f, 0.20f),
        FBlob( 0.02f+sin(s*1.0f)*0.06f, -0.60f+cos(s*1.1f)*0.04f, 0.72f, 0.25f),
        FBlob(-0.66f+sin(s*1.2f)*0.07f,  0.04f+cos(s*1.3f)*0.05f, 0.68f, 0.30f),
        FBlob( 0.63f+sin(s*1.4f)*0.07f,  0.06f+cos(s*1.5f)*0.05f, 0.65f, 0.30f),
        FBlob(-0.18f+sin(s*1.6f)*0.06f, -0.52f+cos(s*1.7f)*0.04f, 0.62f, 0.35f),
        FBlob( 0.22f+sin(s*1.8f)*0.06f, -0.50f+cos(s*1.9f)*0.04f, 0.60f, 0.35f),
        FBlob(-0.40f+sin(s*2.0f)*0.06f,  0.20f+cos(s*2.1f)*0.04f, 0.57f, 0.38f),
        FBlob( 0.38f+sin(s*2.2f)*0.06f,  0.22f+cos(s*2.3f)*0.04f, 0.55f, 0.38f),
        FBlob(-0.10f+sin(s*2.4f)*0.05f, -0.72f+cos(s*2.5f)*0.03f, 0.46f, 0.42f),
        FBlob( 0.14f+sin(s*2.6f)*0.05f, -0.70f+cos(s*2.7f)*0.03f, 0.44f, 0.42f),
        FBlob( 0.72f+sin(s*2.8f)*0.05f, -0.18f+cos(s*2.9f)*0.03f, 0.48f, 0.45f),
        FBlob(-0.70f+sin(s*3.0f)*0.05f, -0.16f+cos(s*3.1f)*0.03f, 0.50f, 0.45f),
        FBlob( 0.55f+sin(s*3.2f)*0.04f,  0.44f+cos(s*3.3f)*0.03f, 0.42f, 0.48f),
        FBlob(-0.52f+sin(s*3.4f)*0.04f,  0.46f+cos(s*3.5f)*0.03f, 0.44f, 0.48f),
        FBlob( 0.02f+sin(s*3.6f)*0.04f,  0.72f+cos(s*3.7f)*0.03f, 0.40f, 0.50f),
        FBlob(-0.28f+sin(s*3.8f)*0.04f,  0.64f+cos(s*3.9f)*0.03f, 0.38f, 0.52f)
    )
    blobDefs.forEach { b ->
        val bx  = cx + b.ox * r + windX * (0.7f + b.rScale * 0.6f)
        val by  = cy + b.oy * r
        val br  = r * b.rScale * (1f + sin(s * 0.5f + b.ox * 2.8f) * 0.05f)
        val exp = ((-b.ox * lightDir.x - b.oy * lightDir.y) * 0.5f + 0.5f).coerceIn(0f, 1f)
        val darkVal  = lerp(Color(0xFF0D3B0D), Color(0xFF1B5E20), b.dark.coerceIn(0f,1f))
        val litColor = lerp(Color(0xFF2E7D32), Color(0xFF81C784), exp)
        val finalCol = lerp(darkVal, litColor, (exp * lightIntensity * 0.7f + 0.20f).coerceIn(0f,1f))
        val bAlpha   = (0.84f - b.dark * 0.28f) * dim * depthFade
        drawCircle(finalCol.copy(alpha = bAlpha), br, Offset(bx, by))
    }
    if (lightIntensity > 0.15f && idx % 2 == 0) {
        repeat(8) { li ->
            val ang  = Math.toRadians(li * 45.0 + s * 12.0)
            val lx   = cx + (cos(ang) * r * 0.92f).toFloat() + windX
            val ly   = cy + (sin(ang) * r * 0.88f).toFloat()
            val lsz  = 5f + (li % 3) * 2.5f
            val lCol = lerp(Color(0xFF1B5E20), Color(0xFF4CAF50), (li.toFloat()/8f + lightIntensity * 0.4f).coerceIn(0f,1f))
            withTransform({ rotate((li * 45f + s * 12f + 90f).toFloat(), Offset(lx, ly)) }) {
                drawOval(lCol.copy(alpha = 0.70f * dim), Offset(lx - lsz, ly - lsz*0.38f), Size(lsz*2f, lsz*0.76f))
                drawLine(lerp(lCol, Color(0xFF0D3B0D), 0.4f).copy(alpha = 0.45f * dim), Offset(lx - lsz*0.6f, ly), Offset(lx + lsz*0.6f, ly), 0.5f)
            }
        }
    }
    val sssAlpha = 0.14f * dim * lightIntensity
    if (sssAlpha > 0.01f) {
        val sssX = cx - lightDir.x * r * 0.38f + windX * 0.5f
        val sssY = cy - lightDir.y * r * 0.38f
        drawCircle(Brush.radialGradient(listOf(Color(0xFF8DC63F).copy(alpha = sssAlpha), Color(0xFF4CAF50).copy(alpha = sssAlpha*0.4f), Color.Transparent), Offset(sssX, sssY), r * 0.70f), r * 0.70f, Offset(sssX, sssY))
    }
    val specAlpha = 0.12f * dim * lightIntensity
    val specX = cx - lightDir.x * r * 0.28f + windX * 0.5f
    val specY = cy - lightDir.y * r * 0.28f
    drawCircle(Color.White.copy(alpha = specAlpha), r * 0.38f, Offset(specX, specY))
    drawCircle(Color.White.copy(alpha = specAlpha * 0.55f), r * 0.16f, Offset(specX - r*0.06f, specY - r*0.08f))
    drawCircle(Color(0xFF061206).copy(alpha = 0.22f * dim * depthFade), r * 1.04f, Offset(cx + windX * 0.5f, cy), style = Stroke(r * 0.038f))
    if (flowers > 0 && idx % 3 == 1) drawFlower(cx + r*.38f + windX, cy - r*.54f, 6.5f + (flowers/4f).coerceAtMost(6f))
    if (flowers > 3 && idx % 4 == 0) drawFlower(cx - r*.52f + windX, cy - r*.30f, 5.5f + (flowers/6f).coerceAtMost(5f))
    if (fruits  > 0 && idx % 4 == 2) drawFruit( cx - r*.42f + windX, cy + r*.46f, 7.5f + (fruits /5f).coerceAtMost(7f))
    if (fruits  > 4 && idx % 5 == 3) drawFruit( cx + r*.52f + windX, cy + r*.32f, 6.5f + (fruits /7f).coerceAtMost(6f))
}

private fun DrawScope.drawCanopyLightShafts(branches: List<Br>, gY: Float, lightDir: Offset, lightIntensity: Float, rayAlpha: Float) {
    if (lightIntensity < 0.35f) return
    branches.filter { it.depth >= 3 }.take(10).forEachIndexed { i, br ->
        val sx  = br.x2; val sy = br.y2
        val endX = sx - lightDir.x * 65f
        val endY = (sy - lightDir.y * 65f + 60f).coerceAtMost(gY)
        val shaftAlpha = rayAlpha * lightIntensity * 0.032f * (1f - i * 0.06f).coerceAtLeast(0f)
        drawLine(
            Brush.linearGradient(listOf(Color(0xFFFFF9C4).copy(alpha = shaftAlpha), Color.Transparent), Offset(sx, sy), Offset(endX, endY)),
            Offset(sx, sy), Offset(endX, endY), 7f + (i % 3) * 5f, cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawTreeShadow(cx: Float, gY: Float, tL: Float, tW: Float, lightDir: Offset, lightIntensity: Float) {
    if (lightIntensity < 0.05f) return
    val shadowAngle = atan2(lightDir.x.toDouble(), (-lightDir.y).toDouble()).toFloat()
    val shadowLen   = tL * (1.8f - lightIntensity * 0.9f)
    val shadowAlpha = 0.11f * lightIntensity
    val tipX        = cx + sin(shadowAngle) * shadowLen
    drawOval(Brush.radialGradient(listOf(Color.Black.copy(alpha = shadowAlpha * 1.8f), Color.Transparent), Offset((cx + tipX) / 2f, gY + 6f), shadowLen * 0.65f), Offset(cx.coerceAtMost(tipX) - 20f, gY - 4f), Size(abs(tipX - cx) + 40f, 22f))
    drawLine(Brush.linearGradient(listOf(Color.Black.copy(alpha = shadowAlpha * 1.5f), Color.Transparent), Offset(cx, gY), Offset(tipX, gY + 12f)), Offset(cx, gY), Offset(tipX, gY + 8f), tW * 0.65f, cap = StrokeCap.Round)
}

private fun DrawScope.drawFireflies(particles: List<FireflyParticle>, W: Float, gY: Float, alpha: Float) {
    particles.forEach { ff ->
        val gA = ff.glowAlpha() * alpha
        if (gA < 0.04f) return@forEach
        val fx = ff.x * W; val fy = ff.y * gY
        drawCircle(ff.glowColor.copy(alpha = gA * 0.07f), ff.size * 9f, Offset(fx, fy))
        drawCircle(ff.glowColor.copy(alpha = gA * 0.16f), ff.size * 4.5f, Offset(fx, fy))
        drawCircle(ff.glowColor.copy(alpha = gA * 0.40f), ff.size * 2.2f, Offset(fx, fy))
        drawCircle(ff.glowColor.copy(alpha = gA),          ff.size,         Offset(fx, fy))
        drawCircle(ff.coreColor.copy(alpha = gA * 0.95f),  ff.size * 0.45f, Offset(fx, fy))
    }
}

private fun DrawScope.drawBirds(birds: List<BirdParticle>, W: Float, gY: Float, alpha: Float) {
    birds.forEach { bird ->
        val bx   = bird.x * W; val by = bird.y * gY
        val s    = bird.size * bird.direction
        val lift = bird.wingLift()
        val wAng = lift * 0.42f
        val col  = Color(0xFF1A1A2E).copy(alpha = alpha * 0.70f)
        val path = Path()
        path.moveTo(bx, by)
        path.cubicTo(bx - s * 0.75f, by - s * (0.45f + 0.40f * sin(wAng + 0.2f)), bx - s * 1.55f, by - s * (0.18f + 0.28f * sin(wAng)), bx - s * 2.10f, by - s * (0.08f + 0.18f * sin(wAng - 0.1f)))
        path.moveTo(bx, by)
        path.cubicTo(bx + s * 0.75f, by - s * (0.45f + 0.40f * sin(wAng + 0.2f)), bx + s * 1.55f, by - s * (0.18f + 0.28f * sin(wAng)), bx + s * 2.10f, by - s * (0.08f + 0.18f * sin(wAng - 0.1f)))
        drawCircle(col, s * 0.28f, Offset(bx, by))
        drawPath(path, col, style = Stroke(1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawPollen(particles: List<PollenParticle>, W: Float, gY: Float) {
    particles.forEach { p ->
        val px = p.x * W; val py = p.y * gY
        if (py > gY * 0.90f) return@forEach
        drawCircle(Color.White.copy(alpha = p.alpha * 0.40f), p.size * 2.0f, Offset(px, py))
        drawCircle(p.color.copy(alpha = p.alpha), p.size, Offset(px, py))
    }
}

private fun DrawScope.drawFlower(cx: Float, cy: Float, sz: Float) {
    drawLine(Color(0xFF2E7D32), Offset(cx, cy+sz*1.2f), Offset(cx, cy+sz*0.5f), sz*0.16f, cap=StrokeCap.Round)
    drawCircle(Color.Black.copy(alpha=0.16f), sz*1.08f, Offset(cx+1.5f, cy+2.5f))
    repeat(6) { i ->
        val rad = Math.toRadians(i*60.0 - 12.0)
        val pc  = Offset(cx+(cos(rad)*sz*.86f).toFloat(), cy+(sin(rad)*sz*.86f).toFloat())
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFCE4EC),Color(0xFFEC407A),Color(0xFFC2185B)),pc,sz*.72f), sz*.48f, pc)
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF9C4),Color(0xFFFBC02D)),Offset(cx,cy),sz*.42f), sz*.36f, Offset(cx,cy))
    drawCircle(Color.White.copy(alpha=0.55f), sz*.10f, Offset(cx-sz*.09f, cy-sz*.09f))
}

private fun DrawScope.drawFruit(cx: Float, cy: Float, sz: Float) {
    val sp = Path().apply { moveTo(cx, cy-sz); cubicTo(cx+sz*.20f, cy-sz*1.28f, cx+sz*.36f, cy-sz*1.55f, cx+sz*.32f, cy-sz*1.46f) }
    drawPath(sp, Color(0xFF388E3C), style = Stroke(sz*.16f, cap=StrokeCap.Round))
    withTransform({ rotate(26f, Offset(cx+sz*.08f, cy-sz*1.28f)) }) {
        drawOval(Color(0xFF4CAF50).copy(alpha=0.84f), Offset(cx+sz*.03f, cy-sz*1.38f), Size(sz*.55f, sz*.24f))
    }
    drawCircle(Color.Black.copy(alpha=0.24f), sz*1.08f, Offset(cx+2.5f, cy+3f))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFCDD2),Color(0xFFEF5350),Color(0xFFC62828),Color(0xFF8B0000)), Offset(cx-sz*.26f, cy-sz*.26f), sz*1.55f), sz, Offset(cx,cy))
    drawCircle(Color.White.copy(alpha=0.50f), sz*.24f, Offset(cx-sz*.30f, cy-sz*.30f))
    drawCircle(Color.White.copy(alpha=0.20f), sz*.10f, Offset(cx-sz*.16f, cy-sz*.46f))
    drawCircle(Color(0xFFFF8A65).copy(alpha=0.25f), sz*.82f, Offset(cx,cy), style=Stroke(sz*.07f))
}
// ══════════════════════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeScreen(onBackClick: () -> Unit, viewModel: TreeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val tod   = remember { getTimeOfDay() }
    val sky   = remember { buildSkyTheme(tod) }
    val quote = remember(state.level) { ISLAMIC_QUOTES[state.level % ISLAMIC_QUOTES.size] }
    var showLvlUp by remember { mutableStateOf(false) }
    var newLevel  by remember { mutableStateOf(0) }
    var showReset by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.levelUpEvent.collect { lvl -> newLevel = lvl; showLvlUp = true } }
    if (showLvlUp) LevelUpDialog(newLevel, sky) { showLvlUp = false }
    if (showReset) ResetConfirmDialog({ viewModel.resetTree(); showReset = false }, { showReset = false })

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(sky.icon, fontSize = 20.sp)
                    Column {
                        Text(
                            stringResource(sky.greetingRes),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.tree_level_points, state.level, state.totalPoints),
                            style = MaterialTheme.typography.labelMedium,
                            color = GreenStart
                        )
                    }
                }
            },
            navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            actions = { IconButton(onClick = { showReset = true }) { Icon(Icons.Default.RestartAlt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
    }) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                TreeCardWithButtons(
                    level              = state.level,
                    progress           = state.progress,
                    leaves             = state.leaves,
                    flowers            = state.flowers,
                    fruits             = state.fruits,
                    branchStrength     = state.branchStrength,
                    sky                = sky,
                    subhanallahCount   = state.subhanallahCount,
                    alhamdulillahCount = state.alhamdulillahCount,
                    allahuakbarCount   = state.allahuakbarCount,
                    laIlahaCount       = state.laIlahaCount,
                    hawqalaCount       = state.hawqalaCount,
                    astaghfirCount     = state.astaghfirCount,
                    salawatCount       = state.salawatCount,
                    bismillahCount     = state.bismillahCount,
                    mashallahCount     = state.mashallahCount,
                    onZikr             = { viewModel.addZikr(it) },
                    modifier           = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            item { QuoteCard(quote) }
            item { ProgressCard(state.level, state.progress, state.pointsToNext) }
            item { StatsRow(state.totalPoints, state.leaves, state.flowers, state.fruits, state.branchStrength) }
            item {
                ZikrCountersCard(
                    subhanallahCount   = state.subhanallahCount,
                    alhamdulillahCount = state.alhamdulillahCount,
                    allahuakbarCount   = state.allahuakbarCount,
                    laIlahaCount       = state.laIlahaCount,
                    hawqalaCount       = state.hawqalaCount,
                    astaghfirCount     = state.astaghfirCount,
                    salawatCount       = state.salawatCount,
                    bismillahCount     = state.bismillahCount,
                    mashallahCount     = state.mashallahCount
                )
            }
            if (state.activities.isNotEmpty()) item { ActivitiesList(state.activities) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ZIKR DATA MODEL (with resource IDs)
// ══════════════════════════════════════════════════════════════════════════════

private data class ZikrItem(
    val key: String,
    val arabicRes: Int,
    val pointsFormatRes: Int,
    val pointsValue: Int,
    val color: Color,
    val emoji: String,
    val count: Int
)

// ══════════════════════════════════════════════════════════════════════════════
//  TREE CARD + BUTTONS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TreeCardWithButtons(
    level: Int, progress: Float,
    leaves: Int, flowers: Int, fruits: Int,
    branchStrength: Int,
    sky: SkyTheme,
    subhanallahCount: Int, alhamdulillahCount: Int, allahuakbarCount: Int,
    laIlahaCount: Int, hawqalaCount: Int, astaghfirCount: Int,
    salawatCount: Int, bismillahCount: Int, mashallahCount: Int,
    onZikr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fps   = remember { mutableStateListOf<FP>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val zikrList = listOf(
        ZikrItem("subhanallah",   R.string.dhikr_short_subhanallah, R.string.plus_d, 10, Color(0xFF1B5E20), "🌿", subhanallahCount),
        ZikrItem("alhamdulillah", R.string.dhikr_short_hamd,        R.string.plus_d, 15, Color(0xFF0D47A1), "💙", alhamdulillahCount),
        ZikrItem("allahuakbar",   R.string.dhikr_short_akbar,       R.string.plus_d, 20, Color(0xFF4A148C), "✨", allahuakbarCount),
        ZikrItem("lailaha",       R.string.dhikr_short_tahleel,     R.string.plus_d, 25, Color(0xFF880E4F), "💎", laIlahaCount),
        ZikrItem("hawqala",       R.string.dhikr_short_hawqala,     R.string.plus_d, 15, Color(0xFF4E342E), "🤲", hawqalaCount),
        ZikrItem("astaghfir",     R.string.dhikr_short_istighfar,   R.string.plus_d, 10, Color(0xFF1A237E), "🌊", astaghfirCount),
        ZikrItem("salawat",       R.string.dhikr_short_salah,       R.string.plus_d, 20, Color(0xFFE65100), "🌙", salawatCount),
        ZikrItem("bismillah",     R.string.dhikr_short_bismillah,   R.string.plus_d, 5,  Color(0xFF2E7D32), "🌱", bismillahCount),
        ZikrItem("mashallah",     R.string.dhikr_short_mashallah,   R.string.plus_d, 5,  Color(0xFF00695C), "🌸", mashallahCount),
    )

    Card(modifier = modifier, shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(24.dp)) {
        Column {
            // ── Scene ────────────────────────────────────────────────────────
            Box(Modifier.fillMaxWidth().height(440.dp)) {
                NaturalTreeScene(
                    level = level, progress = progress,
                    leaves = leaves, flowers = flowers, fruits = fruits,
                    branchStrength = branchStrength,
                    sky = sky,
                    modifier = Modifier.fillMaxSize()
                )
                // Level badge
                Box(Modifier.fillMaxSize().padding(12.dp), Alignment.TopStart) {
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.42f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(sky.icon, fontSize = 12.sp)
                            Text(
                                stringResource(R.string.tree_level, level),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                // Prayer hint
                Box(Modifier.fillMaxSize().padding(12.dp), Alignment.BottomStart) {
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.30f)).padding(horizontal = 9.dp, vertical = 6.dp)) {
                        Text(
                            stringResource(getPrayerTimeHintRes(getTimeOfDay())),
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 9.sp
                        )
                    }
                }
                // Stage badge
                Box(Modifier.fillMaxSize().padding(bottom = 12.dp, end = 12.dp), Alignment.BottomEnd) {
                    val stageColor = getStageBadgeColor(level)
                    val (stageEmoji, stageLabelRes) = getStageParts(level)
                    Row(
                        Modifier.clip(RoundedCornerShape(50.dp)).background(Brush.linearGradient(listOf(stageColor, darken(stageColor, 0.20f)))).padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(stageEmoji, fontSize = 13.sp)
                        Text(
                            stringResource(stageLabelRes),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // ── Zikr Buttons Section ──────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f), MaterialTheme.colorScheme.surface)))
                    .padding(top = 16.dp, bottom = 18.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.tree_dhikr_prompt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.tree_each_dhikr_bears_fruit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(zikrList) { zikr ->
                        ZikrCard(zikr = zikr, onClick = {
                            onZikr(zikr.key)
                            fps.add(FP(
                                System.currentTimeMillis() + Random.nextLong(0, 999),
                                context.getString(R.string.plus_d, zikr.pointsValue) + " ✨",
                                zikr.color
                            ))
                        })
                    }
                }
            }

            // Float labels overlay
            Box(Modifier.fillMaxWidth().height(0.dp)) {
                fps.forEach { f ->
                    key(f.id) {
                        val scope2 = rememberCoroutineScope()
                        LaunchedEffect(f.id) { scope2.launch { delay(1400); fps.remove(f) } }
                        FloatLabel(f)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ZIKR CARD
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ZikrCard(zikr: ZikrItem, onClick:  () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "sc_${zikr.key}"
    )
    val scope = rememberCoroutineScope()
    val countAnim by animateIntAsState(targetValue = zikr.count, animationSpec = tween(400), label = "cnt_${zikr.key}")

    Box(
        Modifier
            .scale(scale)
            .width(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(zikr.color.copy(alpha = 0.13f), zikr.color.copy(alpha = 0.06f))))
            .border(1.dp, zikr.color.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .clickable {
                pressed = true
                scope.launch { delay(120); pressed = false }
                onClick()
            }
            .padding(vertical = 14.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(44.dp).background(zikr.color.copy(alpha = 0.15f), CircleShape).border(1.5.dp, zikr.color.copy(alpha = 0.30f), CircleShape), Alignment.Center) {
                Text(zikr.emoji, fontSize = 18.sp)
            }
            Text(
                stringResource(zikr.arabicRes),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 2
            )
            Surface(shape = RoundedCornerShape(8.dp), color = zikr.color.copy(alpha = 0.15f)) {
                Text(
                    stringResource(zikr.pointsFormatRes, zikr.pointsValue),
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = zikr.color
                )
            }
            Text(
                stringResource(R.string.tree_times_format, countAnim),
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  NATURAL TREE SCENE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun NaturalTreeScene(
    level: Int, progress: Float,
    leaves: Int, flowers: Int, fruits: Int,
    branchStrength: Int,
    sky: SkyTheme,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "scene")

    val w1 by inf.animateFloat(-1f,   1f,    infiniteRepeatable(tween(3900, easing=FastOutSlowInEasing), RepeatMode.Reverse), "w1")
    val w2 by inf.animateFloat(.45f, -.45f,  infiniteRepeatable(tween(5700, easing=LinearEasing),        RepeatMode.Reverse), "w2")
    val w3 by inf.animateFloat(-.22f, .22f,  infiniteRepeatable(tween(8300, easing=FastOutSlowInEasing), RepeatMode.Reverse), "w3")
    val w4 by inf.animateFloat(.12f, -.12f,  infiniteRepeatable(tween(2100, easing=LinearEasing),        RepeatMode.Reverse), "w4")
    val sway = w1 * 1.10f + w2 * 0.50f + w3 * 0.24f + w4 * 0.16f

    val twigPhase   by inf.animateFloat(0f, 2f*PI.toFloat(), infiniteRepeatable(tween(6200,  easing=LinearEasing)), "tp")
    val breath      by inf.animateFloat(.96f, 1.05f, infiniteRepeatable(tween(2800, easing=FastOutSlowInEasing), RepeatMode.Reverse), "br")
    val celestial   by inf.animateFloat(.94f, 1.06f, infiniteRepeatable(tween(3700, easing=FastOutSlowInEasing), RepeatMode.Reverse), "cel")
    val rayAlpha    by inf.animateFloat(.50f, 1.0f,  infiniteRepeatable(tween(4100), RepeatMode.Reverse), "ra")
    val rayShift    by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(14000, easing=LinearEasing)), "rs")
    val cDrift      by inf.animateFloat(0f,   1f,    infiniteRepeatable(tween(27000, easing=LinearEasing)), "cd")
    val cDrift2     by inf.animateFloat(1f,   0f,    infiniteRepeatable(tween(35000, easing=LinearEasing)), "cd2")
    val shimmer     by inf.animateFloat(0f, 2f*PI.toFloat(), infiniteRepeatable(tween(5500, easing=LinearEasing)), "sh")
    val crownPulse  by inf.animateFloat(0f, 2f*PI.toFloat(), infiniteRepeatable(tween(3500, easing=LinearEasing)), "cp")
    val auroraPhase by inf.animateFloat(0f, 2f*PI.toFloat(), infiniteRepeatable(tween(12000, easing=LinearEasing)), "ap")
    val twA by inf.animateFloat(.25f, 1f,   infiniteRepeatable(tween(870),  RepeatMode.Reverse), "tA")
    val twB by inf.animateFloat(1f,  .20f,  infiniteRepeatable(tween(1240), RepeatMode.Reverse), "tB")
    val twC by inf.animateFloat(.50f, 1f,   infiniteRepeatable(tween(1680), RepeatMode.Reverse), "tC")
    val twD by inf.animateFloat(.30f, .90f, infiniteRepeatable(tween(2100), RepeatMode.Reverse), "tD")

    val animProg by animateFloatAsState(targetValue = progress, animationSpec = tween(1000, easing=FastOutSlowInEasing), label="ap2")

    val leafCount     = if (leaves>5) 20 else if (leaves>0) 10 else 0
    val fallingLeaves = remember(leafCount) { List(leafCount) { FallingLeaf(1200f, 900f) } }
    val windLines     = remember { List(14) { WindParticle() } }
    val fireflyCount  = if (sky.hasFireflies) 22 else 0
    val fireflies     = remember(fireflyCount) { List(fireflyCount) { FireflyParticle() } }
    val birdCount     = if (sky.hasBirds) 8 else 0
    val birds         = remember(birdCount) { List(birdCount) { BirdParticle() } }
    val pollenCount   = if (sky.hasPollen && leaves > 2) 35 else 0
    val pollenList    = remember(pollenCount) { List(pollenCount) { PollenParticle() } }

    // ── treeDepth متأثر بالحوقلة (branchStrength) ─────────────────────────
    val baseDepth = when { level<=1->3; level<=3->4; level<=5->5; else->6 }
    val treeDepth = (baseDepth + (branchStrength / 5).coerceAtMost(2)).coerceAtMost(8)

    val lightXFr  = if (sky.hasSun) sky.sunPosX else if (sky.hasMoon) sky.moonPosX else 0.5f
    val lightYFr  = if (sky.hasSun) sky.sunPosY else if (sky.hasMoon) sky.moonPosY else 0.0f

    Canvas(modifier = modifier) {
        val W  = size.width; val H = size.height
        val gY = H * 0.74f
        val cx = W * 0.50f
        val lightX = lightXFr * W; val lightY = lightYFr * gY
        val ldx  = cx - lightX; val ldy = gY * 0.5f - lightY
        val ldLen = hypot(ldx, ldy).coerceAtLeast(1f)
        val lightDir = Offset(ldx / ldLen, ldy / ldLen)

        // 1. Sky
        drawSky(W, gY, sky, lightX, lightY, celestial, rayAlpha, rayShift, twA, twB, twC, twD, cDrift, cDrift2, auroraPhase)

        // 2. Birds
        if (sky.hasBirds) { birds.forEach { it.update() }; drawBirds(birds, W, gY, 1.0f) }

        // 3. Pollen
        if (pollenList.isNotEmpty()) { pollenList.forEach { it.update(sway) }; drawPollen(pollenList, W, gY) }

        // 4. Ground
        drawGround(W, gY, H, sky, shimmer, sway, sky.dewIntensity, sky.heatHaze)

        // 5. Wind streaks
        if (abs(sway) > 0.3f) {
            windLines.forEach { p ->
                p.update(sway)
                drawLine(sky.skyBottom.copy(alpha = p.alpha * abs(sway) * 0.6f), Offset(p.x * W, p.y * gY), Offset(p.x * W + p.length * sway, p.y * gY + 1f), 0.8f)
            }
        }

        // 6. Tree
        val maxTreeH = gY * 0.83f
        val rawLen   = 70f + level * 16f
        val tL       = rawLen.coerceAtMost(maxTreeH)
        val tW       = (20f + level * 2.2f).coerceAtMost(38f)

        drawTreeShadow(cx, gY, tL, tW, lightDir, sky.lightIntensity)
        drawOval(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.34f), Color.Transparent), Offset(cx, gY + 5f), 140f), Offset(cx - 140f, gY - 14f), Size(280f, 30f))
        drawRocks(cx, gY)
        drawMushrooms(cx, gY)
        drawFallenLeafPile(cx, gY, W, sky.leafTint)
        drawWildflowers(W, gY, flowers)
        drawRoots(cx, gY, tW, lightDir)

        val branches = buildBranches(cx, gY, 90f, tL, tW, 0, treeDepth, sway, twigPhase)
        branches.filter { it.depth <= 1 }.forEach { b ->
            drawLine(Color.Black.copy(alpha = 0.07f), Offset(b.x1 + 5f, b.y1 + 9f), Offset(b.x2 + 5f, b.y2 + 9f), b.w * 1.1f, cap = StrokeCap.Round)
        }
        branches.sortedByDescending { it.w }.forEach { b ->
            drawBark(b.x1, b.y1, b.x2, b.y2, b.w, lightDir, b.depth)
        }

        // 7. Foliage
        val leafBrs = branches.filter { it.depth >= treeDepth - 1 }
        leafBrs.forEachIndexed { i, b ->
            drawFoliage(
                cx = b.x2, cy = b.y2,
                radius = 16f + animProg * 18f + leaves * 0.30f,
                breath = breath, idx = i, total = leafBrs.size,
                leaves = leaves, flowers = flowers, fruits = fruits,
                nightTint = sky.leafTint, sway = sway,
                lightDir = lightDir, lightIntensity = sky.lightIntensity
            )
        }

        // 8. Light shafts
        drawCanopyLightShafts(branches, gY, lightDir, sky.lightIntensity, rayAlpha)

        // 9. Crown glow
        if (level >= 3) {
            val tX = cx + sway * 4f; val tY = gY - tL - 22f
            val cR    = (38f + level * 12f + animProg * 20f).coerceAtMost(110f)
            val pulse = cR * (1f + 0.038f * sin(crownPulse))
            drawCircle(Brush.radialGradient(listOf(Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF2E7D32).copy(alpha = 0.06f), Color.Transparent), Offset(tX, tY), pulse), pulse, Offset(tX, tY))
        }

        // 10. Falling leaves
        if (fallingLeaves.isNotEmpty()) {
            val leafColors = listOf(Color(0xFF2E7D32), Color(0xFF43A047), Color(0xFF81C784), Color(0xFF1B5E20), Color(0xFFA5D6A7), Color(0xFFAED581))
            val autumnColors = listOf(Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF8F00))
            fallingLeaves.forEach { lf ->
                lf.update(sway)
                val lx = lf.x % W; val ly = lf.y % H
                withTransform({ translate(lx, ly); rotate(lf.rot, Offset.Zero) }) {
                    val col = if (lf.hue == 3 && sky.leafTint > 0.3f) autumnColors[lf.hue % 3] else leafColors[lf.hue % leafColors.size]
                    drawOval(Color.Black.copy(alpha = lf.alpha * 0.13f), Offset(-lf.size + 2f, -lf.size * 0.42f + 2f), Size(lf.size * 2f, lf.size))
                    drawOval(col.copy(alpha = lf.alpha * (1f - sky.leafTint * 0.25f)), Offset(-lf.size, -lf.size * 0.42f), Size(lf.size * 2f, lf.size))
                    drawLine(lerp(col, Color(0xFF0D3B0D), 0.4f).copy(alpha = lf.alpha * 0.55f), Offset(-lf.size * 0.65f, 0f), Offset(lf.size * 0.65f, 0f), 0.55f)
                    drawOval(Color.White.copy(alpha = lf.alpha * 0.20f), Offset(-lf.size * 0.38f, -lf.size * 0.34f), Size(lf.size * 0.65f, lf.size * 0.26f))
                }
            }
        }

        // 11. Fireflies
        if (sky.hasFireflies && fireflies.isNotEmpty()) {
            fireflies.forEach { it.update(sway) }
            drawFireflies(fireflies, W, gY, sky.fireflyAlpha)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  UI COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun QuoteCard(quote: String) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        RoundedCornerShape(20.dp),
        CardDefaults.cardColors(GreenStart.copy(alpha = 0.07f)),
        CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(42.dp).background(GreenStart.copy(alpha = 0.15f), CircleShape), Alignment.Center) {
                Text("📖", fontSize = 20.sp)
            }
            Text(
                quote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ProgressCard(level: Int, progress: Float, pointsToNext: Int) {
    val p by animateFloatAsState(targetValue = progress, animationSpec = tween(800, easing = FastOutSlowInEasing), label = "p")
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(
                        stringResource(R.string.tree_level, level),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.tree_progress_to_next, pointsToNext),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(Modifier.size(54.dp).background(GreenStart.copy(alpha = 0.12f), CircleShape), Alignment.Center) {
                    Text(
                        "${(p * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = GreenStart,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)).background(GreenStart.copy(alpha = 0.12f))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(p).clip(RoundedCornerShape(7.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF1B5E20), GreenStart, Color(0xFF81C784)))))
            }
        }
    }
}

@Composable
fun StatsRow(totalPoints: Int, leaves: Int, flowers: Int, fruits: Int, branchStrength: Int) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            StatBox(Icons.Default.Star, totalPoints.toString(), stringResource(R.string.tree_total_points), Color(0xFFFFC107), Modifier.weight(1f))
            StatBox(Icons.Default.Spa, leaves.toString(), stringResource(R.string.tree_leaves), GreenStart, Modifier.weight(1f))
            StatBox(Icons.Default.LocalFlorist, flowers.toString(), stringResource(R.string.tree_flowers), Color(0xFFEC407A), Modifier.weight(1f))
            StatBox(Icons.Default.Forest, fruits.toString(), stringResource(R.string.tree_fruits), Color(0xFFFF7043), Modifier.weight(1f))
        }
        BranchStrengthBar(branchStrength)
    }
}

@Composable
private fun BranchStrengthBar(branchStrength: Int) {
    val maxStrength = 10
    val displayStr = branchStrength.coerceAtMost(maxStrength)
    val animFill by animateFloatAsState(targetValue = displayStr / maxStrength.toFloat(), animationSpec = tween(800, easing = FastOutSlowInEasing), label = "branchFill")

    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardDefaults.cardColors(Color(0xFF4E342E).copy(alpha = 0.07f)), CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(38.dp).background(Color(0xFF4E342E).copy(alpha = 0.12f), CircleShape), Alignment.Center) {
                Icon(Icons.Default.AccountTree, null, tint = Color(0xFF6D4C41), modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.tree_branch_strength) + " 🌿",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Text(
                        if (displayStr >= maxStrength) stringResource(R.string.tree_branch_max)
                        else stringResource(R.string.tree_branch_progress, displayStr, maxStrength),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6D4C41)
                    )
                }
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF4E342E).copy(alpha = 0.12f))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(animFill).clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF6D4C41), Color(0xFF8D6E63), Color(0xFFBCAAA4)))))
                }
                Text(
                    stringResource(R.string.tree_branch_strength_detail),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatBox(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    val anim by animateIntAsState(targetValue = value.toIntOrNull() ?: 0, animationSpec = tween(600), label = "s")
    Card(modifier, RoundedCornerShape(18.dp), CardDefaults.cardColors(color.copy(alpha = 0.08f)), CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(anim.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ZIKR COUNTERS CARD (with resources)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ZikrCountersCard(
    subhanallahCount: Int, alhamdulillahCount: Int, allahuakbarCount: Int,
    laIlahaCount: Int, hawqalaCount: Int, astaghfirCount: Int,
    salawatCount: Int, bismillahCount: Int, mashallahCount: Int
) {
    val total = subhanallahCount + alhamdulillahCount + allahuakbarCount +
            laIlahaCount + hawqalaCount + astaghfirCount +
            salawatCount + bismillahCount + mashallahCount

    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(20.dp), CardDefaults.cardColors(GreenStart.copy(alpha = 0.04f)), CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.tree_total_dhikr),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(shape = RoundedCornerShape(10.dp), color = GreenStart.copy(alpha = 0.12f)) {
                    Text(
                        stringResource(R.string.tree_times_format, total),
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = GreenStart,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 🍃 Leaves group
            ZikrGroupCard(
                groupEmoji = "🍃",
                groupLabel = stringResource(R.string.tree_group_leaves),
                groupColor = GreenStart,
                items = listOf(
                    ZikrStatItem(R.string.dhikr_short_subhanallah, subhanallahCount, Color(0xFF2E7D32), R.string.tree_hint_subhanallah),
                    ZikrStatItem(R.string.dhikr_short_tahleel, laIlahaCount, Color(0xFF1B5E20), R.string.tree_hint_lailaha),
                    ZikrStatItem(R.string.dhikr_short_istighfar, astaghfirCount, Color(0xFF388E3C), R.string.tree_hint_astaghfirullah)
                )
            )

            // 🌸 Flowers group
            ZikrGroupCard(
                groupEmoji = "🌸",
                groupLabel = stringResource(R.string.tree_group_flowers),
                groupColor = Color(0xFFEC407A),
                items = listOf(
                    ZikrStatItem(R.string.dhikr_short_hamd, alhamdulillahCount, Color(0xFF1565C0), R.string.tree_hint_alhamdulillah),
                    ZikrStatItem(R.string.dhikr_short_bismillah, bismillahCount, Color(0xFF2E7D32), R.string.tree_hint_bismillah),
                    ZikrStatItem(R.string.dhikr_short_mashallah, mashallahCount, Color(0xFF00695C), R.string.tree_hint_mashallah)
                )
            )

            // 🍎 Fruits group
            ZikrGroupCard(
                groupEmoji = "🍎",
                groupLabel = stringResource(R.string.tree_group_fruits),
                groupColor = Color(0xFFFF7043),
                items = listOf(
                    ZikrStatItem(R.string.dhikr_short_akbar, allahuakbarCount, Color(0xFF6A1B9A), R.string.tree_hint_allahuakbar),
                    ZikrStatItem(R.string.dhikr_short_salah, salawatCount, Color(0xFFE65100), R.string.tree_hint_salawat)
                )
            )

            // 🌿 Branches group
            ZikrGroupCard(
                groupEmoji = "🌿",
                groupLabel = stringResource(R.string.tree_group_branches),
                groupColor = Color(0xFF4E342E),
                items = listOf(
                    ZikrStatItem(R.string.dhikr_short_hawqala, hawqalaCount, Color(0xFF4E342E), R.string.tree_hint_hawqala)
                )
            )
        }
    }
}

private data class ZikrStatItem(val labelRes: Int, val count: Int, val color: Color, val hintRes: Int)

@Composable
private fun ZikrGroupCard(groupEmoji: String, groupLabel: String, groupColor: Color, items: List<ZikrStatItem>) {
    Surface(shape = RoundedCornerShape(14.dp), color = groupColor.copy(alpha = 0.06f)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(groupEmoji, fontSize = 15.sp)
                Text(groupLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = groupColor)
            }
            items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = groupColor.copy(alpha = 0.10f), thickness = 0.5.dp)
                ZikrStatRow(item)
            }
        }
    }
}

@Composable
private fun ZikrStatRow(item: ZikrStatItem) {
    val countAnim by animateIntAsState(targetValue = item.count, animationSpec = tween(500), label = "cnt_${item.labelRes}")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(8.dp).background(item.color, CircleShape))
        Text(
            stringResource(item.labelRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            stringResource(item.hintRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(shape = RoundedCornerShape(8.dp), color = item.color.copy(alpha = 0.12f)) {
            Text(
                stringResource(R.string.tree_times_x, countAnim),
                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = item.color
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ACTIVITIES
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ActivitiesList(activities: List<TreeActivity>) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            stringResource(R.string.tree_activities),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        activities.take(10).forEach { ActivityItem(it) }
    }
}

@Composable
fun ActivityItem(activity: TreeActivity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(activity.color.copy(alpha = 0.15f), CircleShape), Alignment.Center) {
                Icon(activity.icon, null, tint = activity.color, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(activity.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(activity.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = GreenStart.copy(alpha = 0.10f)) {
                Text(
                    stringResource(R.string.plus_d, activity.points),
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = GreenStart,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  DIALOGS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LevelUpDialog(level: Int, sky: SkyTheme, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("🌳", style = MaterialTheme.typography.displayLarge) },
        title = {
            Text(
                stringResource(R.string.tree_level_up, sky.icon),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = GreenStart
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.tree_level_up_detail, level),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = GreenStart.copy(alpha = 0.08f)) {
                    val (e, lRes) = getStageParts(level)
                    Text(
                        "$e ${stringResource(lRes)}",
                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = GreenStart,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.tree_level_up_continue),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(GreenStart),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.tree_level_up_button), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun ResetConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", style = MaterialTheme.typography.displaySmall) },
        title = { Text(stringResource(R.string.tree_reset_title), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.tree_reset_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(Color(0xFFB71C1C))) {
                Text(stringResource(R.string.tree_reset_confirm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  HELPERS (return resource IDs now)
// ══════════════════════════════════════════════════════════════════════════════

private fun getStageParts(level: Int): Pair<String, Int> = when {
    level <= 1 -> "🌱" to R.string.tree_stage_seed
    level <= 2 -> "🌿" to R.string.tree_stage_sapling
    level <= 3 -> "🌳" to R.string.tree_stage_tree
    level <= 4 -> "🌸" to R.string.tree_stage_blooming
    level <= 5 -> "🍎" to R.string.tree_stage_fruiting
    else -> "✨" to R.string.tree_stage_towering
}

private fun getStageBadgeColor(level: Int) = when {
    level <= 1 -> Color(0xFF6D4C41); level <= 2 -> Color(0xFF388E3C)
    level <= 3 -> Color(0xFF1B5E20); level <= 4 -> Color(0xFFC2185B)
    level <= 5 -> Color(0xFFE64A19); else -> Color(0xFFF57F17)
}

private fun darken(c: Color, f: Float) = Color(
    (c.red * (1f - f)).coerceIn(0f, 1f),
    (c.green * (1f - f)).coerceIn(0f, 1f),
    (c.blue * (1f - f)).coerceIn(0f, 1f),
    c.alpha
)

private fun getPrayerTimeHintRes(tod: TimeOfDay): Int = when (tod) {
    TimeOfDay.FAJR -> R.string.prayer_hint_fajr
    TimeOfDay.MORNING -> R.string.prayer_hint_morning
    TimeOfDay.AFTERNOON -> R.string.prayer_hint_afternoon
    TimeOfDay.MAGHRIB -> R.string.prayer_hint_maghrib
    TimeOfDay.NIGHT -> R.string.prayer_hint_night
}

private fun hypot(dx: Float, dy: Float) = sqrt(dx * dx + dy * dy)

// ══════════════════════════════════════════════════════════════════════════════
//  DATA
// ══════════════════════════════════════════════════════════════════════════════

data class TreeActivity(
    val title: String, val time: String, val points: Int,
    val icon: ImageVector, val color: Color
)

private data class FP(val id: Long, val text: String, val color: Color)

@Composable
private fun FloatLabel(fp: FP) {
    val oy by animateFloatAsState(targetValue = -90f, animationSpec = tween(1400, easing = FastOutSlowInEasing), label = "y${fp.id}")
    val al by animateFloatAsState(targetValue = 0f, animationSpec = tween(1400, easing = FastOutSlowInEasing), label = "a${fp.id}")
    Box(Modifier.fillMaxWidth().offset(y = oy.dp), Alignment.Center) {
        Text(fp.text, color = fp.color.copy(alpha = (1f - al).coerceIn(0f, 1f)), fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}