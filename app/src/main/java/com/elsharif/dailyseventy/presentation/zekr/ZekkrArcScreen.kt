package com.elsharif.dailyseventy.presentation.zekr

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZekkrArcScreen(
    navController: NavController,
    category: String
) {
    val viewModel: ZekkrViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val categoryResId = getCategoryResId(category)

    LaunchedEffect(category) {
        viewModel.onEvent(ZekkrEvent.SelectCategory(category))
    }

    val primary = MaterialTheme.colorScheme.primary
    val gradient = getAdaptiveGradient(primary)

    val countsMap = remember { mutableStateMapOf<String, Int>() }
    val flippedCards = remember { mutableStateMapOf<String, Boolean>() }
    var showConfetti by remember { mutableStateOf(false) }
    val confettiTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(state.azkaar) {
        state.azkaar.forEach { zekr ->
            val key = zekr.content
            if (!countsMap.containsKey(key)) {
                countsMap[key] = zekr.count.toInt()
            }
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
            onCountChange = { newCount ->
                countsMap[detailZekr!!.content] = newCount
            },
            onBack = {
                val finalCount = countsMap[detailZekr!!.content] ?: 0
                // لو خلصنا الذكر في التفاصيل (وصلنا للصفر)
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            if (state.azkaar.isNotEmpty()) {
                ZekkrArcContent(
                    paddingValues=paddingValues,
                    azkaar = state.azkaar,
                    primaryColor = primary,
                    gradient = gradient,
                    countsMap = countsMap,
                    flippedCards = flippedCards,
                    showConfetti = showConfetti,
                    confettiTrigger = confettiTrigger.value,
                    onShowConfetti = { show -> showConfetti = show },
                    initialZekrIndex = savedZekrIndex,
                    onCountChange = { zekrContent, newCount ->
                        countsMap[zekrContent] = newCount
                        if (newCount == 0) {
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
    val dragSensitivity = 0.003f
    val maxArcAngle = PI.toFloat() * 0.72f
    val stepAngle = if (cardCount > 1) {
        (2f * maxArcAngle) / (cardCount - 1)
    } else {
        0f
    }

    LaunchedEffect(Unit) {
        val baseAngle = -maxArcAngle
        val targetAngle = baseAngle + initialZekrIndex * stepAngle
        angleOffset.snapTo(-targetAngle)
    }

    var selectedZekrContent by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        // Confetti effect
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
                            colors = listOf(
                                primaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.05f)
                            )
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
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = Black
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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

                SelectionFrame(
                    primaryColor = primaryColor,
                    modifier = Modifier.offset(y = frameYOffset)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .offset(y = frameYOffset)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { deltaX ->
                                scope.launch {
                                    val rawDelta = deltaX * dragSensitivity
                                    val target = angleOffset.value + rawDelta
                                    val alpha = 0.35f
                                    val filtered =
                                        angleOffset.value + alpha * (target - angleOffset.value)
                                    angleOffset.snapTo(filtered)
                                }
                            },
                            onDragStopped = {
                                scope.launch {
                                    val baseAngle = -maxArcAngle
                                    val angles = azkaar.indices.map { index ->
                                        val angle =
                                            baseAngle + index * stepAngle + angleOffset.value
                                        index to angle
                                    }

                                    val (closestIndex, closestAngle) =
                                        angles.minBy { (_, angle) -> abs(angle) }

                                    val targetOffset = angleOffset.value - closestAngle

                                    angleOffset.animateTo(
                                        targetOffset,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )

                                    selectedZekrContent = azkaar[closestIndex].content
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val midIndex = (cardCount - 1) / 2f

                    val cardAngles = azkaar.indices.map { index ->
                        val angle = (index - midIndex) * stepAngle + angleOffset.value
                        index to angle
                    }

                    val selectedIndex = cardAngles
                        .minBy { (_, angle) -> abs(angle) }
                        .first

                    cardAngles.forEach { (index, angle) ->
                        val zekr = azkaar[index]
                        val isSelected = index == selectedIndex

                        val t = (angle / maxArcAngle).coerceIn(-1f, 1f)
                        val cardWidth = 180.dp
                        val cardWidthPx = with(density) { cardWidth.toPx() }
                        val overlapFactor = 1.3f
                        val maxHorizontalOffset =
                            (cardWidthPx * (cardCount - 1) / 2f) * overlapFactor

                        val arcDepthPx = with(density) { 280.dp.toPx() }

                        val x = maxHorizontalOffset * t
                        val y = -arcDepthPx * t * t

                        val rawDepth = (1f - abs(t)).coerceIn(0f, 1f)
                        val baseScale = lerp(0.7f, 1f, rawDepth)

                        val selectionScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "selectionScale"
                        )

                        val scale = baseScale * selectionScale

                        val alphaDepth = rawDepth.coerceIn(0.3f, 1f)
                        val alpha = (alphaDepth - 0.3f) / 0.7f

                        val maxTiltDeg = -45f
                        val rotationZ = t * maxTiltDeg

                        ZekkrCard(
                            paddingValues = paddingValues,
                            zekr = zekr,
                            isSelected = isSelected,
                            isFlipped = flippedCards[zekr.content] ?: false,
                            primaryColor = primaryColor,
                            currentCount = countsMap[zekr.content] ?: zekr.count.toInt(),
                            onCountChange = { newCount ->
                                onCountChange(zekr.content, newCount)
                            },
                            onShowDetail = { onShowDetail(zekr, index) },
                            modifier = Modifier.graphicsLayer {
                                translationX = x
                                translationY = y
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                this.rotationZ = rotationZ
                            }
                        )
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

@Composable
private fun ZekkrCard(
    paddingValues: PaddingValues,
    zekr: Zakker,
    isSelected: Boolean,
    isFlipped: Boolean,
    primaryColor: Color,
    currentCount: Int,
    onCountChange: (Int) -> Unit,
    onShowDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val cardWidth = 180.dp
    val cardHeight = 240.dp

    val flipRotation = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(isFlipped) {
        if (isFlipped && flipRotation.value == 0f) {
            flipRotation.animateTo(
                targetValue = 180f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // عشان نتأكد إن الكارد تفضل مقلوبة لما نرجع من التفاصيل
    LaunchedEffect(Unit) {
        if (isFlipped) {
            flipRotation.snapTo(180f)
        }
    }

    val progress = ((zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat())
    val isComplete = currentCount == 0
    val shouldTruncate = zekr.content.length > 60

    Surface(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .graphicsLayer {
                rotationY = flipRotation.value
                cameraDistance = 12f * density.density
            },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = if (isSelected) 12.dp else 6.dp,
        color = Color.Transparent,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (flipRotation.value > 90f) {
                            listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.8f)
                            )
                        } else {
                            listOf(
                                Color.White,
                                Color(0xFFFAFAFA)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = primaryColor.copy(alpha = if (isSelected) 0.3f else 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
                .pointerInput(currentCount) {
                    detectTapGestures(
                        onPress = {
                            scope.launch {
                                scale.animateTo(
                                    0.95f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    )
                                )
                            }
                            tryAwaitRelease()
                            scope.launch {
                                scale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onTap = {
                            if (currentCount > 0 && !isFlipped) {
                                onCountChange(currentCount - 1)
                            }
                        },
                        onLongPress = {
                            if (!isFlipped) {
                                onShowDetail()
                            }
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
        ) {
            // المحتوى كله محتاج يتعكس عشان يبقى معدول لما الكارد تتقلب
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // نعكس المحتوى لما الكارد تتقلب عشان يفضل معدول
                        rotationY = if (flipRotation.value > 90f) 180f else 0f
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rightcorner),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopStart)
                        .alpha(if (flipRotation.value > 90f) 0f else 1f)
                )
                Image(
                    painter = painterResource(id = R.drawable.leftcorner),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopEnd)
                        .alpha(if (flipRotation.value > 90f) 0f else 1f)
                )

                // الوجه الخلفي - رسالة الاحتفال
                if (flipRotation.value > 90f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
/*
                        // النجوم تطلع من زوايا الشاشة الكبيرة (خارج الكارد)
                        repeat(3) { index ->
                            val starScale = remember { Animatable(0f) }
                            val starRotation = remember { Animatable(-1080f) }
                            val starAlpha = remember { Animatable(0f) }

                            // كل نجمة تبدأ من زاوية الشاشة الحقيقية
                            val corner = index % 4
                            val startX = remember {
                                Animatable(when (corner) {
                                    0, 2 -> -300f  // يسار
                                    else -> 300f   // يمين
                                })
                            }
                            val startY = remember {
                                Animatable(when (corner) {
                                    0, 1 -> -300f  // أعلى
                                    else -> 300f   // أسفل
                                })
                            }

                            // المواقع النهائية في وسط الكارد
                            val targetX = when (index % 3) {
                                0 -> -20f
                                1 -> 20f
                                2 -> -10f
                                else -> 10f
                            }
                            val targetY = when (index / 3) {
                                0 -> -15f
                                1 -> 0f
                                2 -> 15f
                                else -> 5f
                            }

                            LaunchedEffect(isFlipped) {
                                if (isFlipped) {
                                    kotlinx.coroutines.delay((index * 60L))

                                    launch {
                                        starScale.animateTo(
                                            targetValue = 1.5f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = 50f
                                            )
                                        )
                                        starScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }

                                    launch {
                                        starRotation.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = 40f
                                            )
                                        )
                                    }

                                    launch {
                                        starAlpha.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }

                                    launch {
                                        startX.animateTo(
                                            targetValue = targetX,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = 45f
                                            )
                                        )
                                    }

                                    launch {
                                        startY.animateTo(
                                            targetValue = targetY,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = 45f
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "⭐",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = startX.value.dp, y = startY.value.dp)
                                    .graphicsLayer {
                                        scaleX = starScale.value
                                        scaleY = starScale.value
                                        rotationZ = starRotation.value
                                        alpha = starAlpha.value
                                    }
                            )
                        }
*/


                        // محتوى الاحتفال
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 30.dp, y = (-30).dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(60.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-30).dp, y = 30.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(50.dp)
                                    )
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(28.dp)
                                        )
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.tsbehicon),
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = stringResource(R.string.well_done),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = stringResource(R.string.zikr_shield),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 20.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.95f),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(R.string.keep_it_up),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                            }
                        }
                    }
                }

                // الوجه الأمامي - المحتوى الأصلي
                if (flipRotation.value <= 90f) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(top = if (isComplete) 8.dp else 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (shouldTruncate) "${zekr.content.take(60)}..." else zekr.content,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 22.sp
                                    ),
                                    color = Black,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (shouldTruncate) {
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                            Text(
                                                text = stringResource(R.string.show_more),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                painter = painterResource(id = R.drawable.tsbehicon),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(60.dp),
                                    color = primaryColor,
                                    strokeWidth = 5.dp,
                                    trackColor = Color(0xFFE0E0E0),
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$currentCount",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = primaryColor
                                    )
                                    Text(
                                        text = stringResource(R.string.remaining),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp
                                        ),
                                        color = Black.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            primaryColor.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val shareText = "${zekr.content}\n\n${zekr.description}"
                                    ShareTextIcon(text = shareText)
                                }

                            }
                        }
                    }
                }

                // شارة الإكمال
                if (isComplete && flipRotation.value <= 90f) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-12).dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = primaryColor,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.completed),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    val progress = ((zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat())
    val isComplete = currentCount == 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.zekr_details),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 60.dp, y = (-60).dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = RoundedCornerShape(60.dp)
                                )
                        )

                        if (isComplete) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = primaryColor,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-12).dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.completed),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .padding(top = if (isComplete) 8.dp else 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = zekr.content,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 36.sp
                                ),
                                color = Black,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier.size(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(110.dp),
                                    color = primaryColor,
                                    strokeWidth = 8.dp,
                                    trackColor = Color(0xFFE0E0E0),
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$currentCount",
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = primaryColor
                                    )
                                    Text(
                                        text = stringResource(R.string.remaining),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Black.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (currentCount > 0) {
                                        onCountChange(currentCount - 1)
                                    }
                                },
                                enabled = currentCount > 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = if (currentCount == 0)
                                        stringResource(R.string.count_completed)
                                    else
                                        stringResource(R.string.tap_to_count)
                                    ,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (zekr.description.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            primaryColor.copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.tsbehicon),
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.description),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                            Text(
                                text = zekr.description,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp
                                ),
                                color = Black.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, primaryColor)
                ) {
                    val shareText = "${zekr.content}\n\n${zekr.description}"
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShareTextIcon(text = shareText)
                        Text(
                            text = stringResource(R.string.share_zekr),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SelectionFrame(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 194.dp, height = 256.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 6.dp,
                    color = primaryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    color = primaryColor,
                    shape = RoundedCornerShape(28.dp)
                )
        )
    }
}

@Composable
private fun ConfettiEffect(
    primaryColor: Color,
    trigger: Int,
    onFinish: () -> Unit
) {
    val confettiColors = listOf(
        primaryColor,
        Color(0xFF10b981),
        Color(0xFF3b82f6),
        Color(0xFFf59e0b),
        Color(0xFFef4444),
        Color(0xFF8b5cf6)
    )

    val confettiCount = 60
    val confettiPieces = remember(trigger) {
        List(confettiCount) { index ->
            // كل كونفيتي يبدأ من زاوية مختلفة
            val corner = index % 4
            val startX = when (corner) {
                0, 2 -> -400f  // يسار بعيد
                else -> 400f   // يمين بعيد
            }
            val startY = when (corner) {
                0, 1 -> -400f  // أعلى بعيد
                else -> 400f   // أسفل بعيد
            }

            // الهدف: منتصف الشاشة مع انتشار عشوائي بسيط
            val endX = (Math.random() * 120 - 60).toFloat()
            val endY = (Math.random() * 120 - 60).toFloat()

            ConfettiPiece(
                id = index,
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                startDelay = (Math.random() * 300).toLong(),
                duration = (1500 + Math.random() * 1000).toLong(),
                color = confettiColors.random()
            )
        }
    }

    LaunchedEffect(trigger) {
        kotlinx.coroutines.delay(3000)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        confettiPieces.forEach { piece ->
            ConfettiParticle(piece = piece)
        }
    }
}

data class ConfettiPiece(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val startDelay: Long,
    val duration: Long,
    val color: Color
)

@Composable
private fun ConfettiParticle(piece: ConfettiPiece) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti${piece.id}")

    // الحركة من الزاوية للوسط ثم للأسفل
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = piece.duration.toInt(),
                easing = androidx.compose.animation.core.FastOutSlowInEasing,
                delayMillis = piece.startDelay.toInt()
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress${piece.id}"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 720f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = piece.duration.toInt(),
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation${piece.id}"
    )

    // حساب الموقع: من الزاوية البعيدة للمنتصف ثم للأسفل
    val offsetX = if (progress < 0.4f) {
        // من الزاوية للمنتصف
        lerp(piece.startX, piece.endX, progress / 0.4f)
    } else {
        // من المنتصف للأسفل (مع حركة جانبية)
        piece.endX + (sin((progress - 0.4f) * 12) * 30).toFloat()
    }

    val offsetY = if (progress < 0.4f) {
        // من الزاوية للمنتصف
        lerp(piece.startY, piece.endY, progress / 0.4f)
    } else {
        // سقوط للأسفل مع تسارع
        piece.endY + ((progress - 0.4f) * (progress - 0.4f) * 1500f)
    }

    val alpha = if (progress > 0.75f) {
        // fade out في النهاية
        1f - ((progress - 0.75f) / 0.25f)
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .offset(
                x = offsetX.dp,
                y = offsetY.dp
            )
            .size(14.dp)
            .graphicsLayer {
                rotationZ = rotation
                this.alpha = alpha
            }
            .background(
                color = piece.color,
                shape = RoundedCornerShape(3.dp)
            )
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}