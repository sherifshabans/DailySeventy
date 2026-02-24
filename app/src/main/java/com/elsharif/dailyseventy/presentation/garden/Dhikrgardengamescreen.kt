package com.elsharif.dailyseventy.presentation.garden

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
//  TIME OF DAY  — real clock-based sky
// ══════════════════════════════════════════════════════════════════════════════

private fun realDayFraction(): Float {
    val cal = Calendar.getInstance()
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    return (h + m / 60f) / 24f
}

internal data class SkyPalette(
    val top: Color, val mid: Color, val horizon: Color,
    val groundTint: Color, val sunMoonY: Float,
    val sunMoonCol: Color, val ambient: Float
)

private fun skyAt(t: Float): SkyPalette = when {
    t < 0.17f || t > 0.88f -> SkyPalette(
        top = Color(0xFF03060F), mid = Color(0xFF060B1A), horizon = Color(0xFF0D1525),
        groundTint = Color(0xFF0A1208),
        sunMoonY = lerp(0.15f, 0.30f, if (t < 0.5f) t / 0.17f else (t - 0.88f) / 0.12f),
        sunMoonCol = Color(0xFFD8E8F8), ambient = 0.05f
    )
    t < 0.25f -> { val f = (t - 0.17f) / 0.08f
        SkyPalette(
            top = lerp(Color(0xFF03060F), Color(0xFF1A1035), f),
            mid = lerp(Color(0xFF060B1A), Color(0xFF3D2244), f),
            horizon = lerp(Color(0xFF0D1525), Color(0xFFB04020), f),
            groundTint = lerp(Color(0xFF0A1208), Color(0xFF1C1408), f),
            sunMoonY = lerp(0.30f, 0.55f, f),
            sunMoonCol = lerp(Color(0xFFD8E8F8), Color(0xFFFF8C42), f),
            ambient = lerp(0.05f, 0.25f, f)
        )
    }
    t < 0.33f -> { val f = (t - 0.25f) / 0.08f
        SkyPalette(
            top = lerp(Color(0xFF1A1035), Color(0xFF3B6BA8), f),
            mid = lerp(Color(0xFF3D2244), Color(0xFF7BA7D0), f),
            horizon = lerp(Color(0xFFB04020), Color(0xFFFFC06A), f),
            groundTint = lerp(Color(0xFF1C1408), Color(0xFF2A3818), f),
            sunMoonY = lerp(0.55f, 0.80f, f),
            sunMoonCol = lerp(Color(0xFFFF8C42), Color(0xFFFFF4B0), f),
            ambient = lerp(0.25f, 0.75f, f)
        )
    }
    t < 0.71f -> { val peak = if (t < 0.52f) (t - 0.33f) / 0.19f else 1f - (t - 0.52f) / 0.19f
        SkyPalette(
            top = lerp(Color(0xFF3B6BA8), Color(0xFF1A4A8C), peak * 0.3f),
            mid = lerp(Color(0xFF7BA7D0), Color(0xFF4D8CBF), peak * 0.3f),
            horizon = Color(0xFFB8D8F0), groundTint = Color(0xFF2E4A1A),
            sunMoonY = lerp(0.88f, 0.94f, abs(t - 0.52f) * 2f),
            sunMoonCol = Color(0xFFFFFAD0), ambient = 1.00f
        )
    }
    t < 0.79f -> { val f = (t - 0.71f) / 0.08f
        SkyPalette(
            top = lerp(Color(0xFF3B6BA8), Color(0xFF1A0E22), f),
            mid = lerp(Color(0xFF7BA7D0), Color(0xFF8B3A20), f),
            horizon = lerp(Color(0xFFB8D8F0), Color(0xFFFF6B1A), f),
            groundTint = lerp(Color(0xFF2E4A1A), Color(0xFF221808), f),
            sunMoonY = lerp(0.82f, 0.55f, f),
            sunMoonCol = lerp(Color(0xFFFFFAD0), Color(0xFFFF5500), f),
            ambient = lerp(1.00f, 0.20f, f)
        )
    }
    else -> { val f = (t - 0.79f) / 0.09f
        SkyPalette(
            top = lerp(Color(0xFF1A0E22), Color(0xFF03060F), f),
            mid = lerp(Color(0xFF8B3A20), Color(0xFF0D0F1E), f),
            horizon = lerp(Color(0xFFFF6B1A), Color(0xFF1A2030), f),
            groundTint = lerp(Color(0xFF221808), Color(0xFF0A1208), f),
            sunMoonY = lerp(0.55f, 0.28f, f),
            sunMoonCol = lerp(Color(0xFFFF5500), Color(0xFFD8E8F8), f),
            ambient = lerp(0.20f, 0.05f, f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  COLOR UTILITIES
// ══════════════════════════════════════════════════════════════════════════════

private fun lerp(a: Color, b: Color, t: Float): Color {
    val c = t.coerceIn(0f, 1f)
    return Color(
        (a.red + (b.red - a.red) * c).coerceIn(0f, 1f),
        (a.green + (b.green - a.green) * c).coerceIn(0f, 1f),
        (a.blue + (b.blue - a.blue) * c).coerceIn(0f, 1f),
        (a.alpha + (b.alpha - a.alpha) * c).coerceIn(0f, 1f)
    )
}

internal fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)
internal fun hash(x: Float, y: Float = 0f): Float {
    val s = sin(x * 127.1f + y * 311.7f) * 43758.5453f
    return s - floor(s)
}

// ══════════════════════════════════════════════════════════════════════════════
//  MAIN SCREEN — Scrollable
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrGardenScreen(
    onBackClick: () -> Unit,
    viewModel: DhikrGardenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val particles = remember { mutableStateListOf<FloatingParticle>() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var dayFraction by remember { mutableStateOf(realDayFraction()) }
    LaunchedEffect(Unit) {
        while (true) { delay(60_000L); dayFraction = realDayFraction() }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            particles.forEach { p -> p.life -= p.decay; p.rot += p.rotSpeed }
            particles.removeAll { it.life <= 0f }
        }
    }

    var wirdEvent by remember { mutableStateOf<GardenEvent.WirdComplete?>(null) }
    var comboEvent by remember { mutableStateOf<GardenEvent.ComboUp?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GardenEvent.PlantedSeed -> {
                    repeat(8) {
                        particles += FloatingParticle(
                            originX = event.x + Random.nextFloat() * 22f - 11f,
                            originY = event.y, color = Color(0xFF6B4226),
                            size = 3f + Random.nextFloat() * 3f, type = PType.SEED,
                            vy = -(Random.nextFloat() * 1.2f + 0.5f), decay = 0.025f
                        )
                    }
                }
                is GardenEvent.Harvested -> {
                    repeat(28) { i ->
                        val ang = (i.toFloat() / 28f) * 2f * PI.toFloat()
                        val spd = Random.nextFloat() * 2.2f + 0.5f
                        particles += FloatingParticle(
                            originX = event.x, originY = event.y,
                            color = when (event.type) {
                                PlantType.JASMINE   -> listOf(Color(0xFFF8F4EE), Color(0xFFEDE8DE), Color(0xFFD4CFC4))[i % 3]
                                PlantType.SUNFLOWER -> listOf(Color(0xFFD4920A), Color(0xFFC47E08), Color(0xFFB06A06))[i % 3]
                                PlantType.ROSE      -> listOf(Color(0xFFB01030), Color(0xFF8C0C24), Color(0xFFCC2040))[i % 3]
                                PlantType.LOTUS     -> listOf(Color(0xFFE070A0), Color(0xFFC05888), Color(0xFFFF90C0))[i % 3]
                                PlantType.LAVENDER  -> listOf(Color(0xFF9070D0), Color(0xFF7858B8), Color(0xFFB090E0))[i % 3]
                                PlantType.MINT      -> listOf(Color(0xFF40C870), Color(0xFF30A858), Color(0xFF60D888))[i % 3]
                                PlantType.TULIP     -> listOf(Color(0xFFE04040), Color(0xFFC02828), Color(0xFFFF6060))[i % 3]
                                PlantType.CHAMOMILE -> listOf(Color(0xFFF8E060), Color(0xFFD8C040), Color(0xFFFFF090))[i % 3]
                            },
                            size = 4f + Random.nextFloat() * 7f,
                            type = if (i % 5 == 0) PType.STAR else PType.PETAL,
                            vx = cos(ang) * spd, vy = sin(ang) * spd - 1.5f, decay = 0.013f
                        )
                    }
                    particles += FloatingParticle(
                        originX = event.x, originY = event.y - 35f,
                        text = "+${event.anwar} نور${if (event.combo >= 3) " ×${event.combo}" else ""}",
                        color = Color(0xFFF0D060), size = 12f, type = PType.ARABIC,
                        vx = 0f, vy = -2.4f, decay = 0.010f
                    )
                }
                is GardenEvent.PlantBloomed -> {
                    repeat(14) { i ->
                        val ang = (i.toFloat() / 14f) * 2f * PI.toFloat()
                        particles += FloatingParticle(
                            originX = event.plotId * 48f + 80f, originY = 280f,
                            color = Color(0xFFD4E8A0),
                            size = 3f + Random.nextFloat() * 4f, type = PType.SPARKLE,
                            vx = cos(ang) * 1.4f, vy = sin(ang) * 1.4f - 0.8f, decay = 0.020f
                        )
                    }
                }
                is GardenEvent.WirdComplete -> {
                    wirdEvent = event
                    repeat(60) { i ->
                        val ang = (i.toFloat() / 60f) * 2f * PI.toFloat()
                        particles += FloatingParticle(
                            originX = 180f + Random.nextFloat() * 240f, originY = 220f,
                            color = listOf(Color(0xFFFFD700), Color(0xFFF0E68C), Color(0xFF90C870), Color(0xFFD4A0C8), Color(0xFF80C8D8))[i % 5],
                            size = 3f + Random.nextFloat() * 8f,
                            type = if (i % 4 == 0) PType.STAR else PType.SPARKLE,
                            vx = cos(ang) * Random.nextFloat() * 3.5f,
                            vy = -(Random.nextFloat() * 4.5f + 1.2f), decay = 0.008f
                        )
                    }
                    scope.launch { delay(4000); wirdEvent = null }
                }
                is GardenEvent.ComboUp -> {
                    comboEvent = event
                    scope.launch { delay(1400); comboEvent = null }
                }
                else -> Unit
            }
        }
    }

    val sky = skyAt(dayFraction)
    val topBarBg = lerp(Color(0xFF0A0E08), Color(0xFF1A2E10), sky.ambient)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (sky.ambient < 0.3f) "🌙" else if (sky.ambient < 0.7f) "🌅" else "☀️", fontSize = 18.sp)
                        Column {
                            Text("روضة الذاكرين", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (sky.ambient > 0.5f) Color(0xFF1C3010) else Color(0xFFD8E8CC))
                            Text("${state.sessionAnwar} نور · ورد ${state.awradToday} اليوم",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sky.ambient > 0.5f) Color(0xFF3A6820) else Color(0xFF6A9850))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, null,
                            tint = if (sky.ambient > 0.5f) Color(0xFF2A4A18) else Color(0xFF8AB888))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBg)
            )
        },
        containerColor = sky.groundTint
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val canvasH = maxWidth * 1.05f
                    GardenCanvas(
                        state = state, particles = particles, sky = sky, dayFraction = dayFraction,
                        onTapPlot = viewModel::tapPlot,
                        onRegister = viewModel::registerPlotCenter,
                        modifier = Modifier.fillMaxWidth().height(canvasH)
                    )
                }

                DhikrControlPanel(
                    wird = state.wird,
                    combo = state.comboMultiplier.toInt(),
                    sessionAnwar = state.sessionAnwar,
                    sky = sky,
                    onDhikr = viewModel::onDhikr,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
            }

            AnimatedVisibility(
                visible = wirdEvent != null,
                enter = fadeIn(tween(380)) + scaleIn(tween(380, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(550)) + scaleOut(tween(550))
            ) { wirdEvent?.let { WirdCelebrationOverlay(it.awradTotal, sky) } }

            AnimatedVisibility(
                visible = comboEvent != null,
                enter = fadeIn(tween(120)) + slideInVertically { -55 },
                exit = fadeOut(tween(480)) + slideOutVertically { -70 },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp)
            ) {
                comboEvent?.let { evt ->
                    Surface(shape = RoundedCornerShape(50.dp), color = Color(0xFF7A2800).copy(alpha = 0.92f)) {
                        Text("×${evt.level} ${comboLabel(evt.level)}",
                            Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
                            fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFFFFF0CC))
                    }
                }
            }
        }
    }
}

private fun comboLabel(l: Int) = when (l) { 2 -> "ممتاز"; 3 -> "رائع!"; 4 -> "مبهر!"; else -> "ماشاء الله" }

// ══════════════════════════════════════════════════════════════════════════════
//  GARDEN CANVAS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun GardenCanvas(
    state: GardenGameState, particles: List<FloatingParticle>, sky: SkyPalette,
    dayFraction: Float, onTapPlot: (Int) -> Unit, onRegister: (Int, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "g")
    val wA by inf.animateFloat(-1f, 1f, infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "wA")
    val wB by inf.animateFloat(.4f, -.4f, infiniteRepeatable(tween(8100, easing = LinearEasing), RepeatMode.Reverse), "wB")
    val wC by inf.animateFloat(-.2f, .2f, infiniteRepeatable(tween(12400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "wC")
    val sway = wA * 0.80f + wB * 0.32f + wC * 0.14f
    val cdrift by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(38000, easing = LinearEasing)), "cd")
    val bloom by inf.animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(3200, easing = LinearEasing)), "bl")
    val gwave by inf.animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(5000, easing = LinearEasing)), "gw")
    val twA by inf.animateFloat(.22f, 1f, infiniteRepeatable(tween(880), RepeatMode.Reverse), "tA")
    val twB by inf.animateFloat(1f, .15f, infiniteRepeatable(tween(1420), RepeatMode.Reverse), "tB")
    val twC by inf.animateFloat(.38f, 1f, infiniteRepeatable(tween(2100), RepeatMode.Reverse), "tC")
    val birdPh by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), "bp")
    val mistDr by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(22000, easing = LinearEasing)), "md")

    Canvas(modifier = modifier.pointerInput(Unit) {
        detectTapGestures { offset ->
            val W = size.width.toFloat(); val H = size.height.toFloat()
            for (row in 0 until 3) for (col in 0 until 4) {
                val pr = plotRect(row, col, W, H)
                if (offset.x in pr.left..(pr.left + pr.width) &&
                    offset.y in pr.top..(pr.top + pr.height)) {
                    onTapPlot(row * 4 + col); return@detectTapGestures
                }
            }
        }
    }) {
        val W = size.width; val H = size.height
        for (row in 0 until 3) for (col in 0 until 4) {
            val pr = plotRect(row, col, W, H)
            onRegister(row * 4 + col, pr.cx, pr.cy)
        }

        drawRealisticSky(W, H, sky, cdrift, twA, twB, twC, dayFraction)
        drawDistantMountains(W, H, sky)
        drawTreeline(W, H, sky, sway)
        drawGround(W, H, sky)
        drawGardenWall(W, H, sky)
        drawRealGrass(W, H, sway, gwave, sky)
        drawGardenPath(W, H, sky)
        if (sky.ambient < 0.25f) drawMorningMist(W, H, mistDr, sky)

        // ══ PASS 1: كل الـ soil beds ══
        for (row in 0 until 3) {
            for (col in 0 until 4) {
                val plot = state.plots[row * 4 + col]
                val pr = plotRect(plot.row, plot.col, W, H)
                drawSoilBed(pr, row.toFloat(), sky)
            }
        }

        // ══ PASS 2: النباتات من الخلف للأمام — الأمامي يغطي الخلفي طبيعياً (3D) ══
        // كل نبتة ترسم كاملة بدون clip — مفيش قطع لرؤوس الورود
        for (row in 0 until 3) {
            for (col in 0 until 4) {
                val plot = state.plots[row * 4 + col]
                val pr = plotRect(plot.row, plot.col, W, H)
                if (!plot.isEmpty) {
                    drawRealisticPlant(pr, plot, sway, bloom, state.comboMultiplier, row, sky)
                }
            }
        }

        // ══ PASS 3: المؤشرات والـ bloom aura فوق كل حاجة ══
        for (row in 0 until 3) {
            for (col in 0 until 4) {
                val plot = state.plots[row * 4 + col]
                val pr = plotRect(plot.row, plot.col, W, H)
                if (!plot.isEmpty && !plot.isBloom) {
                    drawGrowthIndicator(pr, plot.stageProgress, plot.type!!, sky)
                }
                if (plot.isBloom) drawBloomAura(pr, plot.type!!, bloom, sky)
            }
        }

        if (sky.ambient > 0.15f && sky.ambient < 0.85f) drawBirds(W, H, birdPh, sky)
        drawWirdArc(W, H, state.wird, sky)
        particles.forEach { drawParticle(it) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  REALISTIC SKY
// ══════════════════════════════════════════════════════════════════════════════

private data class NaturalCloud(val rx: Float, val ry: Float, val scale: Float, val speed: Float)
private val CLOUDS = List(7) {
    NaturalCloud(Random.nextFloat(), Random.nextFloat() * 0.55f + 0.05f,
        0.6f + Random.nextFloat() * 0.9f, 0.3f + Random.nextFloat() * 0.7f)
}

private data class StarData(val rx: Float, val ry: Float, val r: Float, val ti: Int)
private val STARS = List(180) {
    StarData(Random.nextFloat(), Random.nextFloat() * 0.65f,
        Random.nextFloat() * 1.8f + 0.2f, it % 3)
}

private fun DrawScope.drawRealisticSky(
    W: Float, H: Float, sky: SkyPalette, cdrift: Float,
    twA: Float, twB: Float, twC: Float, dayF: Float
) {
    val skyH = H * 0.52f
    val tws = listOf(twA, twB, twC)
    drawRect(Brush.verticalGradient(listOf(sky.top, sky.mid, sky.horizon), 0f, skyH), size = Size(W, skyH))

    if (sky.ambient < 0.40f) {
        val starAlpha = (0.40f - sky.ambient) / 0.40f
        STARS.forEach { s ->
            val al = tws[s.ti] * starAlpha * (0.30f + (s.r / 2.0f) * 0.55f)
            val sx = s.rx * W; val sy = s.ry * skyH
            if (al > 0.05f) {
                if (s.r > 1.2f) drawCircle(Color.White.copy(alpha = al * 0.08f), s.r * 2.8f, Offset(sx, sy))
                drawCircle(Color.White.copy(alpha = al * 0.85f), s.r * 0.42f, Offset(sx, sy))
            }
        }
    }

    val bodyX = W * 0.78f; val bodyY = skyH * (1f - sky.sunMoonY)
    if (sky.ambient > 0.20f) {
        val sunR = 18f + sky.ambient * 6f
        listOf(6.0f to 0.04f, 4.0f to 0.08f, 2.6f to 0.14f, 1.8f to 0.22f).forEach { (rm, al) ->
            drawCircle(sky.sunMoonCol.copy(alpha = al * sky.ambient), sunR * rm, Offset(bodyX, bodyY))
        }
        drawCircle(Brush.radialGradient(listOf(Color.White, sky.sunMoonCol, sky.sunMoonCol.copy(alpha = 0.7f)), Offset(bodyX, bodyY), sunR), sunR, Offset(bodyX, bodyY))
        if (sky.ambient in 0.20f..0.55f) {
            val rayAlpha = (0.55f - abs(sky.ambient - 0.375f) / 0.375f).coerceIn(0f, 0.18f)
            repeat(8) { i ->
                val ang = Math.toRadians(i * 45.0 + 10.0)
                drawLine(sky.sunMoonCol.copy(alpha = rayAlpha), Offset(bodyX, bodyY),
                    Offset(bodyX + (cos(ang) * W * 0.55f).toFloat(), bodyY + (sin(ang) * H * 0.30f).toFloat()),
                    2.5f + i * 0.4f, cap = StrokeCap.Round)
            }
        }
    } else {
        val mr = 19f
        listOf(5.0f to 0.04f, 3.0f to 0.08f, 1.8f to 0.14f).forEach { (rm, al) ->
            drawCircle(Color(0xFFD8E8F0).copy(alpha = al), mr * rm, Offset(bodyX, bodyY))
        }
        drawCircle(Brush.radialGradient(listOf(Color(0xFFF0F4F8), Color(0xFFD8E0E8), Color(0xFFB8C8D4)), Offset(bodyX - mr * 0.1f, bodyY - mr * 0.1f), mr), mr, Offset(bodyX, bodyY))
        drawCircle(sky.top.copy(alpha = 0.94f), mr * 0.82f, Offset(bodyX + mr * 0.40f, bodyY - mr * 0.05f))
        listOf(-0.1f to -0.2f, 0.2f to 0.15f, -0.25f to 0.18f).forEach { (ox, oy) ->
            drawCircle(Color(0xFF7090A8).copy(alpha = 0.18f), mr * 0.22f, Offset(bodyX + ox * mr, bodyY + oy * mr))
        }
    }

    val cloudAlpha = when { sky.ambient > 0.85f -> 0.92f; sky.ambient > 0.40f -> 0.75f; else -> 0.35f }
    val cloudLit = lerp(Color(0xFF606870), Color(0xFFF8F6F2), sky.ambient)
    val cloudDark = lerp(Color(0xFF1A1E22), Color(0xFFD0CEC8), sky.ambient)
    CLOUDS.forEach { c ->
        val cx = ((c.rx + cdrift * c.speed * 0.25f) % 1.2f - 0.1f) * W
        val cy = c.ry * skyH * 0.90f
        drawNaturalCloud(cx, cy, c.scale * 22f, cloudLit, cloudDark, cloudAlpha)
    }

    val glowCol = if (sky.ambient > 0.3f)
        lerp(sky.horizon, Color.White, 0.15f).copy(alpha = 0.35f * sky.ambient)
    else sky.horizon.copy(alpha = 0.55f)
    drawRect(Brush.verticalGradient(listOf(Color.Transparent, glowCol), skyH * 0.70f, skyH), Offset(0f, skyH * 0.70f), Size(W, skyH * 0.30f))
}

private fun DrawScope.drawNaturalCloud(cx: Float, cy: Float, r: Float, lit: Color, dark: Color, alpha: Float) {
    drawCircle(Color.Black.copy(alpha = alpha * 0.08f), r * 1.05f, Offset(cx + r * 0.06f, cy + r * 0.62f))
    drawCircle(dark.copy(alpha = alpha * 0.78f), r, Offset(cx, cy + r * 0.12f))
    drawCircle(dark.copy(alpha = alpha * 0.72f), r * 0.80f, Offset(cx + r * 0.88f, cy + r * 0.20f))
    drawCircle(dark.copy(alpha = alpha * 0.68f), r * 0.70f, Offset(cx - r * 0.82f, cy + r * 0.22f))
    drawCircle(lit.copy(alpha = alpha * 0.90f), r * 0.96f, Offset(cx, cy - r * 0.08f))
    drawCircle(lit.copy(alpha = alpha * 0.80f), r * 0.75f, Offset(cx + r * 0.86f, cy))
    drawCircle(lit.copy(alpha = alpha * 0.75f), r * 0.64f, Offset(cx - r * 0.80f, cy))
    drawCircle(lit.copy(alpha = alpha * 0.72f), r * 0.50f, Offset(cx + r * 0.28f, cy - r * 0.25f))
    drawCircle(Color.White.copy(alpha = alpha * 0.22f), r * 0.32f, Offset(cx - r * 0.12f, cy - r * 0.28f))
}

// ══════════════════════════════════════════════════════════════════════════════
//  MOUNTAINS / TREELINE / GROUND / WALL / GRASS / PATH
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawDistantMountains(W: Float, H: Float, sky: SkyPalette) {
    val base = H * 0.52f
    data class ML(val yFrac: Float, val darkBase: Color, val litTop: Color, val alpha: Float)
    listOf(
        ML(0.16f, lerp(sky.mid, sky.horizon, 0.30f), lerp(sky.horizon, Color.White, 0.12f), 0.55f),
        ML(0.11f, lerp(sky.mid, sky.groundTint, 0.25f), lerp(sky.horizon, Color.White, 0.06f), 0.70f),
        ML(0.07f, lerp(sky.groundTint, Color.Black, 0.15f), sky.groundTint, 0.85f)
    ).forEachIndexed { li, ml ->
        val path = Path().apply {
            moveTo(0f, H); lineTo(0f, base - H * ml.yFrac * 0.5f)
            var tx = 0f
            while (tx < W) {
                val pk = sin(tx * 0.012f + li * 2.1f) * H * ml.yFrac + sin(tx * 0.031f + li * 1.3f) * H * ml.yFrac * 0.40f + sin(tx * 0.074f + li * 0.7f) * H * ml.yFrac * 0.18f
                lineTo(tx, base - pk.coerceAtLeast(0f)); tx += 4f
            }
            lineTo(W, H); close()
        }
        drawPath(path, Brush.verticalGradient(listOf(ml.litTop.copy(alpha = ml.alpha), ml.darkBase.copy(alpha = ml.alpha)), base - H * ml.yFrac, base))
    }
}

private fun DrawScope.drawTreeline(W: Float, H: Float, sky: SkyPalette, sway: Float) {
    val baseY = H * 0.52f
    val treeCol = lerp(Color(0xFF0D1A08), Color(0xFF1C3A14), sky.ambient * 0.7f)
    val treeAlpha = lerp(0.55f, 0.88f, sky.ambient)
    var tx = -15f
    while (tx < W + 15f) {
        val n1 = hash(tx, 1f); val n2 = hash(tx, 2f)
        val treeH = H * (0.05f + n1 * 0.07f); val treeW = treeH * (0.20f + n2 * 0.18f)
        val lean = sway * (1.2f + n1 * 0.8f) * 0.4f
        when ((tx * 0.07f).toInt() % 3) {
            0 -> {
                val path = Path().apply {
                    moveTo(tx + lean, baseY - treeH)
                    repeat(3) { i ->
                        val ly = baseY - treeH * (0.78f - i * 0.24f); val lw = treeW * (0.45f + i * 0.28f)
                        lineTo(tx - lw + lean * (1f - i * 0.25f), ly); lineTo(tx + lw + lean * (1f - i * 0.25f), ly)
                    }
                    lineTo(tx + treeW * 0.12f, baseY); lineTo(tx - treeW * 0.12f, baseY); close()
                }
                drawPath(path, treeCol.copy(alpha = treeAlpha))
            }
            1 -> {
                val trunkH = treeH * 0.38f
                drawLine(lerp(Color(0xFF2A1A08), Color.Black, 0.3f).copy(alpha = treeAlpha), Offset(tx, baseY), Offset(tx + lean * 0.5f, baseY - trunkH), treeW * 0.18f, cap = StrokeCap.Round)
                drawCircle(treeCol.copy(alpha = treeAlpha * 0.85f), treeW * 0.65f, Offset(tx + lean * 0.7f, baseY - treeH * 0.80f))
                drawCircle(treeCol.copy(alpha = treeAlpha * 0.75f), treeW * 0.52f, Offset(tx + lean + treeW * 0.28f, baseY - treeH * 0.68f))
                drawCircle(treeCol.copy(alpha = treeAlpha * 0.72f), treeW * 0.48f, Offset(tx + lean - treeW * 0.30f, baseY - treeH * 0.70f))
            }
            else -> {
                val path = Path().apply {
                    moveTo(tx + lean, baseY - treeH)
                    cubicTo(tx + treeW * 0.38f + lean * 0.7f, baseY - treeH * 0.65f, tx + treeW * 0.45f + lean * 0.5f, baseY - treeH * 0.35f, tx + treeW * 0.22f, baseY)
                    lineTo(tx - treeW * 0.22f, baseY)
                    cubicTo(tx - treeW * 0.45f + lean * 0.5f, baseY - treeH * 0.35f, tx - treeW * 0.38f + lean * 0.7f, baseY - treeH * 0.65f, tx + lean, baseY - treeH)
                    close()
                }
                drawPath(path, treeCol.copy(alpha = treeAlpha))
            }
        }
        tx += treeW * 0.9f + 4f + n2 * 18f
    }
}

private fun DrawScope.drawGround(W: Float, H: Float, sky: SkyPalette) {
    val gY = H * 0.51f
    drawRect(Brush.verticalGradient(listOf(lerp(Color(0xFF0E1A08), Color(0xFF1E3A12), sky.ambient * 0.80f), lerp(Color(0xFF0A1206), Color(0xFF163008), sky.ambient * 0.70f), Color(0xFF060A04)), gY, H), Offset(0f, gY), Size(W, H - gY))
}

private fun DrawScope.drawGardenWall(W: Float, H: Float, sky: SkyPalette) {
    val wTop = H * 0.512f; val wBot = H * 0.558f; val wH = wBot - wTop
    val stoneBase = lerp(Color(0xFF1A2018), Color(0xFF3A4A36), sky.ambient * 0.65f)
    val stoneLit = lerp(Color(0xFF242C22), Color(0xFF4E6248), sky.ambient * 0.60f)
    drawRect(Brush.verticalGradient(listOf(stoneLit, stoneBase), wTop, wBot), Offset(0f, wTop), Size(W, wH))
    var sx = -4f
    while (sx < W + 4f) {
        val sw = 28f + hash(sx, 5f) * 22f; val sh = wH * (0.55f + hash(sx, 6f) * 0.35f)
        val sy = wTop + (wH - sh) * hash(sx, 7f) * 0.45f
        drawRoundRect(stoneLit.copy(alpha = 0.72f), Offset(sx + 1f, sy + 1f), Size(sw - 2f, sh - 2f), CornerRadius(2f))
        drawRect(Color.Black.copy(alpha = 0.42f), Offset(sx - 0.5f, wTop), Size(1f, wH))
        if (hash(sx, 9f) > 0.55f) drawCircle(lerp(Color(0xFF2A5A28), Color(0xFF3A7A36), sky.ambient * 0.6f).copy(alpha = 0.52f), 3f + hash(sx, 10f) * 7f, Offset(sx + sw * 0.5f, wBot - 4f))
        sx += sw + hash(sx, 8f) * 3f + 1.5f
    }
    drawRect(stoneLit.copy(alpha = 0.45f), Offset(0f, wTop), Size(W, 1.8f))
}

private fun DrawScope.drawRealGrass(W: Float, H: Float, sway: Float, wave: Float, sky: SkyPalette) {
    val gY = H * 0.51f; val amb = sky.ambient
    val grassDark = lerp(Color(0xFF0A1A08), Color(0xFF1A3A12), amb * 0.75f)
    val grassMid = lerp(Color(0xFF0E2210), Color(0xFF2A5A1C), amb * 0.80f)
    val grassTip = lerp(Color(0xFF142818), Color(0xFF3A7828), amb * 0.85f)
    val grassDew = lerp(Color(0xFF1A3020), Color(0xFF4A9840), amb * 0.60f)
    data class GL(val dark: Color, val mid: Color, val tip: Color, val maxH: Float, val sp: Float, val ws: Float, val bw: Float)
    listOf(
        GL(grassDark, grassMid, grassTip, 22f, 32f, 2.8f, 1.6f),
        GL(lerp(grassDark, grassMid, 0.35f), lerp(grassMid, grassTip, 0.35f), grassTip, 16f, 21f, 2.2f, 1.3f),
        GL(lerp(grassDark, grassMid, 0.65f), lerp(grassMid, grassTip, 0.65f), grassDew, 12f, 15f, 1.7f, 1.0f),
        GL(grassMid, grassTip, grassDew, 9f, 10f, 1.2f, 0.8f)
    ).forEachIndexed { li, l ->
        val yBase = gY + li * 2.2f; val bend = sway * l.ws; var gx = 0f
        while (gx < W) {
            val h2 = hash(gx, li.toFloat())
            val ht = l.maxH * (0.50f + h2 * 0.36f + sin(gx * 0.08f + li * 1.7f + wave) * 0.14f)
            val lean = sin(gx * 0.11f + li * 0.9f) * 1.8f
            val tipX = gx + bend + lean + sin(gx * 0.065f + wave * 0.45f) * 1.0f; val tipY = yBase - ht
            drawLine(Color.Black.copy(alpha = 0.12f), Offset(gx + 1f, yBase), Offset(tipX + 0.7f, tipY + 1f), l.bw * 0.75f, cap = StrokeCap.Round)
            drawLine(l.dark.copy(alpha = 0.94f), Offset(gx, yBase), Offset(tipX, tipY), l.bw, cap = StrokeCap.Round)
            drawLine(l.mid.copy(alpha = 0.58f), Offset(gx - l.bw * 0.18f, yBase - ht * 0.08f), Offset(tipX - l.bw * 0.35f, tipY), l.bw * 0.42f, cap = StrokeCap.Round)
            if (li == 3 && amb < 0.65f && (gx * 7f).toInt() % 8 == 0) drawCircle(Color.White.copy(alpha = (0.65f - amb) * 0.72f), 1.1f, Offset(tipX, tipY + 0.3f))
            gx += l.sp * (0.74f + h2 * 0.26f + sin(gx * 0.032f) * 0.09f)
        }
    }
}

private fun DrawScope.drawGardenPath(W: Float, H: Float, sky: SkyPalette) {
    val pX = W * 0.44f; val pW = W * 0.16f; val pTop = H * 0.515f; val pBot = H * 0.95f
    val pathCol = lerp(Color(0xFF1A1C14), Color(0xFF2E3028), sky.ambient * 0.55f)
    drawRect(Brush.verticalGradient(listOf(pathCol.copy(alpha = 0.82f), pathCol.copy(alpha = 0.94f)), pTop, pBot), Offset(pX, pTop), Size(pW, pBot - pTop))
    var py = pTop + 5f; var row = 0
    while (py < pBot - 10f) {
        val sh = 14f + hash(row.toFloat(), 11f) * 12f; val off = if (row % 2 == 0) 0f else pW * 0.12f
        val lw = pW * (0.40f + hash(row.toFloat(), 12f) * 0.14f)
        naturalStone(pX + off + 3f, py + 2f, lw - 4f, sh - 4f, row.toFloat(), sky)
        val rx = pX + off + lw + 3f; val rw = pX + pW - off - 6f - lw
        if (rw > 7f) naturalStone(rx, py + 2f, rw - 4f, sh - 4f, (row + 100).toFloat(), sky)
        drawRect(Color.Black.copy(alpha = 0.60f), Offset(pX + 1f, py), Size(pW - 2f, 2.2f))
        py += sh + 2.2f; row++
    }
}

private fun DrawScope.naturalStone(x: Float, y: Float, w: Float, h: Float, s: Float, sky: SkyPalette) {
    val cr = CornerRadius(3f + hash(s, 13f) * 4f)
    drawRoundRect(Color.Black.copy(alpha = 0.22f), Offset(x + 2f, y + 2f), Size(w, h), cr)
    val stoneCol = lerp(lerp(Color(0xFF1E2218), Color(0xFF262C20), hash(s, 14f)), lerp(Color(0xFF2A3224), Color(0xFF323E2C), hash(s, 14f)), sky.ambient * 0.6f)
    drawRoundRect(stoneCol, Offset(x, y), Size(w, h), cr)
    drawRoundRect(lerp(stoneCol, Color.White, sky.ambient * 0.08f).copy(alpha = 0.55f), Offset(x + 1f, y + 1f), Size(w - 2f, h * 0.28f), cr)
    drawRoundRect(Color.Black.copy(alpha = 0.32f), Offset(x, y), Size(w, h), cr, style = Stroke(0.9f))
}

private fun DrawScope.drawMorningMist(W: Float, H: Float, drift: Float, sky: SkyPalette) {
    val gY = H * 0.51f; val mistAlpha = (0.25f - sky.ambient).coerceIn(0f, 0.25f) / 0.25f
    val dx = drift * W * 0.12f
    repeat(6) { i ->
        val mx = W * (0.06f + i * 0.18f) + dx * (if (i % 2 == 0) 1f else -0.55f); val mw = W * (0.22f + i * 0.05f)
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFB8C8B0).copy(alpha = mistAlpha * (0.06f + i * 0.008f)), Color.Transparent), mx - mw * 0.5f, mx + mw * 0.5f), Offset(mx - mw * 0.5f, gY - 20f), Size(mw, 48f))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SOIL BED
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSoilBed(pr: PlotRect, rowF: Float, sky: SkyPalette) {
    val r = CornerRadius(5f)
    drawRoundRect(Color.Black.copy(alpha = 0.24f), Offset(pr.left + 2f, pr.top + 3f), Size(pr.width, pr.height), r)
    val soilTop = lerp(Color(0xFF1A0E06), Color(0xFF281408), sky.ambient * 0.45f)
    val soilBot = lerp(Color(0xFF0E0804), Color(0xFF160A05), sky.ambient * 0.35f)
    drawRoundRect(Brush.verticalGradient(listOf(soilTop, soilBot), pr.top, pr.top + pr.height), Offset(pr.left, pr.top), Size(pr.width, pr.height), r)
    repeat(4) { i ->
        val px = pr.left + pr.width * (0.12f + hash(i.toFloat(), rowF + 20f) * 0.76f)
        val py = pr.top + pr.height * (0.22f + hash(i.toFloat(), rowF + 21f) * 0.56f)
        drawCircle(Color(0xFF080402).copy(alpha = 0.25f + hash(i.toFloat(), rowF + 22f) * 0.15f), pr.width * (0.04f + hash(i.toFloat(), rowF + 23f) * 0.07f), Offset(px, py))
    }
    drawRoundRect(soilTop.copy(alpha = 0.22f), Offset(pr.left, pr.top), Size(pr.width, 2.5f), r)
    drawRoundRect(lerp(Color(0xFF3A2014), Color.White, sky.ambient * 0.05f).copy(alpha = 0.30f), Offset(pr.left, pr.top), Size(pr.width, pr.height), r, style = Stroke(1.0f))
}

// ══════════════════════════════════════════════════════════════════════════════
//  PLANT DISPATCHER
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawRealisticPlant(
    pr: PlotRect, plot: GardenPlot, sway: Float, bloom: Float, combo: Float, row: Int, sky: SkyPalette
) {
    val type = plot.type ?: return
    val cx = pr.cx
    val soilY = pr.top + pr.height * 0.75f
    // maxH = المساحة المتاحة للنبتة فوق التربة
    // الصف الخلفي أصغر perspective، الأمامي أكبر
    val rowHMul = when (row) { 0 -> 1.6f; 1 -> 1.4f; else -> 1.2f }
    val maxH = pr.height * rowHMul
    val bend = sway * (1.8f - row * 0.22f)
    val amb = sky.ambient

    // ✅ الحل الجذري: clip أفقي فقط (لا نقطع الارتفاع أبداً)
    // الـ painter's algorithm في الـ render loop هو اللي بيعمل الـ 3D overlap
    // النبتة الأمامية بترسم فوق الخلفية تلقائياً
    clipRect(
        left   = pr.left,
        top    = 0f,                  // من أعلى الـ canvas — النبتة تطلع قد ما تشاء
        right  = pr.left + pr.width,
        bottom = size.height          // لأسفل الـ canvas
    ) {
        when (plot.stage) {
            PlantStage.SEED     -> drawRealisticSeed(cx, soilY, pr.width * 0.12f, type, amb)
            PlantStage.SPROUT   -> drawRealisticSprout(cx, soilY, maxH * 0.15f, bend, type, amb)
            PlantStage.YOUNG    -> drawRealisticYoung(cx, soilY, maxH * 0.36f, bend, type, amb)
            PlantStage.MATURE   -> drawRealisticMature(cx, soilY, maxH * 0.62f, bend, type, amb)
            PlantStage.BLOOMING -> drawRealisticBlooming(cx, soilY, maxH, bend, bloom, type, combo, amb)
            else -> Unit
        }
    }
}

// ── LIGHTING ──────────────────────────────────────────────────────────────────

private fun lightDir(amb: Float) = if (amb > 0.5f) Offset(0.45f, -0.82f) else Offset(-0.35f, -0.82f)
internal fun litFactor(nx: Float, ny: Float, amb: Float): Float {
    val ld = lightDir(amb)
    return ((ld.x * nx + ld.y * ny) * 0.5f + 0.5f).coerceIn(0.0f, 1.0f)
}

private fun DrawScope.stemNatural(path: Path, w: Float, t: PlantType, amb: Float) {
    val stemDark = lerp(t.stemColor, Color.Black, 0.55f)
    val stemLit = lerp(t.stemColor, Color.White, amb * 0.10f)
    drawPath(path, Color.Black.copy(alpha = 0.18f), style = Stroke(w * 1.40f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, stemDark, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, stemLit.copy(alpha = 0.65f), style = Stroke(w * 0.44f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.leafNatural(cx: Float, cy: Float, lw: Float, lh: Float, t: PlantType, alpha: Float, amb: Float) {
    val lf = litFactor(-0.5f, -0.8f, amb)
    val dark = lerp(t.stemColor, Color.Black, 0.52f)
    val mid = t.stemColor
    val lit = lerp(t.stemColor, Color.White, lf * 0.14f + amb * 0.04f)
    drawOval(Color.Black.copy(alpha = 0.16f), Offset(cx - lw * 0.5f + 1.5f, cy - lh * 0.44f + 2f), Size(lw, lh))
    drawOval(dark.copy(alpha = alpha * 0.92f), Offset(cx - lw * 0.5f, cy - lh * 0.44f), Size(lw, lh))
    drawOval(Brush.linearGradient(listOf(lit.copy(alpha = alpha * 0.80f), mid.copy(alpha = alpha * 0.85f), dark.copy(alpha = alpha)), Offset(cx - lw * 0.5f, cy - lh * 0.5f), Offset(cx + lw * 0.5f, cy + lh * 0.5f)), Offset(cx - lw * 0.5f, cy - lh * 0.44f), Size(lw, lh))
    drawLine(dark.copy(alpha = alpha * 0.45f), Offset(cx - lw * 0.42f, cy), Offset(cx + lw * 0.42f, cy), 0.5f)
    if (lw > 10f && amb > 0.3f) drawCircle(Color.White.copy(alpha = amb * 0.25f), 1.0f, Offset(cx + lw * 0.35f, cy - lh * 0.12f))
}

// ── STAGES ────────────────────────────────────────────────────────────────────

private fun DrawScope.drawRealisticSeed(cx: Float, soilY: Float, r: Float, t: PlantType, amb: Float) {
    drawOval(lerp(Color(0xFF1A0E06), Color(0xFF281408), amb * 0.4f).copy(alpha = 0.65f), Offset(cx - r * 1.4f, soilY - r * 0.55f), Size(r * 2.8f, r * 1.1f))
    val sc = lerp(lerp(t.petalColor, Color(0xFF7A4A28), 0.72f), Color.White, amb * 0.05f)
    drawOval(Color.Black.copy(alpha = 0.22f), Offset(cx - r * 0.52f + 1f, soilY - r * 0.96f + 1.5f), Size(r, r * 0.58f))
    drawOval(Brush.radialGradient(listOf(lerp(sc, Color.White, 0.12f + amb * 0.05f), sc, lerp(sc, Color.Black, 0.32f)), Offset(cx - r * 0.18f, soilY - r * 0.90f), r * 0.75f), Offset(cx - r * 0.50f, soilY - r * 1.00f), Size(r, r * 0.58f))
    drawLine(lerp(t.stemColor, Color.Black, 0.45f).copy(alpha = 0.55f), Offset(cx, soilY - r * 0.58f), Offset(cx + r * 0.06f, soilY - r * 1.28f), 1.2f, cap = StrokeCap.Round)
}

private fun DrawScope.drawRealisticSprout(cx: Float, soilY: Float, height: Float, bend: Float, t: PlantType, amb: Float) {
    val tipX = cx + bend * 0.35f; val tipY = soilY - height
    val path = Path().apply { moveTo(cx, soilY); cubicTo(cx - bend * 0.08f, soilY - height * 0.42f, cx + bend * 0.18f, soilY - height * 0.76f, tipX, tipY) }
    stemNatural(path, 2.0f, t, amb)
    val lr = height * 0.28f; val midX = lerp(cx, tipX, 0.62f); val midY = soilY - height * 0.62f
    withTransform({ rotate(-30f, Offset(midX, midY)) }) { leafNatural(midX, midY, lr * 1.4f, lr * 0.62f, t, 0.78f, amb) }
    withTransform({ rotate(30f, Offset(midX, midY)) }) { leafNatural(midX, midY, lr * 1.4f, lr * 0.62f, t, 0.82f, amb) }
}

private fun DrawScope.drawRealisticYoung(cx: Float, soilY: Float, height: Float, bend: Float, t: PlantType, amb: Float) {
    val tipX = cx + bend * 0.65f; val tipY = soilY - height
    val path = Path().apply { moveTo(cx, soilY); cubicTo(cx + bend * 0.16f, soilY - height * 0.40f, cx + bend * 0.50f, soilY - height * 0.70f, tipX, tipY) }
    stemNatural(path, 2.6f, t, amb)
    val lr = height * 0.21f
    repeat(3) { i ->
        val t2 = 0.28f + i * 0.24f; val lx = lerp(cx, tipX, t2); val ly = soilY - height * t2
        withTransform({ rotate(if (i % 2 == 0) -36f else 38f, Offset(lx, ly)) }) { leafNatural(lx, ly, lr * 1.68f, lr * 0.72f, t, 0.72f + i * 0.04f, amb) }
    }
    drawCircle(lerp(t.stemColor, Color.Black, 0.45f).copy(alpha = 0.80f), height * 0.065f, Offset(tipX, tipY))
    drawCircle(lerp(t.stemColor, Color.White, amb * 0.06f).copy(alpha = 0.52f), height * 0.035f, Offset(tipX - height * 0.011f, tipY - height * 0.011f))
}

private fun DrawScope.drawRealisticMature(cx: Float, soilY: Float, height: Float, bend: Float, t: PlantType, amb: Float) {
    val tipX = cx + bend * 0.85f; val tipY = soilY - height
    val path = Path().apply { moveTo(cx, soilY); cubicTo(cx + bend * 0.20f, soilY - height * 0.44f, cx + bend * 0.60f, soilY - height * 0.73f, tipX, tipY) }
    stemNatural(path, 3.1f, t, amb)
    val lr = height * 0.24f
    repeat(4) { i ->
        val t2 = 0.22f + i * 0.18f; val lx = lerp(cx, tipX, t2); val ly = soilY - height * t2
        withTransform({ rotate(if (i % 2 == 0) -42f else 43f, Offset(lx, ly)) }) { leafNatural(lx, ly, lr * 1.78f, lr * 0.76f, t, 0.76f, amb) }
    }
    val budR = height * 0.115f
    drawCircle(lerp(t.stemColor, Color.Black, 0.40f).copy(alpha = 0.84f), budR, Offset(tipX, tipY))
    drawCircle(lerp(t.petalColor, t.stemColor, 0.55f).copy(alpha = 0.72f), budR * 0.66f, Offset(tipX, tipY))
    repeat(5) { i ->
        val ang = Math.toRadians(i * 72.0 - 90.0)
        drawLine(t.stemColor.copy(alpha = 0.58f), Offset(tipX, tipY), Offset(tipX + (cos(ang) * budR * 0.90f).toFloat(), tipY + (sin(ang) * budR * 0.90f).toFloat()), 0.7f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawRealisticBlooming(
    cx: Float, cy: Float, height: Float, bend: Float,
    phase: Float, t: PlantType, combo: Float, amb: Float
) {
    val tipX = cx + bend * 1.02f
    val tipY = cy - height
    val path = Path().apply {
        moveTo(cx, cy)
        cubicTo(cx + bend * 0.24f, cy - height * 0.46f, cx + bend * 0.66f, cy - height * 0.75f, tipX, tipY)
    }
    stemNatural(path, 3.7f, t, amb)
    val lr = height * 0.27f
    repeat(4) { i ->
        val t2 = 0.26f + i * 0.17f
        val lx = lerp(cx, tipX, t2); val ly = cy - height * t2
        withTransform({ rotate(if (i % 2 == 0) -44f else 45f, Offset(lx, ly)) }) { leafNatural(lx, ly, lr * 1.88f, lr * 0.79f, t, 0.79f, amb) }
    }
    val petalR = height * 0.172f + combo * 1.1f
    when (t) {
        PlantType.JASMINE   -> drawRealisticJasmine(tipX, tipY, petalR, phase, amb)
        PlantType.SUNFLOWER -> drawRealisticSunflower(tipX, tipY, petalR, phase, amb)
        PlantType.ROSE      -> drawRealisticRose(tipX, tipY, petalR, phase, amb)
        PlantType.LOTUS     -> drawRealisticLotus(tipX, tipY, petalR, phase, amb)
        PlantType.LAVENDER  -> drawRealisticLavender(tipX, tipY, petalR, phase, amb)
        PlantType.MINT      -> drawRealisticMint(tipX, tipY, petalR, phase, amb)
        PlantType.TULIP     -> drawRealisticTulip(tipX, tipY, petalR, phase, amb)
        PlantType.CHAMOMILE -> drawRealisticChamomile(tipX, tipY, petalR, phase, amb)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  FLOWER RENDERERS — Jasmine / Sunflower / Rose (unchanged)
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawRealisticJasmine(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p = r * (1f + sin(phase) * 0.038f); val lf = litFactor(-0.42f, -0.90f, amb)
    val openF = (1f - amb * 0.18f).coerceIn(0.82f, 1f)
    val nightGlow = ((0.40f - amb) / 0.40f).coerceIn(0f, 1f)
    if (nightGlow > 0.02f) {
        drawCircle(Color(0xFFEEF8EC).copy(alpha = nightGlow * 0.055f), p * 4.2f, Offset(cx, cy))
        drawCircle(Color(0xFFF4FCF0).copy(alpha = nightGlow * 0.095f), p * 2.8f, Offset(cx, cy))
        drawCircle(Color(0xFFFAFEF8).copy(alpha = nightGlow * 0.140f), p * 1.9f, Offset(cx, cy))
    }
    drawCircle(Color.Black.copy(alpha = 0.18f), p * 1.12f, Offset(cx + p * 0.06f, cy + p * 0.55f))
    repeat(5) { i ->
        val ang = Math.toRadians(i * 72.0 - 90.0); val sLen = p * 0.72f; val sW = p * 0.13f
        val tx = cx + (cos(ang) * sLen).toFloat(); val ty = cy + (sin(ang) * sLen).toFloat()
        val lx = cx + (cos(ang) * p * 0.22f).toFloat(); val ly = cy + (sin(ang) * p * 0.22f).toFloat()
        val litC = lerp(Color(0xFF1E4820), Color(0xFF3A7A38), lf * 0.55f + amb * 0.20f)
        val drkC = lerp(Color(0xFF142E14), Color(0xFF244C22), lf * 0.30f)
        val pe = ang + PI / 2.0
        val sp = Path().apply {
            moveTo(lx + (cos(pe) * sW * 0.5f).toFloat(), ly + (sin(pe) * sW * 0.5f).toFloat())
            cubicTo(lx + (cos(pe) * sW).toFloat() + (cos(ang) * sLen * 0.55f).toFloat(), ly + (sin(pe) * sW).toFloat() + (sin(ang) * sLen * 0.55f).toFloat(), tx + (cos(pe) * sW * 0.25f).toFloat(), ty + (sin(pe) * sW * 0.25f).toFloat(), tx, ty)
            cubicTo(tx + (cos(pe) * (-sW * 0.25f)).toFloat(), ty + (sin(pe) * (-sW * 0.25f)).toFloat(), lx + (cos(pe) * (-sW)).toFloat() + (cos(ang) * sLen * 0.55f).toFloat(), ly + (sin(pe) * (-sW)).toFloat() + (sin(ang) * sLen * 0.55f).toFloat(), lx + (cos(pe) * (-sW * 0.5f)).toFloat(), ly + (sin(pe) * (-sW * 0.5f)).toFloat()); close()
        }
        drawPath(sp, Brush.linearGradient(listOf(litC, drkC), Offset(lx, ly), Offset(tx, ty)))
    }
    repeat(5) { i -> jasmineOnePetal(cx, cy, p * 0.90f * openF, Math.toRadians(i * 72.0 - 54.0 + sin(phase * 0.09f) * 1.8), lf, amb, 0.55f) }
    repeat(5) { i -> jasmineOnePetal(cx, cy, p * openF, Math.toRadians(i * 72.0 - 78.0 + sin(phase * 0.11f) * 2.2), lf, amb, 1.0f) }
    drawCircle(Color(0xFF2A5230).copy(alpha = 0.95f), p * 0.32f, Offset(cx, cy))
    drawCircle(Brush.radialGradient(listOf(Color(0xFF3A7040), Color(0xFF224228)), Offset(cx - p * 0.06f, cy - p * 0.06f), p * 0.28f), p * 0.28f, Offset(cx, cy))
    repeat(8) { i ->
        val ang = Math.toRadians(i * 45.0 + phase * 18.0); val fi = p * 0.20f
        val fx = cx + (cos(ang) * fi).toFloat(); val fy = cy + (sin(ang) * fi).toFloat()
        drawLine(Color(0xFFC8D840).copy(alpha = 0.68f), Offset(cx, cy), Offset(fx, fy), 0.55f)
        drawCircle(Color(0xFFD8E044).copy(alpha = 0.82f), 1.15f, Offset(fx, fy))
    }
    drawCircle(Color(0xFFFFF8E0).copy(alpha = 0.90f), p * 0.09f, Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.70f), p * 0.05f, Offset(cx - p * 0.04f, cy - p * 0.04f))
}

private fun DrawScope.jasmineOnePetal(cx: Float, cy: Float, p: Float, ang: Double, lf: Float, amb: Float, alpha: Float) {
    val bx = cx + (cos(ang) * p * 0.24f).toFloat(); val by = cy + (sin(ang) * p * 0.24f).toFloat()
    val tx = cx + (cos(ang) * p).toFloat(); val ty = cy + (sin(ang) * p).toFloat()
    val la = ang - 0.34f; val ra = ang + 0.34f
    val pp = Path().apply {
        moveTo(bx, by)
        cubicTo(cx + (cos(la) * p * 0.64f).toFloat(), cy + (sin(la) * p * 0.64f).toFloat(), cx + (cos(la) * p * 0.92f).toFloat(), cy + (sin(la) * p * 0.92f).toFloat(), tx, ty)
        cubicTo(cx + (cos(ra) * p * 0.92f).toFloat(), cy + (sin(ra) * p * 0.92f).toFloat(), cx + (cos(ra) * p * 0.64f).toFloat(), cy + (sin(ra) * p * 0.64f).toFloat(), bx, by); close()
    }
    withTransform({ translate(1.2f, 1.8f) }) { drawPath(pp, Color.Black.copy(alpha = alpha * 0.12f)) }
    val baseC = lerp(Color(0xFFECE6DC), Color(0xFFFFFEFC), lf * 0.45f + amb * 0.08f)
    val tipC = lerp(Color(0xFFF8F6F2), Color(0xFFFFFFFF), lf * 0.55f)
    drawPath(pp, Brush.radialGradient(listOf(tipC.copy(alpha = alpha), lerp(Color(0xFFF4F0E8), Color(0xFFFFFFFF), lf * 0.35f).copy(alpha = alpha), baseC.copy(alpha = alpha * 0.88f)), Offset(tx * 0.52f + cx * 0.48f, ty * 0.52f + cy * 0.48f), p * 0.88f))
    drawPath(pp, Color(0xFFFFF0D0).copy(alpha = alpha * 0.10f * (1f - amb * 0.3f)))
    drawLine(Color(0xFFCCC4B4).copy(alpha = alpha * 0.38f), Offset(bx, by), Offset(tx, ty), 0.45f)
    drawPath(pp, Color(0xFFD8D0C4).copy(alpha = alpha * 0.22f), style = Stroke(0.5f))
}

private fun DrawScope.drawRealisticSunflower(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p = r * (1f + sin(phase) * 0.025f); val lf = litFactor(0.50f, -0.86f, amb)
    drawCircle(Color(0xFF6A4A00).copy(alpha = 0.06f * amb), p * 3.0f, Offset(cx, cy))
    drawCircle(Color(0xFF8A6010).copy(alpha = 0.10f * amb), p * 2.0f, Offset(cx, cy))
    val COUNT = 16
    repeat(COUNT) { i ->
        val ang = Math.toRadians(i * (360.0 / COUNT) + phase * 3.8); val pLen = p * (if (i % 2 == 0) 1.32f else 1.16f)
        val pW = p * 0.29f; val px = cx + (cos(ang) * pLen).toFloat(); val py = cy + (sin(ang) * pLen).toFloat()
        val perp = ang + PI / 2.0; val hw = pW * 0.42f; val base = p * 0.54f
        val bx = cx + (cos(ang) * base).toFloat(); val by = cy + (sin(ang) * base).toFloat()
        val pf = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb)
        val pp2 = Path().apply {
            moveTo(bx + (cos(perp) * hw).toFloat(), by + (sin(perp) * hw).toFloat())
            cubicTo(bx + (cos(perp) * hw * 1.22f).toFloat() + (cos(ang) * pLen * 0.50f).toFloat(), by + (sin(perp) * hw * 1.22f).toFloat() + (sin(ang) * pLen * 0.50f).toFloat(), px + (cos(perp) * hw * 0.58f).toFloat(), py + (sin(perp) * hw * 0.58f).toFloat(), px, py)
            cubicTo(px + (cos(perp) * (-hw * 0.58f)).toFloat(), py + (sin(perp) * (-hw * 0.58f)).toFloat(), bx + (cos(perp) * (-hw * 1.22f)).toFloat() + (cos(ang) * pLen * 0.50f).toFloat(), by + (sin(perp) * (-hw * 1.22f)).toFloat() + (sin(ang) * pLen * 0.50f).toFloat(), bx + (cos(perp) * (-hw)).toFloat(), by + (sin(perp) * (-hw)).toFloat()); close()
        }
        withTransform({ translate(1.3f, 2.0f) }) { drawPath(pp2, Color.Black.copy(alpha = 0.13f)) }
        drawPath(pp2, Brush.linearGradient(listOf(lerp(Color(0xFF7A4E00), Color(0xFFA06C08), pf * 0.70f), lerp(Color(0xFFA87010), Color(0xFFCC9418), pf * 0.75f), lerp(Color(0xFFCC9818), Color(0xFFECBC38), pf * 0.80f)), Offset(cx, cy), Offset(px, py)))
        if (amb > 0.4f) drawPath(pp2, Color(0xFFFFE87A).copy(alpha = pf * 0.14f * amb))
    }
    val discR = p * 0.70f
    repeat(96) { k ->
        val theta = Math.toRadians(k * 137.50776); val rho = discR * sqrt(k.toFloat() / 96)
        val sx = cx + (cos(theta) * rho).toFloat(); val sy = cy + (sin(theta) * rho).toFloat()
        val sf = litFactor((sx - cx) / discR.coerceAtLeast(1f), (sy - cy) / discR.coerceAtLeast(1f), amb)
        val kF = k.toFloat() / 96
        drawCircle(lerp(lerp(Color(0xFF1A0802), Color(0xFF2E1208), sf), lerp(Color(0xFF3A1C06), Color(0xFF4E2A0A), sf), kF), 1.55f + (1f - kF) * 0.95f, Offset(sx, sy))
    }
    drawCircle(Color(0xFF160600).copy(alpha = 0.60f), discR, Offset(cx, cy), style = Stroke(discR * 0.12f))
    if (amb > 0.30f) repeat(12) { i ->
        val ang = Math.toRadians(i * 30.0 + phase * 22.0); val pr = discR * (0.30f + hash(i.toFloat(), 44f) * 0.42f)
        drawCircle(Color(0xFFD4A820).copy(alpha = amb * 0.28f), 1.1f, Offset(cx + (cos(ang) * pr).toFloat(), cy + (sin(ang) * pr).toFloat()))
    }
}

private fun DrawScope.drawRealisticRose(cx: Float, cy: Float, r: Float, phase: Float, amb: Float) {
    val p = r * (1f + sin(phase) * 0.055f); val lf = litFactor(-0.30f, -0.95f, amb)
    val warmth = ((0.6f - abs(amb - 0.4f)) / 0.6f).coerceIn(0f, 1f)
    drawCircle(Color(0xFF5A000C).copy(alpha = 0.05f + warmth * 0.04f), p * 3.0f, Offset(cx, cy))
    drawCircle(Color(0xFF780010).copy(alpha = 0.08f + warmth * 0.06f), p * 2.0f, Offset(cx, cy))
    drawCircle(Color.Black.copy(alpha = 0.20f), p * 1.18f, Offset(cx + p * 0.05f, cy + p * 0.60f))
    data class RR(val n: Int, val rho: Float, val sz: Float, val rot: Float, val depth: Float)
    listOf(RR(7, .95f, .62f, 0f, .00f), RR(6, .72f, .52f, 13f, .14f), RR(5, .51f, .44f, 30f, .28f), RR(4, .33f, .36f, 48f, .44f), RR(3, .16f, .26f, 66f, .60f)).forEach { ring ->
        repeat(ring.n) { i ->
            val ang = Math.toRadians(i * (360.0 / ring.n) + ring.rot + phase * (2.5 / ring.n))
            val pf = litFactor(cos(ang).toFloat(), sin(ang).toFloat(), amb) * (1f - ring.depth * 0.52f)
            val hw = p * ring.sz * 0.46f
            val bx = cx + (cos(ang) * p * ring.rho * 0.34f).toFloat(); val by = cy + (sin(ang) * p * ring.rho * 0.34f).toFloat()
            val px2 = cx + (cos(ang) * p * ring.rho).toFloat(); val py2 = cy + (sin(ang) * p * ring.rho).toFloat()
            val pe = ang + PI / 2.0
            val pp = Path().apply {
                moveTo(bx + (cos(pe) * hw * 0.50f).toFloat(), by + (sin(pe) * hw * 0.50f).toFloat())
                cubicTo(bx + (cos(pe) * hw).toFloat() + (cos(ang) * p * ring.sz * 0.56f).toFloat(), by + (sin(pe) * hw).toFloat() + (sin(ang) * p * ring.sz * 0.56f).toFloat(), px2 + (cos(pe) * hw * 0.80f).toFloat(), py2 + (sin(pe) * hw * 0.80f).toFloat(), px2 + (cos(pe) * hw * 0.24f).toFloat(), py2 + (sin(pe) * hw * 0.24f).toFloat())
                cubicTo(px2, py2, px2 + (cos(pe) * (-hw * 0.24f)).toFloat(), py2 + (sin(pe) * (-hw * 0.24f)).toFloat(), px2 + (cos(pe) * (-hw * 0.80f)).toFloat(), py2 + (sin(pe) * (-hw * 0.80f)).toFloat())
                cubicTo(bx + (cos(pe) * (-hw)).toFloat() + (cos(ang) * p * ring.sz * 0.56f).toFloat(), by + (sin(pe) * (-hw)).toFloat() + (sin(ang) * p * ring.sz * 0.56f).toFloat(), bx + (cos(pe) * (-hw * 0.50f)).toFloat(), by + (sin(pe) * (-hw * 0.50f)).toFloat(), bx + (cos(pe) * hw * 0.50f).toFloat(), by + (sin(pe) * hw * 0.50f).toFloat()); close()
            }
            withTransform({ translate(1.5f, 2.1f) }) { drawPath(pp, Color.Black.copy(alpha = 0.18f)) }
            val shdC = lerp(Color(0xFF160006), Color(0xFF240008), ring.depth)
            drawPath(pp, Brush.radialGradient(listOf(shdC, lerp(Color(0xFF6A0A18), Color(0xFF8C1224), pf), lerp(Color(0xFF8C1224), Color(0xFFAC1C34), pf), lerp(Color(0xFFAC1C34), Color(0xFFCC2A48), pf * 0.65f)), Offset(px2 * 0.42f + cx * 0.58f, py2 * 0.42f + cy * 0.58f), p * ring.sz * 1.1f))
            drawPath(pp, Color(0xFFCC2840).copy(alpha = pf * 0.10f * (0.5f + amb * 0.5f)))
        }
    }
    drawCircle(Color(0xFF420010).copy(alpha = 0.96f), p * 0.18f, Offset(cx, cy))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFB80C28), Color(0xFF7A0618), Color(0xFF2E0008)), Offset(cx - p * 0.04f, cy - p * 0.04f), p * 0.16f), p * 0.16f, Offset(cx, cy))
    listOf(0.54f to -0.20f, -0.46f to 0.30f, 0.18f to 0.58f).forEach { (ox, oy) ->
        val dx = cx + ox * p; val dy = cy + oy * p
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.60f), Color(0xFFFFD5DC).copy(alpha = 0.32f), Color.Transparent), Offset(dx - 1.0f, dy - 1.0f), 3.5f), 2.8f, Offset(dx, dy))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  BIRDS / INDICATORS / AURA / ARC / PARTICLES
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawBirds(W: Float, H: Float, phase: Float, sky: SkyPalette) {
    val birdAlpha = when { sky.ambient < 0.25f -> 0f; sky.ambient > 0.80f -> (1f - sky.ambient) / 0.20f * 0.55f; else -> (sky.ambient - 0.25f) / 0.35f * 0.55f }.coerceIn(0f, 0.55f)
    if (birdAlpha < 0.05f) return
    val bx = (phase * 1.4f % 1.2f - 0.1f) * W; val by = H * 0.18f + sin(phase * 2f * PI.toFloat()) * H * 0.04f
    val bc = Color.Black.copy(alpha = birdAlpha); val ws = 10f
    listOf(0f to 0f, -22f to -8f, -44f to -4f, 22f to -8f, 44f to -4f).forEach { (ox, oy) ->
        val fx = bx + ox; val fy = by + oy; val flap = sin(phase * 2f * PI.toFloat() * 3f + ox * 0.1f)
        drawLine(bc, Offset(fx, fy), Offset(fx - ws, fy - flap * ws * 0.38f), 1.2f, cap = StrokeCap.Round)
        drawLine(bc, Offset(fx, fy), Offset(fx + ws, fy - flap * ws * 0.38f), 1.2f, cap = StrokeCap.Round)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GROWTH INDICATOR — ✅ عمودي على يسار الـ plot، لا يتداخل مع النبتة
//  شريط رفيع طولي على الحافة اليسرى، يمتلئ من الأسفل للأعلى
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawGrowthIndicator(pr: PlotRect, progress: Float, t: PlantType, sky: SkyPalette) {
    val barW  = 3.5f                          // عرض الشريط
    val barH  = pr.height * 0.78f             // طول الشريط (78% من ارتفاع الـ plot)
    val barX  = pr.left + 3.5f               // على الحافة اليسرى مع مسافة صغيرة
    val barBotY = pr.top + pr.height - 4f     // قاع الشريط
    val barTopY = barBotY - barH              // قمة الشريط

    // خلفية الشريط — track
    drawRoundRect(
        color = lerp(Color(0xFF1A1810), Color(0xFF2C2820), sky.ambient * 0.5f).copy(alpha = 0.50f),
        topLeft = Offset(barX, barTopY),
        size = Size(barW, barH),
        cornerRadius = CornerRadius(barW / 2f)
    )

    // الجزء المملوء — يمتلئ من الأسفل للأعلى
    if (progress > 0f) {
        val filledH = barH * progress
        val filledTopY = barBotY - filledH

        // لون الشريط يأخذ لون النبتة
        val fillColor = t.petalColor.copy(alpha = 0.75f)
        val fillColorDim = t.petalColor.copy(alpha = 0.30f)

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, fillColorDim),
                startY = filledTopY,
                endY = barBotY
            ),
            topLeft = Offset(barX, filledTopY),
            size = Size(barW, filledH),
            cornerRadius = CornerRadius(barW / 2f)
        )

        // نقطة مضيئة في القمة
        drawCircle(
            color = t.petalColor.copy(alpha = 0.90f),
            radius = barW * 0.75f,
            center = Offset(barX + barW / 2f, filledTopY)
        )
    }
}

private fun DrawScope.drawBloomAura(pr: PlotRect, t: PlantType, phase: Float, sky: SkyPalette) {
    val cx = pr.cx; val cy = pr.cy; val p = sin(phase) * 0.5f + 0.5f; val ar = pr.width * 0.50f + p * 5f
    val glowCol = lerp(t.glowColor, Color.White, 0.40f)

    // ✅ clip صارم: الـ aura محبوسة في حدود الـ plot — لا تطغى على الـ plots المجاورة
    // نوسّع قليلاً للأعلى عشان النبتة بتطلع فوق الـ plot، لكن لا نتعدى عرض الـ plot أفقياً
    clipRect(
        left  = pr.left,
        top   = pr.top - pr.height * 1.2f,   // يسمح للـ glow يصعد مع النبتة
        right = pr.left + pr.width,
        bottom = pr.top + pr.height
    ) {
        drawCircle(glowCol.copy(alpha = 0.04f + p * 0.04f), ar * 2.2f, Offset(cx, cy))
        drawCircle(glowCol.copy(alpha = 0.08f + p * 0.06f), ar * 1.4f, Offset(cx, cy))
        drawCircle(Color.White.copy(alpha = 0.14f + p * 0.12f), ar, Offset(cx, cy), style = Stroke(1.5f))
    }
}

