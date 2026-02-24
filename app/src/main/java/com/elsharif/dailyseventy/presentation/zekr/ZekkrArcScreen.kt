package com.elsharif.dailyseventy.presentation.zekr

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.domain.data.model.Zakker
import com.elsharif.dailyseventy.presentation.azkarcategories.getCategoryResId
import com.elsharif.dailyseventy.presentation.components.ShareTextIcon
import com.elsharif.dailyseventy.ui.theme.Black
import com.elsharif.dailyseventy.util.getAdaptiveGradient
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// كل بيانات كارت زكر معزولة تماماً عن باقي الكروت بـ key = zekr.content
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZekkrArcScreen(
    navController: NavController,
    category: String
) {
    val viewModel: ZekkrViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categoryResId = getCategoryResId(category)
    val bgType = remember(category) { categoryToZekkrBg(category) }

    LaunchedEffect(category) { viewModel.onEvent(ZekkrEvent.SelectCategory(category)) }

    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    // ✅ كل كارت عنده count مستقل — الـ key هو zekr.content
    val countsMap = remember { mutableStateMapOf<String, Int>() }
    // ✅ كل كارت عنده flip مستقل
    val flippedCards = remember { mutableStateMapOf<String, Boolean>() }

    var showConfetti by remember { mutableStateOf(false) }
    val confettiTrigger = remember { mutableStateOf(0) }

    // ✅ نضع قيم البداية مرة واحدة فقط (getOrPut = مش بيعيد التعيين لو موجود)
    LaunchedEffect(state.azkaar) {
        state.azkaar.forEach { zekr ->
            countsMap.getOrPut(zekr.content) { zekr.count.toInt().coerceAtLeast(1) }
        }
    }

    var detailZekr by remember { mutableStateOf<Zakker?>(null) }
    var savedZekrIndex by remember { mutableStateOf(0) }
    var previousCount by remember { mutableStateOf<Int?>(null) }

    if (detailZekr != null) {
        ZekkrDetailScreen(
            zekr = detailZekr!!,
            primaryColor = primary,
            gradient = gradient,
            currentCount = countsMap[detailZekr!!.content] ?: detailZekr!!.count.toInt(),
            onCountChange = { newCount -> countsMap[detailZekr!!.content] = newCount },
            onBack = {
                val finalCount = countsMap[detailZekr!!.content] ?: 0
                if (finalCount == 0 && previousCount != 0) {
                    flippedCards[detailZekr!!.content] = true
                    showConfetti = true
                    confettiTrigger.value++
                }
                detailZekr = null
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(categoryResId),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            if (state.azkaar.isNotEmpty()) {
                ZekkrArcContent(
                    paddingValues = paddingValues,
                    azkaar = state.azkaar,
                    primaryColor = primary,
                    gradient = gradient,
                    bgType = bgType,
                    countsMap = countsMap,
                    flippedCards = flippedCards,
                    showConfetti = showConfetti,
                    confettiTrigger = confettiTrigger.value,
                    onShowConfetti = { show -> showConfetti = show },
                    initialZekrIndex = savedZekrIndex,
                    onCountChange = { zekrContent, newCount ->
                        val prev = countsMap[zekrContent] ?: 1
                        countsMap[zekrContent] = newCount
                        // ✅ بس لو الكارت ده اكتمل — مش بيأثر على التاني
                        if (newCount == 0 && prev > 0) {
                            flippedCards[zekrContent] = true
                            showConfetti = true
                            confettiTrigger.value++
                        }
                    },
                    onShowDetail = { zekr, currentIndex ->
                        previousCount = countsMap[zekr.content] ?: zekr.count.toInt()
                        detailZekr = zekr
                        savedZekrIndex = currentIndex
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ZekkrArcContent(
    paddingValues: PaddingValues,
    azkaar: List<Zakker>,
    primaryColor: Color,
    gradient: Brush,
    bgType: ZekkrBgType,
    countsMap: Map<String, Int>,
    flippedCards: Map<String, Boolean>,
    showConfetti: Boolean,
    confettiTrigger: Int,
    onShowConfetti: (Boolean) -> Unit,
    initialZekrIndex: Int,
    onCountChange: (String, Int) -> Unit,
    onShowDetail: (Zakker, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val angleOffset = remember { Animatable(0f) }
    val cardCount = azkaar.size
    val maxArcAngle = PI.toFloat() * 0.72f
    val stepAngle = if (cardCount > 1) (2f * maxArcAngle) / (cardCount - 1) else 0f

    LaunchedEffect(Unit) {
        val baseAngle = -maxArcAngle
        val targetAngle = baseAngle + initialZekrIndex * stepAngle
        angleOffset.snapTo(-targetAngle)
    }

    val currentAngleValue = angleOffset.value
    val midIndex = (cardCount - 1) / 2f

    val selectedIndex = remember(currentAngleValue, cardCount, stepAngle) {
        azkaar.indices.minByOrNull { index ->
            val angle = (index - midIndex) * stepAngle + currentAngleValue
            abs(angle)
        } ?: 0
    }

    // ✅ rememberUpdatedState عشان الـ lambda دايماً تشوف أحدث values
    val countsMapRef     by rememberUpdatedState(countsMap)
    val selectedIndexRef by rememberUpdatedState(selectedIndex)
    val azkaarRef        by rememberUpdatedState(azkaar)
    val flippedCardsRef  by rememberUpdatedState(flippedCards)
    val angleOffsetRef   by rememberUpdatedState(angleOffset)
    val stepAngleRef     by rememberUpdatedState(stepAngle)
    val maxArcAngleRef   by rememberUpdatedState(maxArcAngle)

    Box(modifier = modifier.fillMaxSize()) {
        ZekkrDynamicBackground(bgType = bgType, modifier = Modifier.matchParentSize())

        if (showConfetti) {
            ConfettiEffect(
                primaryColor = primaryColor,
                trigger = confettiTrigger,
                onFinish = { onShowConfetti(false) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.2f), primaryColor.copy(alpha = 0.05f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.tsbehicon),
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(55.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "يَا أَيُّهَا الَّذِينَ آمَنُوا اذْكُرُوا اللَّهَ ذِكْرًا كَثِيرًا",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = primaryColor
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val frameYOffset = 20.dp

                Text(
                    text = stringResource(R.string.drag_to_select_zekr),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = frameYOffset + 180.dp)
                        .alpha(0.7f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )

                SelectionFrame(primaryColor = primaryColor, modifier = Modifier.offset(y = frameYOffset))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .offset(y = frameYOffset)
                        // ═══════════════════════════════════════════════════════════
                        // ✅ SINGLE pointerInput — بيتحكم في drag + tap + long-press
                        //    مفيش draggable modifier = مفيش conflict على الإطلاق
                        //    كل كارت معزول بـ zekr.content كـ key
                        // ═══════════════════════════════════════════════════════════
                        .pointerInput(Unit) {
                            val dragSensitivity  = 0.0035f  // حساسية السحب
                            val dragSlop         = 10f       // الحد الأدنى للـ drag (px)
                            val longPressDuration = 450L     // مدة الـ long press (ms)

                            awaitEachGesture {
                                // ── 1. انتظر اللمسة الأولى ──────────────────────────────
                                val down    = awaitFirstDown(requireUnconsumed = false)
                                val downX   = down.position.x
                                var prevX   = downX
                                var totalDx = 0f
                                var pointerUp   = false
                                var wasDragging = false

                                // ── 2. تتبع الحركة حتى timeout أو رفع الإصبع ────────────
                                val completedBeforeTimeout = withTimeoutOrNull(longPressDuration) {
                                    while (true) {
                                        val event  = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break

                                        val x  = change.position.x
                                        val dx = x - prevX        // الفرق عن النقطة السابقة
                                        totalDx = abs(x - downX)  // المسافة الكلية عن البداية

                                        // ── drag: حرّك الـ arc مباشرةً ───────────────────
                                        if (totalDx > dragSlop) {
                                            wasDragging = true
                                            change.consume() // نمنع أي modifier تاني من التدخل
                                            scope.launch {
                                                val rawDelta = dx * dragSensitivity
                                                val target   = angleOffsetRef.value + rawDelta
                                                // smoothing خفيف عشان مش تكون jerky
                                                angleOffsetRef.snapTo(
                                                    angleOffsetRef.value + 0.55f * (target - angleOffsetRef.value)
                                                )
                                            }
                                        }

                                        prevX = x

                                        // ── رفع الإصبع ───────────────────────────────────
                                        if (change.changedToUp()) {
                                            if (wasDragging) change.consume()
                                            pointerUp = true
                                            break
                                        }
                                    }
                                    true // انتهى قبل الـ timeout
                                }

                                // ── 3. Snap بعد الـ drag ─────────────────────────────────
                                if (wasDragging) {
                                    scope.launch {
                                        val baseAngle = -maxArcAngleRef
                                        val (_, closestAngle) = azkaarRef.indices
                                            .map { i -> i to (baseAngle + i * stepAngleRef + angleOffsetRef.value) }
                                            .minBy { (_, a) -> abs(a) }
                                        angleOffsetRef.animateTo(
                                            angleOffsetRef.value - closestAngle,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness    = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    return@awaitEachGesture // drag → خلاص، مفيش tap/longpress
                                }

                                // ── 4. جيب بيانات الكارت المختار ─────────────────────────
                                val idx = selectedIndexRef
                                if (idx < 0 || idx >= azkaarRef.size) return@awaitEachGesture
                                val zekr = azkaarRef[idx]

                                // ✅ الكارت المكتمل (مقلوب) → تجاهل تماماً
                                //    لا عداد، لا تفاصيل، ولا تأثير على التاني
                                if (flippedCardsRef[zekr.content] == true) return@awaitEachGesture

                                // ── 5. صنّف الـ gesture ──────────────────────────────────
                                when {
                                    // LONG PRESS: انتهى الـ timeout قبل ما الإصبع يترفع
                                    completedBeforeTimeout == null -> {
                                        onShowDetail(zekr, idx)
                                    }
                                    // TAP: الإصبع اترفع قبل الـ timeout
                                    pointerUp -> {
                                        val currentCount = countsMapRef[zekr.content]
                                            ?: zekr.count.toInt()
                                        if (currentCount > 0) {
                                            // ✅ بيعدّ الكارت ده بس — معزول تماماً
                                            onCountChange(zekr.content, currentCount - 1)
                                        }
                                    }
                                    // غير كده → ignore
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val sortedIndices = azkaar.indices.sortedBy { index ->
                        val angle = (index - midIndex) * stepAngle + currentAngleValue
                        -abs(angle)
                    }

                    sortedIndices.forEach { index ->
                        val angle      = (index - midIndex) * stepAngle + currentAngleValue
                        val isSelected = index == selectedIndex
                        val t          = (angle / maxArcAngle).coerceIn(-1f, 1f)
                        val rawDepth   = (1f - abs(t)).coerceIn(0f, 1f)
                        if (rawDepth < 0.05f) return@forEach

                        val cardWidthPx         = with(density) { 180.dp.toPx() }
                        val maxHorizontalOffset  = cardWidthPx * (cardCount - 1) / 2f * 1.3f
                        val arcDepthPx           = with(density) { 280.dp.toPx() }

                        val x         = maxHorizontalOffset * t
                        val y         = -arcDepthPx * t * t
                        val scale     = lerp(0.7f, 1f, rawDepth) * if (isSelected) 1.05f else 1f
                        val alpha     = ((rawDepth - 0.05f) / 0.95f).coerceIn(0f, 1f)
                        val rotationZ = t * -45f
                        val zekr      = azkaar[index]

                        // ✅ key(zekr.content) = الحل الجذري لعزل كل كارت
                        // بدونه Compose بيربط الـ remember بالموقع في اللوب مش بهوية الكارت
                        // لما الكروت بتترتب أثناء الدراج كانت بتتبادل state مع بعضها
                        key(zekr.content) {
                            ZekkrCard(
                                paddingValues = paddingValues,
                                zekr          = zekr,
                                isSelected    = isSelected,
                                isFlipped     = flippedCards[zekr.content] ?: false,
                                primaryColor  = primaryColor,
                                currentCount  = countsMap[zekr.content] ?: zekr.count.toInt(),
                                onShowDetail  = { onShowDetail(zekr, index) },
                                modifier      = Modifier.graphicsLayer {
                                    translationX   = x
                                    translationY   = y
                                    scaleX         = scale
                                    scaleY         = scale
                                    this.alpha     = alpha
                                    this.rotationZ = rotationZ
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.tap_to_count_long_press_details),
                style = MaterialTheme.typography.bodySmall,
                color = Black.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZekkrCard — عرض فقط، مفيش gestures خالص
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ZekkrCard(
    paddingValues: PaddingValues,
    zekr: Zakker,
    isSelected: Boolean,
    isFlipped: Boolean,
    primaryColor: Color,
    currentCount: Int,
    onShowDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth  = 180.dp
    val cardHeight = 240.dp
    val flipRotation = remember { Animatable(if (isFlipped) 180f else 0f) }
    val density = LocalDensity.current

    LaunchedEffect(isFlipped) {
        if (isFlipped && flipRotation.value < 180f) {
            flipRotation.animateTo(
                180f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
    }

    val progress       = (zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat()
    val hasStarted     = currentCount < zekr.count.toInt().coerceAtLeast(1)
    val isComplete     = currentCount == 0 && hasStarted
    val shouldTruncate = zekr.content.length > 60
    val isShowingBack  = flipRotation.value > 90f

    Surface(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer {
                rotationY      = flipRotation.value
                cameraDistance = 12f * density.density
            },
        shape           = RoundedCornerShape(24.dp),
        tonalElevation  = if (isSelected) 12.dp else 6.dp,
        color           = Color.Transparent,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isShowingBack)
                            listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                        else
                            listOf(Color.White, Color(0xFFFAFAFA))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = primaryColor.copy(alpha = if (isSelected) 0.3f else 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = if (isShowingBack) 180f else 0f }
            ) {
                // ── الوجه الخلفي (بعد الاكتمال) ──────────────────────────────
                if (isShowingBack) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(120.dp).align(Alignment.TopEnd)
                                .offset(x = 30.dp, y = (-30).dp)
                                .background(
                                    Brush.radialGradient(listOf(Color.White.copy(0.3f), Color.Transparent)),
                                    RoundedCornerShape(60.dp)
                                )
                        )
                        Box(
                            modifier = Modifier.size(100.dp).align(Alignment.BottomStart)
                                .offset(x = (-30).dp, y = 30.dp)
                                .background(
                                    Brush.radialGradient(listOf(Color.White.copy(0.3f), Color.Transparent)),
                                    RoundedCornerShape(50.dp)
                                )
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp)
                                    .background(Color.White, RoundedCornerShape(28.dp))
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(painterResource(R.drawable.tsbehicon), null, tint = primaryColor, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.well_done), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.zikr_shield), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, lineHeight = 20.sp), color = Color.White.copy(0.95f), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.keep_it_up), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.9f), textAlign = TextAlign.Center)
                        }
                    }
                }

                // ── الوجه الأمامي ────────────────────────────────────────────
                if (!isShowingBack) {
                    Image(painterResource(R.drawable.rightcorner), null, modifier = Modifier.size(40.dp).align(Alignment.TopStart))
                    Image(painterResource(R.drawable.leftcorner),  null, modifier = Modifier.size(40.dp).align(Alignment.TopEnd))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(top = if (isComplete) 8.dp else 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (shouldTruncate) "${zekr.content.take(60)}..." else zekr.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
                                    color = Black,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (shouldTruncate) {
                                    Spacer(Modifier.height(4.dp))
                                    Surface(
                                        onClick = { onShowDetail() },
                                        shape = RoundedCornerShape(20.dp),
                                        color = primaryColor.copy(alpha = 0.9f),
                                        shadowElevation = 4.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(stringResource(R.string.show_more), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(painterResource(R.drawable.tsbehicon), null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(60.dp),
                                    color = primaryColor,
                                    strokeWidth = 5.dp,
                                    trackColor = Color(0xFFE0E0E0)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$currentCount", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = primaryColor)
                                    Text(stringResource(R.string.remaining), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Black.copy(0.5f))
                                }
                            }
                            Box(
                                modifier = Modifier.size(32.dp).background(primaryColor.copy(0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                ShareTextIcon(text = "${zekr.content}\n\n${zekr.description}")
                            }
                        }
                    }

                    if (isComplete) {
                        Box(modifier = Modifier.padding(top = 4.dp).align(Alignment.TopCenter).offset(y = (-12).dp)) {
                            Surface(shape = RoundedCornerShape(16.dp), color = primaryColor, shadowElevation = 4.dp) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.completed), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZekkrDetailScreen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZekkrDetailScreen(
    zekr: Zakker,
    primaryColor: Color,
    gradient: Brush,
    currentCount: Int,
    onCountChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val progress   = (zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat()
    val isComplete = currentCount == 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.zekr_details), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(gradient).padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(32.dp), color = Color.White, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Box {
                        Box(
                            modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 60.dp, y = (-60).dp)
                                .background(Brush.radialGradient(listOf(primaryColor.copy(0.1f), Color.Transparent)), RoundedCornerShape(60.dp))
                        )
                        if (isComplete) {
                            Surface(
                                shape = RoundedCornerShape(16.dp), color = primaryColor, shadowElevation = 4.dp,
                                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-12).dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.completed), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp).padding(top = if (isComplete) 8.dp else 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(zekr.content, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 36.sp), color = Black, textAlign = TextAlign.Center)
                            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(110.dp), color = primaryColor, strokeWidth = 8.dp, trackColor = Color(0xFFE0E0E0))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$currentCount", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                                    Text(stringResource(R.string.remaining), style = MaterialTheme.typography.bodySmall, color = Black.copy(0.5f))
                                }
                            }
                            Button(
                                onClick = { if (currentCount > 0) onCountChange(currentCount - 1) },
                                enabled = currentCount > 0,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, disabledContainerColor = Color.Gray.copy(0.3f))
                            ) {
                                Text(
                                    text = if (currentCount == 0) stringResource(R.string.count_completed) else stringResource(R.string.tap_to_count),
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (zekr.description.isNotBlank()) {
                    Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(painterResource(R.drawable.tsbehicon), null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                }
                                Text(stringResource(R.string.description), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            Text(zekr.description, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp), color = Black.copy(0.8f))
                        }
                    }
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, primaryColor)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        ShareTextIcon(text = "${zekr.content}\n\n${zekr.description}")
                        Text(stringResource(R.string.share_zekr), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SelectionFrame(primaryColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 194.dp, height = 256.dp)) {
        Box(modifier = Modifier.matchParentSize().border(6.dp, primaryColor.copy(0.2f), RoundedCornerShape(32.dp)))
        Box(modifier = Modifier.matchParentSize().border(2.dp, primaryColor, RoundedCornerShape(28.dp)))
    }
}

@Composable
private fun ConfettiEffect(primaryColor: Color, trigger: Int, onFinish: () -> Unit) {
    val confettiColors = listOf(primaryColor, Color(0xFF10b981), Color(0xFF3b82f6), Color(0xFFf59e0b), Color(0xFFef4444), Color(0xFF8b5cf6))
    val confettiPieces = remember(trigger) {
        List(60) { index ->
            val corner = index % 4
            ConfettiPiece(
                id = index,
                startX = if (corner == 0 || corner == 2) -400f else 400f,
                startY = if (corner == 0 || corner == 1) -400f else 400f,
                endX = (Math.random() * 120 - 60).toFloat(),
                endY = (Math.random() * 120 - 60).toFloat(),
                startDelay = (Math.random() * 300).toLong(),
                duration = (1500 + Math.random() * 1000).toLong(),
                color = confettiColors.random()
            )
        }
    }
    LaunchedEffect(trigger) { kotlinx.coroutines.delay(3000); onFinish() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        confettiPieces.forEach { ConfettiParticle(it) }
    }
}

data class ConfettiPiece(val id: Int, val startX: Float, val startY: Float, val endX: Float, val endY: Float, val startDelay: Long, val duration: Long, val color: Color)

@Composable
private fun ConfettiParticle(piece: ConfettiPiece) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti${piece.id}")
    val progress by infiniteTransition.animateFloat(
        0f, 1f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(piece.duration.toInt(), easing = androidx.compose.animation.core.FastOutSlowInEasing, delayMillis = piece.startDelay.toInt()),
            RepeatMode.Restart
        ), label = "p${piece.id}"
    )
    val rotation by infiniteTransition.animateFloat(
        0f, 720f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(piece.duration.toInt(), easing = androidx.compose.animation.core.LinearEasing),
            RepeatMode.Restart
        ), label = "r${piece.id}"
    )
    val offsetX = if (progress < 0.4f) lerp(piece.startX, piece.endX, progress / 0.4f)
    else piece.endX + (sin((progress - 0.4f) * 12) * 30).toFloat()
    val offsetY = if (progress < 0.4f) lerp(piece.startY, piece.endY, progress / 0.4f)
    else piece.endY + (progress - 0.4f).let { it * it * 1500f }
    val alpha   = if (progress > 0.75f) 1f - (progress - 0.75f) / 0.25f else 1f
    Box(
        modifier = Modifier
            .offset(offsetX.dp, offsetY.dp)
            .size(14.dp)
            .graphicsLayer { rotationZ = rotation; this.alpha = alpha }
            .background(piece.color, RoundedCornerShape(3.dp))
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction