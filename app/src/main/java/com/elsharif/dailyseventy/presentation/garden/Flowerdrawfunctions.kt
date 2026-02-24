package com.elsharif.dailyseventy.presentation.garden



import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random
// ══════════════════════════════════════════════════════════════════════════════
//  LOTUS  — Nelumbo nucifera, sacred water lily
//  Passes: water reflection glow → 8 outer petals (cupped) → 8 inner petals
//  → golden receptacle disc → stamens ring → stigma head
// ══════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawRealisticLotus(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p  = r * (1f + sin(phase) * 0.045f)
    val lf = litFactor(-0.25f, -0.92f, amb)

    // 1. Sacred glow — lotus blooms at dawn, strong early-morning aura
    val dawnGlow = ((0.75f - abs(amb - 0.45f)) / 0.75f).coerceIn(0f, 1f)
    drawCircle(Color(0xFFE070A0).copy(alpha = 0.04f + dawnGlow * 0.05f), p * 3.4f, Offset(cx, cy))
    drawCircle(Color(0xFFFF90C0).copy(alpha = 0.07f + dawnGlow * 0.07f), p * 2.2f, Offset(cx, cy))
    drawCircle(Color(0xFFFFB8D8).copy(alpha = 0.10f + dawnGlow * 0.08f), p * 1.5f, Offset(cx, cy))

    // 2. Water reflection shimmer below
    drawOval(Color(0xFFB060A0).copy(alpha = 0.08f * amb),
        Offset(cx - p * 1.1f, cy + p * 0.55f), Size(p * 2.2f, p * 0.35f))

    // 3. Cast shadow
    drawCircle(Color.Black.copy(alpha = 0.16f), p * 1.08f, Offset(cx + p * 0.04f, cy + p * 0.52f))

    // 4. Outer petals — 8, wide and cupped upward (concave)
    repeat(8) { i ->
        val ang  = Math.toRadians(i * 45.0 + phase * 2.2)
        val pf   = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb)
        val hw   = p * 0.32f
        val bx   = cx + (cos(ang) * p * 0.22f).toFloat()
        val by   = cy + (sin(ang) * p * 0.22f).toFloat()
        val px   = cx + (cos(ang) * p * 1.05f).toFloat()
        val py   = cy + (sin(ang) * p * 1.05f).toFloat()
        val perp = ang + PI / 2.0
        // Cup shape — control points pulled inward to simulate concave petal
        val pp = Path().apply {
            moveTo(bx + (cos(perp) * hw * 0.55f).toFloat(), by + (sin(perp) * hw * 0.55f).toFloat())
            cubicTo(
                bx + (cos(perp) * hw * 1.15f).toFloat() + (cos(ang) * p * 0.42f).toFloat(),
                by + (sin(perp) * hw * 1.15f).toFloat() + (sin(ang) * p * 0.42f).toFloat(),
                px + (cos(perp) * hw * 0.62f).toFloat() + (cos(ang + PI) * p * 0.08f).toFloat(),
                py + (sin(perp) * hw * 0.62f).toFloat() + (sin(ang + PI) * p * 0.08f).toFloat(),
                px, py
            )
            cubicTo(
                px + (cos(perp) * (-hw * 0.62f)).toFloat() + (cos(ang + PI) * p * 0.08f).toFloat(),
                py + (sin(perp) * (-hw * 0.62f)).toFloat() + (sin(ang + PI) * p * 0.08f).toFloat(),
                bx + (cos(perp) * (-hw * 1.15f)).toFloat() + (cos(ang) * p * 0.42f).toFloat(),
                by + (sin(perp) * (-hw * 1.15f)).toFloat() + (sin(ang) * p * 0.42f).toFloat(),
                bx + (cos(perp) * (-hw * 0.55f)).toFloat(), by + (sin(perp) * (-hw * 0.55f)).toFloat()
            )
            close()
        }
        withTransform({ translate(1.3f, 2.0f) }) { drawPath(pp, Color.Black.copy(alpha = 0.14f)) }
        val darkC = lerp(Color(0xFF8A2060), Color(0xFFB03080), pf * 0.60f)
        val midC  = lerp(Color(0xFFCC4898), Color(0xFFE070B8), pf * 0.70f)
        val tipC  = lerp(Color(0xFFE898CC), Color(0xFFFFC8E0), pf * 0.75f + lf * 0.15f)
        drawPath(pp, Brush.linearGradient(listOf(darkC, midC, tipC), Offset(cx, cy), Offset(px, py)))
        // SSS — pink light scatter
        drawPath(pp, Color(0xFFFF80C0).copy(alpha = pf * 0.09f * (0.4f + amb * 0.6f)))
        // Petal veins — 3 radiating lines
        val midX = lerp(bx, px, 0.55f); val midY = lerp(by, py, 0.55f)
        drawLine(darkC.copy(alpha = 0.35f), Offset(bx, by), Offset(px, py), 0.45f)
        listOf(-0.22f, 0.22f).forEach { of ->
            val vx = cx + (cos(ang + of) * p * 0.85f).toFloat()
            val vy = cy + (sin(ang + of) * p * 0.85f).toFloat()
            drawLine(darkC.copy(alpha = 0.18f), Offset(midX, midY), Offset(vx, vy), 0.30f)
        }
        // Tip notch
        drawCircle(tipC.copy(alpha = 0.45f), 1.2f, Offset(px, py))
    }

    // 5. Inner petals — 8, smaller, more erect (nearly vertical)
    repeat(8) { i ->
        val ang  = Math.toRadians(i * 45.0 + 22.5 + phase * 1.8)
        val pf   = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb)
        val hw   = p * 0.22f
        val bx   = cx + (cos(ang) * p * 0.18f).toFloat()
        val by   = cy + (sin(ang) * p * 0.18f).toFloat()
        val px   = cx + (cos(ang) * p * 0.68f).toFloat()
        val py   = cy + (sin(ang) * p * 0.68f).toFloat()
        val perp = ang + PI / 2.0
        val pp = Path().apply {
            moveTo(bx + (cos(perp) * hw * 0.45f).toFloat(), by + (sin(perp) * hw * 0.45f).toFloat())
            cubicTo(
                bx + (cos(perp) * hw).toFloat() + (cos(ang) * p * 0.36f).toFloat(),
                by + (sin(perp) * hw).toFloat() + (sin(ang) * p * 0.36f).toFloat(),
                px + (cos(perp) * hw * 0.55f).toFloat(), py + (sin(perp) * hw * 0.55f).toFloat(),
                px, py
            )
            cubicTo(
                px + (cos(perp) * (-hw * 0.55f)).toFloat(), py + (sin(perp) * (-hw * 0.55f)).toFloat(),
                bx + (cos(perp) * (-hw)).toFloat() + (cos(ang) * p * 0.36f).toFloat(),
                by + (sin(perp) * (-hw)).toFloat() + (sin(ang) * p * 0.36f).toFloat(),
                bx + (cos(perp) * (-hw * 0.45f)).toFloat(), by + (sin(perp) * (-hw * 0.45f)).toFloat()
            )
            close()
        }
        val darkC = lerp(Color(0xFFAA3070), Color(0xFFCC5090), pf * 0.55f)
        val tipC  = lerp(Color(0xFFEEA8D0), Color(0xFFFFD0E8), pf * 0.70f + lf * 0.18f)
        drawPath(pp, Brush.radialGradient(listOf(tipC, darkC), Offset(px, py), p * 0.55f))
        drawPath(pp, Color(0xFFFF90C8).copy(alpha = pf * 0.10f))
    }

    // 6. Golden receptacle disc (seedpod)
    val discR = p * 0.28f
    drawCircle(Color(0xFF4A3000).copy(alpha = 0.88f), discR, Offset(cx, cy))
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFFD4A020), Color(0xFFA87010), Color(0xFF6A4A00)),
            Offset(cx - discR * 0.15f, cy - discR * 0.15f), discR
        ), discR, Offset(cx, cy)
    )
    // Seed holes in golden disc — Fibonacci pattern
    repeat(12) { k ->
        val theta = Math.toRadians(k * 137.5)
        val rho   = discR * 0.72f * sqrt(k.toFloat() / 12f)
        val sx    = cx + (cos(theta) * rho).toFloat()
        val sy    = cy + (sin(theta) * rho).toFloat()
        drawCircle(Color(0xFF2A1A00).copy(alpha = 0.75f), discR * 0.09f, Offset(sx, sy))
        drawCircle(Color(0xFF1A0E00).copy(alpha = 0.55f), discR * 0.05f, Offset(sx, sy))
    }

    // 7. Stamens — ring of yellow filaments
    repeat(16) { i ->
        val ang = Math.toRadians(i * 22.5 + phase * 12.0)
        val sr  = p * 0.36f
        val fx  = cx + (cos(ang) * sr).toFloat()
        val fy  = cy + (sin(ang) * sr).toFloat()
        drawLine(Color(0xFFD4B820).copy(alpha = 0.65f), Offset(cx + (cos(ang) * discR).toFloat(), cy + (sin(ang) * discR).toFloat()),
            Offset(fx, fy), 0.6f)
        drawCircle(Color(0xFFEED030).copy(alpha = 0.80f), 1.3f, Offset(fx, fy))
    }

    // 8. Dew on outer petal tips
    listOf(0.0 to 1.02, 2.09 to 1.02, 4.19 to 1.02).forEach { (angOff, rMul) ->
        val ang = angOff + phase * 0.15
        val dx  = cx + (cos(ang) * p * rMul).toFloat()
        val dy  = cy + (sin(ang) * p * rMul).toFloat()
        drawCircle(Color(0xFF90C0B0).copy(alpha = 0.28f), 2.4f, Offset(dx + 1f, dy + 1f))
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.60f), Color(0xFFFFD0E8).copy(alpha = 0.22f), Color.Transparent),
                Offset(dx - 0.8f, dy - 0.8f), 3.0f
            ), 2.4f, Offset(dx, dy)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  LAVENDER  — Lavandula angustifolia, fragrant Mediterranean spike
//  Passes: purple haze → long spike stem → 6 whorls of tiny florets
//  → calyx bracts → pollen shimmer
// ══════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawRealisticLavender(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p  = r * (1f + sin(phase) * 0.028f)
    val lf = litFactor(-0.30f, -0.92f, amb)

    // 1. Fragrance haze — lavender is intensely aromatic
    val hazeA = 0.04f + amb * 0.03f
    drawCircle(Color(0xFF9070D0).copy(alpha = hazeA), p * 3.6f, Offset(cx, cy))
    drawCircle(Color(0xFFB090E8).copy(alpha = hazeA * 1.6f), p * 2.4f, Offset(cx, cy))
    drawCircle(Color(0xFFD0B8F8).copy(alpha = hazeA * 2.2f), p * 1.6f, Offset(cx, cy))

    // 2. Shadow
    drawCircle(Color.Black.copy(alpha = 0.14f), p * 0.95f, Offset(cx + p * 0.04f, cy + p * 0.48f))

    // 3. Main spike — elongated vertical form
    val spikeH = p * 2.0f
    val spikeW = p * 0.22f
    val spikeTop = cy - spikeH
    val sway    = sin(phase * 0.7f) * p * 0.04f

    // Spike stem
    val stemPath = Path().apply {
        moveTo(cx, cy)
        cubicTo(cx + sway * 0.3f, cy - spikeH * 0.4f,
            cx + sway * 0.7f, cy - spikeH * 0.7f,
            cx + sway, spikeTop)
    }
    val stemDark = lerp(Color(0xFF2A3050), Color.Black, 0.45f)
    val stemLit  = lerp(Color(0xFF4A5080), Color.White, lf * 0.10f + amb * 0.05f)
    drawPath(stemPath, Color.Black.copy(alpha = 0.16f), style = Stroke(spikeW * 1.35f, cap = StrokeCap.Round))
    drawPath(stemPath, stemDark, style = Stroke(spikeW, cap = StrokeCap.Round))
    drawPath(stemPath, stemLit.copy(alpha = 0.55f), style = Stroke(spikeW * 0.38f, cap = StrokeCap.Round))

    // 4. Six whorls of tiny florets along the spike
    val whorlCount = 6
    repeat(whorlCount) { w ->
        val t2   = 0.18f + w * 0.14f           // 0 = base, 1 = tip
        val wx   = lerp(cx, cx + sway, t2)
        val wy   = cy - spikeH * t2
        val wAge = (whorlCount - w).toFloat() / whorlCount  // lower whorls older/more open
        val floretR = p * (0.08f + wAge * 0.04f)
        val floretCount = if (w < 2) 6 else if (w < 4) 5 else 4

        // Bract (green leaf-like base for each whorl)
        val bracW = spikeW * (1.6f + wAge * 0.8f)
        drawOval(
            lerp(Color(0xFF2A3050), Color(0xFF404870), lf * 0.4f + amb * 0.15f).copy(alpha = 0.75f),
            Offset(wx - bracW, wy - bracW * 0.35f), Size(bracW * 2f, bracW * 0.70f)
        )

        // Tiny florets in a ring
        repeat(floretCount) { fi ->
            val fAng = Math.toRadians(fi * (360.0 / floretCount) + w * 30.0 + phase * 8.0)
            val fr   = spikeW * (1.0f + wAge * 0.5f)
            val ffx  = wx + (cos(fAng) * fr).toFloat()
            val ffy  = wy + (sin(fAng) * fr).toFloat() * 0.55f  // flatten vertically
            val fpf  = litFactor(cos(fAng).toFloat(), sin(fAng).toFloat(), amb)

            // Each floret — tiny 2-lipped shape (labiate)
            val fDark = lerp(Color(0xFF5A3090), Color(0xFF7848B8), fpf * 0.65f)
            val fTip  = lerp(Color(0xFF9868D8), Color(0xFFBE98F0), fpf * 0.75f + lf * 0.12f)
            drawCircle(Color.Black.copy(alpha = 0.18f), floretR * 1.1f, Offset(ffx + 0.6f, ffy + 0.8f))
            drawCircle(
                Brush.radialGradient(listOf(fTip, fDark, Color(0xFF3A1870)), Offset(ffx, ffy), floretR),
                floretR, Offset(ffx, ffy)
            )
            // Upper lip highlight
            drawCircle(Color.White.copy(alpha = fpf * 0.22f * amb), floretR * 0.30f,
                Offset(ffx - floretR * 0.18f, ffy - floretR * 0.22f))
        }
    }

    // 5. Tip bud — tight unopened cluster
    val tipR = p * 0.09f
    drawCircle(Color(0xFF3A1870).copy(alpha = 0.85f), tipR, Offset(cx + sway, spikeTop))
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFF7850B8), Color(0xFF4A2890), Color(0xFF2A1060)),
            Offset(cx + sway - tipR * 0.1f, spikeTop - tipR * 0.1f), tipR
        ), tipR, Offset(cx + sway, spikeTop)
    )

    // 6. Pollen shimmer (tiny dots around lower whorls)
    if (amb > 0.25f) {
        repeat(10) { i ->
            val ang = Math.toRadians(i * 36.0 + phase * 25.0)
            val pr2 = p * (0.20f + hash(i.toFloat(), 55f) * 0.18f)
            val py2 = cy - spikeH * (0.18f + hash(i.toFloat(), 56f) * 0.35f)
            drawCircle(Color(0xFFD0B0F0).copy(alpha = amb * 0.22f), 0.9f,
                Offset(cx + (cos(ang) * pr2).toFloat(), py2))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MINT  — Mentha, aromatic herb with tiny clustered flower whorls
//  Passes: fresh green glow → branching stems → leaf pairs → tiny white
//  floret clusters → essential oil shimmer dots
// ══════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawRealisticMint(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p  = r * (1f + sin(phase) * 0.022f)
    val lf = litFactor(0.20f, -0.95f, amb)

    // 1. Fresh herbal aura
    drawCircle(Color(0xFF30A860).copy(alpha = 0.04f * amb), p * 3.0f, Offset(cx, cy))
    drawCircle(Color(0xFF50C878).copy(alpha = 0.07f * amb), p * 1.9f, Offset(cx, cy))

    // 2. Shadow
    drawCircle(Color.Black.copy(alpha = 0.13f), p * 0.88f, Offset(cx + p * 0.03f, cy + p * 0.44f))

    // 3. Three branching stems
    val stems = listOf(
        Triple(cx - p * 0.25f, cy - p * 1.70f, -0.18f),   // left branch
        Triple(cx,             cy - p * 2.00f,  0.00f),   // central
        Triple(cx + p * 0.22f, cy - p * 1.65f,  0.16f)    // right branch
    )
    stems.forEach { (ex, ey, lean) ->
        val stemPath = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx + lean * p * 0.5f, cy - p * 0.7f,
                ex + lean * p * 0.3f, ey + p * 0.4f, ex, ey)
        }
        val stemDark = lerp(Color(0xFF1A4828), Color.Black, 0.48f)
        val stemLit  = lerp(Color(0xFF2A6838), Color.White, lf * 0.12f + amb * 0.04f)
        drawPath(stemPath, Color.Black.copy(alpha = 0.14f), style = Stroke(2.8f, cap = StrokeCap.Round))
        drawPath(stemPath, stemDark, style = Stroke(1.9f, cap = StrokeCap.Round))
        drawPath(stemPath, stemLit.copy(alpha = 0.50f), style = Stroke(0.75f, cap = StrokeCap.Round))
    }

    // 4. Leaf pairs along each stem — ovate with serrated edge hint
    val leafPositions = listOf(
        cx - p*0.08f to cy - p*0.55f,
        cx + p*0.05f to cy - p*1.10f,
        cx - p*0.18f to cy - p*1.38f,
        cx + p*0.18f to cy - p*1.62f
    )
    leafPositions.forEachIndexed { idx, (lx, ly) ->
        val age   = idx.toFloat() / leafPositions.size
        val lw    = p * (0.52f - age * 0.15f)
        val lh    = p * (0.30f - age * 0.08f)
        val leafF = litFactor(-0.5f, -0.85f, amb)
        val lDark = lerp(Color(0xFF1A4828), Color.Black, 0.50f)
        val lMid  = lerp(Color(0xFF2A6838), Color(0xFF3A8848), leafF * 0.55f + amb * 0.12f)
        val lLit  = lerp(Color(0xFF40C870), Color.White, leafF * 0.16f + amb * 0.06f)

        // Left leaf
        withTransform({ rotate(-38f - age * 8f, Offset(lx, ly)) }) {
            drawOval(Color.Black.copy(alpha = 0.15f), Offset(lx - lw * 0.5f + 1.2f, ly - lh * 0.44f + 1.5f), Size(lw, lh))
            drawOval(Brush.linearGradient(listOf(lLit.copy(alpha = 0.85f), lMid, lDark),
                Offset(lx - lw * 0.5f, ly - lh * 0.5f), Offset(lx + lw * 0.5f, ly + lh * 0.5f)),
                Offset(lx - lw * 0.5f, ly - lh * 0.44f), Size(lw, lh))
            // Midrib
            drawLine(lDark.copy(alpha = 0.42f), Offset(lx - lw * 0.44f, ly), Offset(lx + lw * 0.44f, ly), 0.5f)
            // Serration hint
            repeat(4) { s ->
                val sx2 = lx - lw * 0.38f + s * lw * 0.25f
                drawLine(lDark.copy(alpha = 0.20f), Offset(sx2, ly - lh * 0.40f), Offset(sx2 + 1.5f, ly - lh * 0.52f), 0.4f)
            }
            if (lw > 12f && amb > 0.3f) drawCircle(Color.White.copy(alpha = amb * 0.22f), 1.0f, Offset(lx + lw * 0.28f, ly - lh * 0.14f))
        }
        // Right leaf (mirrored)
        withTransform({ rotate(40f + age * 8f, Offset(lx, ly)) }) {
            drawOval(Color.Black.copy(alpha = 0.13f), Offset(lx - lw * 0.5f + 1.2f, ly - lh * 0.44f + 1.5f), Size(lw, lh))
            drawOval(Brush.linearGradient(listOf(lLit.copy(alpha = 0.82f), lMid, lDark),
                Offset(lx - lw * 0.5f, ly - lh * 0.5f), Offset(lx + lw * 0.5f, ly + lh * 0.5f)),
                Offset(lx - lw * 0.5f, ly - lh * 0.44f), Size(lw, lh))
            drawLine(lDark.copy(alpha = 0.40f), Offset(lx - lw * 0.44f, ly), Offset(lx + lw * 0.44f, ly), 0.5f)
        }
    }

    // 5. Tiny floret clusters at each stem tip — mint flowers are small rounded whorls
    stems.forEachIndexed { si, (ex, ey, _) ->
        val clusterR = p * (0.16f + si * 0.02f)
        val floretN  = 8

        // Cluster base
        drawCircle(lerp(Color(0xFF1A4828), Color(0xFF2A6838), lf * 0.4f).copy(alpha = 0.72f),
            clusterR * 0.85f, Offset(ex, ey))

        repeat(floretN) { fi ->
            val fAng = Math.toRadians(fi * (360.0 / floretN) + si * 22.5 + phase * 10.0)
            val ffx  = ex + (cos(fAng) * clusterR * 0.72f).toFloat()
            val ffy  = ey + (sin(fAng) * clusterR * 0.72f).toFloat() * 0.80f
            val fpf  = litFactor(cos(fAng).toFloat(), sin(fAng).toFloat(), amb)
            val fr   = clusterR * 0.28f

            // Tiny 4-petalled floret
            repeat(4) { pi ->
                val pAng = Math.toRadians(pi * 90.0 + fi * 11.0 + phase * 5.0)
                val ppx  = ffx + (cos(pAng) * fr).toFloat()
                val ppy  = ffy + (sin(pAng) * fr).toFloat()
                val pCol = lerp(
                    lerp(Color(0xFFD8F4E8), Color(0xFFFFFFFF), fpf * 0.55f),
                    Color.White, lf * 0.18f
                )
                drawCircle(pCol.copy(alpha = 0.82f + fpf * 0.12f), fr * 0.55f, Offset(ppx, ppy))
            }
            // Tiny stamen
            drawCircle(Color(0xFFD4E840).copy(alpha = 0.80f), fr * 0.22f, Offset(ffx, ffy))
        }
    }

    // 6. Essential oil shimmer — tiny reflective glands (characteristic of mint)
    if (amb > 0.20f) {
        repeat(14) { i ->
            val ang  = Math.toRadians(i * 25.7 + phase * 18.0)
            val dist = p * (0.22f + hash(i.toFloat(), 60f) * 0.55f)
            val oy   = cy - p * (0.30f + hash(i.toFloat(), 61f) * 1.40f)
            drawCircle(Color(0xFF90F0B0).copy(alpha = amb * 0.18f), 0.85f,
                Offset(cx + (cos(ang) * dist).toFloat(), oy))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  TULIP  — Tulipa, elegant cup-shaped bloom
//  Passes: warm colour glow → 3 outer tepals → 3 inner tepals (alternating)
//  → 6 stamens + pistil → fine surface striping
// ══════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawRealisticTulip(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p   = r * (1f + sin(phase) * 0.048f)
    val lf  = litFactor(-0.20f, -0.96f, amb)
    val openF = (0.70f + amb * 0.18f).coerceIn(0.70f, 0.92f)  // more open in daylight

    // 1. Colour warmth glow
    drawCircle(Color(0xFFC02020).copy(alpha = 0.05f + amb * 0.03f), p * 2.8f, Offset(cx, cy))
    drawCircle(Color(0xFFE03030).copy(alpha = 0.08f + amb * 0.04f), p * 1.8f, Offset(cx, cy))

    // 2. Cast shadow — elliptical, large cup projects strong shadow
    drawCircle(Color.Black.copy(alpha = 0.20f), p * 1.14f, Offset(cx + p * 0.05f, cy + p * 0.58f))

    // 3. Outer tepals — 3, broader with pointed tip, reflexing outward
    repeat(3) { i ->
        val ang  = Math.toRadians(i * 120.0 + phase * 1.8)
        val pf   = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb)
        val hw   = p * openF * 0.52f
        val bx   = cx + (cos(ang) * p * 0.14f).toFloat()
        val by   = cy + (sin(ang) * p * 0.14f).toFloat()
        val px   = cx + (cos(ang) * p * openF).toFloat()
        val py   = cy + (sin(ang) * p * openF).toFloat()
        val perp = ang + PI / 2.0
        // Tulip tepal — characteristic pointed shape with slight inward curl
        val pp = Path().apply {
            moveTo(bx + (cos(perp) * hw * 0.40f).toFloat(), by + (sin(perp) * hw * 0.40f).toFloat())
            cubicTo(
                bx + (cos(perp) * hw * 1.10f).toFloat() + (cos(ang) * p * 0.38f).toFloat(),
                by + (sin(perp) * hw * 1.10f).toFloat() + (sin(ang) * p * 0.38f).toFloat(),
                px + (cos(perp) * hw * 0.55f).toFloat() + (cos(ang + PI) * p * 0.06f).toFloat(),
                py + (sin(perp) * hw * 0.55f).toFloat() + (sin(ang + PI) * p * 0.06f).toFloat(),
                px, py  // pointed tip
            )
            cubicTo(
                px + (cos(perp) * (-hw * 0.55f)).toFloat() + (cos(ang + PI) * p * 0.06f).toFloat(),
                py + (sin(perp) * (-hw * 0.55f)).toFloat() + (sin(ang + PI) * p * 0.06f).toFloat(),
                bx + (cos(perp) * (-hw * 1.10f)).toFloat() + (cos(ang) * p * 0.38f).toFloat(),
                by + (sin(perp) * (-hw * 1.10f)).toFloat() + (sin(ang) * p * 0.38f).toFloat(),
                bx + (cos(perp) * (-hw * 0.40f)).toFloat(), by + (sin(perp) * (-hw * 0.40f)).toFloat()
            )
            close()
        }
        withTransform({ translate(1.5f, 2.2f) }) { drawPath(pp, Color.Black.copy(alpha = 0.16f)) }

        val darkC = lerp(Color(0xFF6A0808), Color(0xFF8C1010), pf * 0.55f)
        val midC  = lerp(Color(0xFFB01820), Color(0xFFD02830), pf * 0.65f)
        val rimC  = lerp(Color(0xFFD83838), Color(0xFFEE5048), pf * 0.75f + lf * 0.12f)
        drawPath(pp, Brush.linearGradient(listOf(darkC, midC, rimC), Offset(cx, cy), Offset(px, py)))

        // Velvet sheen — tulip petals have characteristic waxy/velvet surface
        drawPath(pp, Color(0xFFEE4040).copy(alpha = pf * 0.08f * (0.5f + amb * 0.5f)))

        // Surface stripes — tulip veins run parallel from base to tip
        repeat(4) { si ->
            val t2  = -0.35f + si * 0.22f
            val vx1 = cx + (cos(ang + t2 * 0.5f) * p * 0.25f).toFloat()
            val vy1 = cy + (sin(ang + t2 * 0.5f) * p * 0.25f).toFloat()
            val vx2 = cx + (cos(ang + t2 * 0.3f) * p * 0.88f).toFloat()
            val vy2 = cy + (sin(ang + t2 * 0.3f) * p * 0.88f).toFloat()
            drawLine(darkC.copy(alpha = 0.18f), Offset(vx1, vy1), Offset(vx2, vy2), 0.40f)
        }

        // Black base blotch — many red tulips have distinctive dark centre mark
        drawCircle(Color(0xFF180004).copy(alpha = 0.55f), p * 0.12f,
            Offset(cx + (cos(ang) * p * 0.22f).toFloat(), cy + (sin(ang) * p * 0.22f).toFloat()))
    }

    // 4. Inner tepals — 3, alternating, slightly shorter and more cupped
    repeat(3) { i ->
        val ang  = Math.toRadians(i * 120.0 + 60.0 + phase * 1.4)
        val pf   = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb)
        val hw   = p * openF * 0.44f
        val bx   = cx + (cos(ang) * p * 0.12f).toFloat()
        val by   = cy + (sin(ang) * p * 0.12f).toFloat()
        val px   = cx + (cos(ang) * p * openF * 0.85f).toFloat()
        val py   = cy + (sin(ang) * p * openF * 0.85f).toFloat()
        val perp = ang + PI / 2.0
        val pp = Path().apply {
            moveTo(bx + (cos(perp) * hw * 0.38f).toFloat(), by + (sin(perp) * hw * 0.38f).toFloat())
            cubicTo(
                bx + (cos(perp) * hw).toFloat() + (cos(ang) * p * 0.34f).toFloat(),
                by + (sin(perp) * hw).toFloat() + (sin(ang) * p * 0.34f).toFloat(),
                px + (cos(perp) * hw * 0.48f).toFloat(), py + (sin(perp) * hw * 0.48f).toFloat(),
                px, py
            )
            cubicTo(
                px + (cos(perp) * (-hw * 0.48f)).toFloat(), py + (sin(perp) * (-hw * 0.48f)).toFloat(),
                bx + (cos(perp) * (-hw)).toFloat() + (cos(ang) * p * 0.34f).toFloat(),
                by + (sin(perp) * (-hw)).toFloat() + (sin(ang) * p * 0.34f).toFloat(),
                bx + (cos(perp) * (-hw * 0.38f)).toFloat(), by + (sin(perp) * (-hw * 0.38f)).toFloat()
            )
            close()
        }
        withTransform({ translate(1.2f, 1.8f) }) { drawPath(pp, Color.Black.copy(alpha = 0.14f)) }
        val darkC = lerp(Color(0xFF8A1010), Color(0xFFAA1E1E), pf * 0.60f)
        val tipC  = lerp(Color(0xFFCC3030), Color(0xFFE85050), pf * 0.70f + lf * 0.14f)
        drawPath(pp, Brush.radialGradient(listOf(tipC, darkC, Color(0xFF500808)), Offset(px, py), p * 0.85f))
        drawPath(pp, Color(0xFFEE3030).copy(alpha = pf * 0.07f))
        drawCircle(Color(0xFF1A0004).copy(alpha = 0.48f), p * 0.10f,
            Offset(cx + (cos(ang) * p * 0.20f).toFloat(), cy + (sin(ang) * p * 0.20f).toFloat()))
    }

    // 5. Stamens — 6 with thick yellow anthers (characteristic tulip feature)
    repeat(6) { i ->
        val ang = Math.toRadians(i * 60.0 + phase * 6.0)
        val sl  = p * 0.30f
        val sx  = cx + (cos(ang) * sl).toFloat()
        val sy  = cy + (sin(ang) * sl).toFloat()
        // Filament
        drawLine(Color(0xFF1A0A00).copy(alpha = 0.70f), Offset(cx, cy), Offset(sx, sy), 0.9f)
        // Anther — elongated, characteristic of tulip
        val aAng = ang + PI / 2.0
        drawLine(
            Color(0xFFD4A000).copy(alpha = 0.85f),
            Offset(sx + (cos(aAng) * 2.2f).toFloat(), sy + (sin(aAng) * 2.2f).toFloat()),
            Offset(sx - (cos(aAng) * 2.2f).toFloat(), sy - (sin(aAng) * 2.2f).toFloat()),
            1.8f, cap = StrokeCap.Round
        )
        drawCircle(Color(0xFFECC010).copy(alpha = 0.72f), 1.0f, Offset(sx, sy))
    }

    // 6. Pistil
    drawCircle(Color(0xFF2A1A00).copy(alpha = 0.88f), p * 0.10f, Offset(cx, cy))
    drawCircle(Color(0xFF3A7A20).copy(alpha = 0.80f), p * 0.07f, Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = lf * 0.25f), p * 0.03f, Offset(cx - p * 0.03f, cy - p * 0.04f))
}

// ══════════════════════════════════════════════════════════════════════════════
//  CHAMOMILE  — Matricaria chamomilla, classic daisy-family wildflower
//  Passes: warm halo → 18 white ray florets → domed yellow disc
//  → disc florets in concentric rings → pollen glow → dew
// ══════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawRealisticChamomile(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p  = r * (1f + sin(phase) * 0.030f)
    val lf = litFactor(0.35f, -0.92f, amb)

    // 1. Warm meadow glow
    drawCircle(Color(0xFFD0A000).copy(alpha = 0.05f * amb), p * 2.8f, Offset(cx, cy))
    drawCircle(Color(0xFFE8C000).copy(alpha = 0.08f * amb), p * 1.8f, Offset(cx, cy))

    // 2. Cast shadow
    drawCircle(Color.Black.copy(alpha = 0.15f), p * 1.06f, Offset(cx + p * 0.04f, cy + p * 0.50f))

    // 3. Ray florets — 18 white strap-shaped petals, slightly reflexing
    val rayCount = 18
    repeat(rayCount) { i ->
        val ang  = Math.toRadians(i * (360.0 / rayCount) + phase * 2.0)
        val pf   = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb)
        val hw   = p * 0.18f
        val bx   = cx + (cos(ang) * p * 0.44f).toFloat()
        val by   = cy + (sin(ang) * p * 0.44f).toFloat()
        val px   = cx + (cos(ang) * p * 1.08f).toFloat()
        val py   = cy + (sin(ang) * p * 1.08f).toFloat()
        val perp = ang + PI / 2.0

        val pp = Path().apply {
            moveTo(bx + (cos(perp) * hw * 0.55f).toFloat(), by + (sin(perp) * hw * 0.55f).toFloat())
            cubicTo(
                bx + (cos(perp) * hw).toFloat() + (cos(ang) * p * 0.48f).toFloat(),
                by + (sin(perp) * hw).toFloat() + (sin(ang) * p * 0.48f).toFloat(),
                px + (cos(perp) * hw * 0.40f).toFloat(), py + (sin(perp) * hw * 0.40f).toFloat(),
                px, py  // slightly notched tip
            )
            cubicTo(
                px + (cos(perp) * (-hw * 0.40f)).toFloat(), py + (sin(perp) * (-hw * 0.40f)).toFloat(),
                bx + (cos(perp) * (-hw)).toFloat() + (cos(ang) * p * 0.48f).toFloat(),
                by + (sin(perp) * (-hw)).toFloat() + (sin(ang) * p * 0.48f).toFloat(),
                bx + (cos(perp) * (-hw * 0.55f)).toFloat(), by + (sin(perp) * (-hw * 0.55f)).toFloat()
            )
            close()
        }
        withTransform({ translate(1.0f, 1.6f) }) { drawPath(pp, Color.Black.copy(alpha = 0.10f)) }

        // White with slight warmth — chamomile petals are pure white/cream
        val baseC = lerp(Color(0xFFD8CEB4), Color(0xFFEEE4CA), pf * 0.45f)
        val midC  = lerp(Color(0xFFEEE4CC), Color(0xFFF8F4E0), pf * 0.55f)
        val tipC  = lerp(Color(0xFFF4F0E0), Color(0xFFFFFFFF), pf * 0.70f + lf * 0.18f)
        drawPath(pp, Brush.linearGradient(listOf(baseC, midC, tipC), Offset(cx, cy), Offset(px, py)))
        // SSS — white petals transmit warm light
        drawPath(pp, Color(0xFFFFF0C0).copy(alpha = pf * 0.08f * (0.3f + amb * 0.7f)))
        // Midrib vein
        drawLine(Color(0xFFCCBEA0).copy(alpha = 0.38f), Offset(bx, by), Offset(px, py), 0.42f)
        // Notch at tip
        drawCircle(Color(0xFFF0ECDE).copy(alpha = 0.55f), 0.8f, Offset(px, py))
    }

    // 4. Yellow disc — domed, characteristic of chamomile family
    val discR = p * 0.44f
    // Dome shape using concentric circles with gradient
    drawCircle(Color.Black.copy(alpha = 0.18f), discR * 1.05f, Offset(cx + discR * 0.04f, cy + discR * 0.55f))
    drawCircle(
        Brush.radialGradient(
            listOf(
                lerp(Color(0xFFEECC00), Color.White, lf * 0.22f + amb * 0.08f),
                Color(0xFFD4A800),
                Color(0xFFB08000),
                Color(0xFF7A5400)
            ),
            Offset(cx - discR * 0.12f, cy - discR * 0.18f), discR * 1.1f
        ), discR, Offset(cx, cy)
    )

    // 5. Disc florets — tiny tubular flowers in concentric rings
    val rings = listOf(0.72f to 12, 0.48f to 8, 0.24f to 5)
    rings.forEach { (rFrac, count) ->
        repeat(count) { k ->
            val theta = Math.toRadians(k * (360.0 / count) + rFrac * 30.0 + phase * 5.0)
            val rho   = discR * rFrac
            val fx    = cx + (cos(theta) * rho).toFloat()
            val fy    = cy + (sin(theta) * rho).toFloat()
            val fPf   = litFactor((fx - cx) / discR, (fy - cy) / discR, amb)
            val fR    = discR * 0.10f

            // Floret body
            drawCircle(lerp(Color(0xFF5A3800), Color(0xFF7A5000), fPf * 0.55f).copy(alpha = 0.85f), fR, Offset(fx, fy))
            // Open floret — tiny 5-petalled yellow tube
            drawCircle(
                Brush.radialGradient(
                    listOf(lerp(Color(0xFFEEC800), Color.White, fPf * 0.20f), Color(0xFFCC9C00)),
                    Offset(fx - fR * 0.1f, fy - fR * 0.1f), fR
                ), fR * 0.72f, Offset(fx, fy)
            )
            // Stigma dot
            if (rFrac > 0.6f) drawCircle(Color(0xFFFFE840).copy(alpha = 0.65f), fR * 0.25f, Offset(fx, fy))
        }
    }

    // 6. Pollen glow around disc
    if (amb > 0.20f) {
        drawCircle(Color(0xFFEEC800).copy(alpha = 0.12f * amb), discR * 1.5f, Offset(cx, cy))
        repeat(14) { i ->
            val ang = Math.toRadians(i * 25.7 + phase * 20.0)
            val pr2 = discR * (0.88f + hash(i.toFloat(), 70f) * 0.35f)
            drawCircle(Color(0xFFD4A820).copy(alpha = amb * 0.25f), 0.95f,
                Offset(cx + (cos(ang) * pr2).toFloat(), cy + (sin(ang) * pr2).toFloat()))
        }
    }

    // 7. Dew on petal tips (morning)
    if (amb < 0.60f) {
        listOf(0.0, 2.09, 4.19, 1.05, 3.14).forEach { angOff ->
            val dAng = angOff + phase * 0.12
            val dx   = cx + (cos(dAng) * p * 1.06f).toFloat()
            val dy   = cy + (sin(dAng) * p * 1.06f).toFloat()
            val dewA = (0.60f - amb) * 0.50f
            drawCircle(Color(0xFF88B0A0).copy(alpha = dewA * 0.30f), 2.2f, Offset(dx + 0.9f, dy + 1.0f))
            drawCircle(Brush.radialGradient(
                listOf(Color.White.copy(alpha = dewA * 0.65f), Color(0xFFFFF4D0).copy(alpha = dewA * 0.20f), Color.Transparent),
                Offset(dx - 0.8f, dy - 0.8f), 2.8f), 2.2f, Offset(dx, dy))
        }
    }
}