private fun DrawScope.drawWirdArc(W: Float, H: Float, wird: WirdProgress, sky: SkyPalette) {
    val cx = W * 0.50f; val cy = H * 0.11f; val arcR = W * 0.095f
    val bgCol = lerp(Color(0xFF0A0E08), Color(0xFF141E10), sky.ambient * 0.65f)
    drawCircle(bgCol.copy(alpha = 0.82f), arcR * 1.42f, Offset(cx, cy))
    drawCircle(lerp(Color(0xFF1A1C18), Color(0xFF2A2E26), sky.ambient * 0.5f).copy(alpha = 0.62f), arcR, Offset(cx, cy), style = Stroke(6.5f))
    fun arc(prog: Float, start: Float, col: Color) {
        if (prog <= 0f) return
        drawArc(Brush.sweepGradient(listOf(col.copy(alpha = 0f), col, col.copy(alpha = 0f)), Offset(cx, cy)), start, prog * 120f, false, Offset(cx - arcR, cy - arcR), Size(arcR * 2f, arcR * 2f), style = Stroke(6.5f, cap = StrokeCap.Round))
    }
    arc(wird.subProg, -90f, Color(0xFF3A7030))
    arc(wird.hamdProg, 30f, Color(0xFFB06C10))
    arc(wird.akbarProg, 150f, Color(0xFF882040))
    drawCircle(bgCol.copy(alpha = 0.95f), arcR * 0.64f, Offset(cx, cy))
}

private fun DrawScope.drawParticle(p: FloatingParticle) {
    val a = p.alpha.coerceIn(0f, 1f); if (a <= 0f) return
    val px = p.x; val py = p.y
    when (p.type) {
        PType.PETAL -> {
            withTransform({ rotate(p.rot, Offset(px, py)) }) {
                drawOval(Color.Black.copy(alpha = a * 0.14f), Offset(px - p.size + 1.2f, py - p.size * 0.38f + 1.2f), Size(p.size * 2f, p.size * 0.76f))
                drawOval(Brush.linearGradient(listOf(lerp(p.color, Color.White, 0.22f), p.color, lerp(p.color, Color.Black, 0.26f)), Offset(px - p.size, py - p.size * 0.38f), Offset(px + p.size, py + p.size * 0.38f)), Offset(px - p.size, py - p.size * 0.38f), Size(p.size * 2f, p.size * 0.76f))
                drawLine(lerp(p.color, Color.Black, 0.36f).copy(alpha = a * 0.45f), Offset(px - p.size * 0.62f, py), Offset(px + p.size * 0.62f, py), 0.4f)
            }
        }
        PType.STAR, PType.SPARKLE -> {
            drawCircle(p.color.copy(alpha = a * 0.07f), p.size * 3.2f, Offset(px, py))
            drawCircle(p.color.copy(alpha = a * 0.22f), p.size * 1.6f, Offset(px, py))
            drawCircle(p.color.copy(alpha = a), p.size * 0.55f, Offset(px, py))
            repeat(4) { i ->
                val ang = Math.toRadians(i * 90.0 + p.rot.toDouble())
                drawLine(p.color.copy(alpha = a * 0.45f), Offset(px + (cos(ang) * p.size * 0.55f).toFloat(), py + (sin(ang) * p.size * 0.55f).toFloat()), Offset(px + (cos(ang) * p.size * 1.7f).toFloat(), py + (sin(ang) * p.size * 1.7f).toFloat()), 0.6f, cap = StrokeCap.Round)
            }
        }
        PType.LIGHT -> { drawCircle(p.color.copy(alpha = a * 0.06f), p.size * 4.2f, Offset(px, py)); drawCircle(p.color.copy(alpha = a * 0.18f), p.size * 1.8f, Offset(px, py)); drawCircle(p.color.copy(alpha = a), p.size * 0.44f, Offset(px, py)) }
        PType.SEED -> {
            withTransform({ rotate(p.rot, Offset(px, py)) }) {
                drawOval(Color.Black.copy(alpha = a * 0.14f), Offset(px - p.size * 0.56f + 0.9f, py - p.size * 0.36f + 0.9f), Size(p.size * 1.12f, p.size * 0.72f))
                drawOval(Brush.radialGradient(listOf(lerp(p.color, Color.White, 0.14f), p.color), Offset(px, py), p.size * 0.85f), Offset(px - p.size * 0.56f, py - p.size * 0.36f), Size(p.size * 1.12f, p.size * 0.72f))
            }
        }
        PType.ARABIC -> { drawCircle(p.color.copy(alpha = a * 0.10f), p.size * 2.8f, Offset(px, py)); drawCircle(p.color.copy(alpha = a), p.size * 0.70f, Offset(px, py)) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  CONTROL PANEL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun DhikrControlPanel(
    wird: WirdProgress, combo: Int, sessionAnwar: Int,
    sky: SkyPalette, onDhikr: (String) -> Unit, modifier: Modifier = Modifier
) {
    val panelBg = lerp(Color(0xFF06100A), Color(0xFF0C1A0E), sky.ambient * 0.70f)

    Surface(modifier = modifier, color = panelBg, tonalElevation = 4.dp) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {

            // ── معلومات الجلسة ──────────────────────────────────────────────
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("ورد: ${wird.completedAwrad}",
                    color = lerp(Color(0xFF6A9860), Color(0xFF2A5820), sky.ambient * 0.7f),
                    style = MaterialTheme.typography.labelSmall)
                if (combo > 1) {
                    Surface(shape = RoundedCornerShape(50.dp), color = Color(0xFF7A2800).copy(alpha = 0.90f)) {
                        Text("×$combo", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color(0xFFFFF0CC), fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text("$sessionAnwar نور", color = Color(0xFFF0D060).copy(alpha = 0.88f),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            // ── الصف الأول: 4 أزرار ─────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(5.dp)) {
                DHIKR_BUTTONS.take(4).forEach { btn ->
                    DhikrBtn(btn, wird, { onDhikr(btn.id) }, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(5.dp))

            // ── الصف الثاني: 4 أزرار ────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(5.dp)) {
                DHIKR_BUTTONS.takeLast(4).forEach { btn ->
                    DhikrBtn(btn, wird, { onDhikr(btn.id) }, Modifier.weight(1f))
                }
            }

            // ── أشرطة التقدم ─────────────────────────────────────────────────
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(3.dp)) {
                DHIKR_BUTTONS.forEach { btn ->
                    val prog by animateFloatAsState(wird.progressFor(btn.id).coerceIn(0f, 1f), tween(480), label = "p_${btn.id}")
                    Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(btn.arcColor.copy(alpha = 0.15f))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(prog).clip(RoundedCornerShape(2.dp)).background(btn.arcColor.copy(alpha = if (prog >= 1f) 1f else 0.65f)))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  زر ذكر فردي — ✅ نص كامل مع ارتفاع ديناميكي
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DhikrBtn(btn: DhikrButton, wird: WirdProgress, onTap: () -> Unit, modifier: Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (pressed) 0.90f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "sc_${btn.id}")
    val scope = rememberCoroutineScope()
    val current = wird.counts[btn.id] ?: 0
    val done = current >= btn.target
    val prog by animateFloatAsState(wird.progressFor(btn.id).coerceIn(0f, 1f), tween(350), label = "bp_${btn.id}")

    Box(
        modifier = modifier
            .heightIn(min = 72.dp)   // ✅ حد أدنى بدل ارتفاع ثابت — يكبر إذا احتاج النص مساحة
            .scale(sc)
            .clip(RoundedCornerShape(10.dp))
            .background(if (done) btn.litBg.copy(alpha = 0.40f) else if (pressed) btn.darkBg else btn.litBg)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                }, onTap = { onTap() })
            },
        contentAlignment = Alignment.Center
    ) {
        // شريط تقدم خلفي
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.BottomStart) {
            Box(
                Modifier
                    .fillMaxWidth(prog)
                    .height(3.dp)
                    .background(btn.arcColor.copy(alpha = 0.40f))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 5.dp)
        ) {
            // emoji + count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(btn.plant.emoji, fontSize = 12.sp, lineHeight = 14.sp)
                Spacer(Modifier.width(2.dp))
                Text(
                    if (done) "✓" else "$current/${btn.target}",
                    fontSize = 8.sp,
                    color = if (done) Color(0xFF80E880) else btn.arcColor.copy(alpha = 0.90f),
                    fontWeight = if (done) FontWeight.ExtraBold else FontWeight.Normal
                )
            }
            Spacer(Modifier.height(2.dp))
            // ✅ اسم الذكر — كامل دائماً، بدون قطع
            Text(
                text = btn.arabic,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (done) Color(0xFF90E890) else Color(0xFFCCDCC8),
                textAlign = TextAlign.Center,
                softWrap = true,
                modifier = Modifier.fillMaxWidth()
                // ✅ بدون maxLines — النص يظهر كامل مهما كان
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  WIRD CELEBRATION
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun WirdCelebrationOverlay(awradTotal: Int, sky: SkyPalette) {
    val bgAlpha = lerp(0.78f, 0.88f, 1f - sky.ambient)
    Box(Modifier.fillMaxSize().background(Color(0xFF020504).copy(alpha = bgAlpha)), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(26.dp), color = lerp(Color(0xFF060E06), Color(0xFF0C180C), sky.ambient * 0.6f), tonalElevation = 20.dp, modifier = Modifier.fillMaxWidth(0.90f).wrapContentHeight()) {
            Column(Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📿", fontSize = 50.sp)
                Text("ورد مكتمل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6A9E60))
                Text("اللهم تقبل منا — الورد رقم $awradTotal", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFBCCCB8).copy(alpha = 0.92f), textAlign = TextAlign.Center)
                Text("«لا يزال لسانك رطباً من ذكر الله»", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE8C84A), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1A4A18).copy(alpha = 0.45f)) {
                    Text("+${200 + awradTotal * 75} نور  ·  مكافأة الورد", Modifier.padding(horizontal = 20.dp, vertical = 10.dp), fontWeight = FontWeight.Bold, color = Color(0xFFF0D060), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}