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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
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

    val countsMap = remember { mutableStateMapOf<String, Int>() }
    val flippedCards = remember { mutableStateMapOf<String, Boolean>() }

    var showConfetti by remember { mutableStateOf(false) }
    val confettiTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(state.azkaar) {
        state.azkaar.forEach { zekr ->
            countsMap.getOrPut(zekr.content) { zekr.count.toInt().coerceAtLeast(1) }
        }
    }

    var savedZekrIndex by remember { mutableStateOf(0) }
    var bottomSheetZekr by remember { mutableStateOf<Zakker?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    if (newCount == 0 && prev > 0) {
                        flippedCards[zekrContent] = true
                        showConfetti = true
                        confettiTrigger.value++
                    }
                },
                onShowDetail = { zekr, currentIndex ->
                    bottomSheetZekr = zekr
                    savedZekrIndex = currentIndex
                },
                modifier = Modifier.padding(paddingValues)
            )
        }

        if (bottomSheetZekr != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    val zekr = bottomSheetZekr!!
                    if ((countsMap[zekr.content] ?: 0) == 0) {
                        flippedCards[zekr.content] = true
                        showConfetti = true
                        confettiTrigger.value++
                    }
                    bottomSheetZekr = null
                },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 0.dp
            ) {
                ZekkrDetailBottomSheet(
                    zekr = bottomSheetZekr!!,
                    primaryColor = primary,
                    currentCount = countsMap[bottomSheetZekr!!.content] ?: bottomSheetZekr!!.count.toInt(),
                    onCountChange = { newCount -> countsMap[bottomSheetZekr!!.content] = newCount },
                    onDismiss = {
                        val zekr = bottomSheetZekr!!
                        if ((countsMap[zekr.content] ?: 0) == 0) {
                            flippedCards[zekr.content] = true
                            showConfetti = true
                            confettiTrigger.value++
                        }
                        bottomSheetZekr = null
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZekkrDetailBottomSheet
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ZekkrDetailBottomSheet(
    zekr: Zakker,
    primaryColor: Color,
    currentCount: Int,
    onCountChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val progress   = (zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat()
    val isComplete = currentCount == 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color(0xFFE0E0E0), CircleShape)
        )
        Spacer(Modifier.height(4.dp))

        if (isComplete) {
            Surface(shape = RoundedCornerShape(20.dp), color = primaryColor) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.tsbehicon), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.completed), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(listOf(primaryColor.copy(0.06f), primaryColor.copy(0.02f))), shape = RoundedCornerShape(20.dp))
                .border(1.dp, primaryColor.copy(0.12f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = zekr.content,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 34.sp, fontSize = 18.sp),
                color = Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(90.dp), color = primaryColor, strokeWidth = 7.dp, trackColor = Color(0xFFE0E0E0))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$currentCount", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = primaryColor)
                    Text(stringResource(R.string.remaining), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Black.copy(0.5f))
                }
            }
            Button(
                onClick = { if (currentCount > 0) onCountChange(currentCount - 1) },
                enabled = currentCount > 0,
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, disabledContainerColor = Color.Gray.copy(0.25f))
            ) {
                Text(
                    text = if (currentCount == 0) stringResource(R.string.count_completed) else stringResource(R.string.tap_to_count),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (zekr.description.isNotBlank()) {
            HorizontalDivider(color = primaryColor.copy(0.1f))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(primaryColor.copy(0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.tsbehicon), null, tint = primaryColor, modifier = Modifier.size(18.dp))
                    }
                    Text(stringResource(R.string.description), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                }
                Text(zekr.description, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 26.sp), color = Black.copy(0.8f))
            }
        }

        OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, primaryColor)
        ) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                ShareTextIcon(text = "${zekr.content}\n\n${zekr.description}")
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.share_zekr), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryColor)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZekkrArcContent
// ─────────────────────────────────────────────────────────────────────────────
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
    val thumbnailListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        angleOffset.snapTo(-((-maxArcAngle) + initialZekrIndex * stepAngle))
    }

    val currentAngleValue = angleOffset.value
    val midIndex = (cardCount - 1) / 2f

    val selectedIndex = remember(currentAngleValue, cardCount, stepAngle) {
        azkaar.indices.minByOrNull { i -> abs((i - midIndex) * stepAngle + currentAngleValue) } ?: 0
    }

    LaunchedEffect(selectedIndex) {
        thumbnailListState.animateScrollToItem(index = selectedIndex, scrollOffset = -200)
    }

    val countsMapRef    by rememberUpdatedState(countsMap)
    val flippedCardsRef by rememberUpdatedState(flippedCards)
    val azkaarRef       by rememberUpdatedState(azkaar)

    LaunchedEffect(flippedCards.size) {
        if (flippedCards.isNotEmpty()) {
            val nextIndex = azkaarRef.indices.firstOrNull { i -> i > selectedIndex && flippedCardsRef[azkaarRef[i].content] != true }
                ?: azkaarRef.indices.firstOrNull { i -> flippedCardsRef[azkaarRef[i].content] != true }
            if (nextIndex != null && nextIndex != selectedIndex) {
                kotlinx.coroutines.delay(800)
                angleOffset.animateTo(-((-maxArcAngle) + nextIndex * stepAngle), spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
            }
        }
    }

    val selectedIndexRef by rememberUpdatedState(selectedIndex)
    val angleOffsetRef   by rememberUpdatedState(angleOffset)
    val stepAngleRef     by rememberUpdatedState(stepAngle)
    val maxArcAngleRef   by rememberUpdatedState(maxArcAngle)

    Box(modifier = modifier.fillMaxSize()) {
        ZekkrDynamicBackground(bgType = bgType, modifier = Modifier.matchParentSize())

        if (showConfetti) {
            ConfettiEffect(primaryColor = primaryColor, trigger = confettiTrigger, onFinish = { onShowConfetti(false) })
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(6.dp))

            // ✅ Header مصغر — أيقونة أصغر + نص أصغر + مسافات أقل
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(primaryColor.copy(0.2f), primaryColor.copy(0.05f))),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.tsbehicon),
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "اذْكُرُوا اللَّهَ ذِكْرًا كَثِيرًا",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Start,
                    color = primaryColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── منطقة الكروت ──────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val frameYOffset = 16.dp

                Text(
                    text = stringResource(R.string.drag_to_select_zekr),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = frameYOffset + 220.dp)
                        .alpha(0.65f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                )

                // ✅ الإطار أكبر (210×300)
                SelectionFrame(primaryColor = primaryColor, modifier = Modifier.offset(y = frameYOffset))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .offset(y = frameYOffset)
                        .pointerInput(Unit) {
                            val dragSensitivity  = 0.0035f
                            val dragSlop         = 10f
                            val longPressDuration = 450L

                            awaitEachGesture {
                                val down  = awaitFirstDown(requireUnconsumed = false)
                                val downX = down.position.x
                                var prevX = downX
                                var totalDx = 0f
                                var pointerUp   = false
                                var wasDragging = false

                                val completedBeforeTimeout = withTimeoutOrNull(longPressDuration) {
                                    while (true) {
                                        val event  = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break
                                        val x  = change.position.x
                                        val dx = x - prevX
                                        totalDx = abs(x - downX)
                                        if (totalDx > dragSlop) {
                                            wasDragging = true
                                            change.consume()
                                            scope.launch {
                                                val target = angleOffsetRef.value + dx * dragSensitivity
                                                angleOffsetRef.snapTo(angleOffsetRef.value + 0.55f * (target - angleOffsetRef.value))
                                            }
                                        }
                                        prevX = x
                                        if (change.changedToUp()) {
                                            if (wasDragging) change.consume()
                                            pointerUp = true
                                            break
                                        }
                                    }
                                    true
                                }

                                if (wasDragging) {
                                    scope.launch {
                                        val baseAngle = -maxArcAngleRef
                                        val (_, closestAngle) = azkaarRef.indices
                                            .map { i -> i to (baseAngle + i * stepAngleRef + angleOffsetRef.value) }
                                            .minBy { (_, a) -> abs(a) }
                                        angleOffsetRef.animateTo(angleOffsetRef.value - closestAngle, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                                    }
                                    return@awaitEachGesture
                                }

                                val idx = selectedIndexRef
                                if (idx < 0 || idx >= azkaarRef.size) return@awaitEachGesture
                                val zekr = azkaarRef[idx]
                                if (flippedCardsRef[zekr.content] == true) return@awaitEachGesture

                                when {
                                    completedBeforeTimeout == null -> onShowDetail(zekr, idx)
                                    pointerUp -> {
                                        val cc = countsMapRef[zekr.content] ?: zekr.count.toInt()
                                        if (cc > 0) onCountChange(zekr.content, cc - 1)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val sortedIndices = azkaar.indices.sortedBy { i -> -abs((i - midIndex) * stepAngle + currentAngleValue) }

                    sortedIndices.forEach { index ->
                        val angle      = (index - midIndex) * stepAngle + currentAngleValue
                        val isSelected = index == selectedIndex
                        val t          = (angle / maxArcAngle).coerceIn(-1f, 1f)
                        val rawDepth   = (1f - abs(t)).coerceIn(0f, 1f)
                        if (rawDepth < 0.05f) return@forEach

                        val cardWidthPx        = with(density) { 210.dp.toPx() }
                        val maxHorizontalOffset = cardWidthPx * (cardCount - 1) / 2f * 1.3f
                        val arcDepthPx          = with(density) { 310.dp.toPx() }

                        val x         = maxHorizontalOffset * t
                        val y         = -arcDepthPx * t * t
                        val scale     = lerp(0.7f, 1f, rawDepth) * if (isSelected) 1.05f else 1f
                        val alpha     = ((rawDepth - 0.05f) / 0.95f).coerceIn(0f, 1f)
                        val rotationZ = t * -45f
                        val zekr      = azkaar[index]

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

            Spacer(Modifier.height(6.dp))

            ZekkrThumbnailStrip(
                azkaar = azkaar,
                selectedIndex = selectedIndex,
                countsMap = countsMap,
                flippedCards = flippedCards,
                primaryColor = primaryColor,
                listState = thumbnailListState,
                onSelectIndex = { targetIndex ->
                    scope.launch {
                        angleOffset.animateTo(-((-maxArcAngle) + targetIndex * stepAngle), spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                    }
                }
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.tap_to_count_long_press_details),
                style = MaterialTheme.typography.bodySmall,
                color = Black.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ZekkrThumbnailStrip
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ZekkrThumbnailStrip(
    azkaar: List<Zakker>,
    selectedIndex: Int,
    countsMap: Map<String, Int>,
    flippedCards: Map<String, Boolean>,
    primaryColor: Color,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSelectIndex: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(listOf(primaryColor.copy(0.04f), primaryColor.copy(0.10f))), shape = RoundedCornerShape(20.dp))
            .border(1.dp, primaryColor.copy(0.15f), RoundedCornerShape(20.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            itemsIndexed(azkaar) { index, zekr ->
                val isSelected   = index == selectedIndex
                val isFlipped    = flippedCards[zekr.content] == true
                val currentCount = countsMap[zekr.content] ?: zekr.count.toInt()
                val progress     = if (zekr.count.toInt() > 0) (zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat() else 0f
                val thumbScale   = remember { Animatable(1f) }

                LaunchedEffect(isSelected) {
                    thumbScale.animateTo(if (isSelected) 1.12f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer { scaleX = thumbScale.value; scaleY = thumbScale.value }
                        .width(60.dp).height(78.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(when {
                            isFlipped  -> Brush.verticalGradient(listOf(primaryColor, primaryColor.copy(0.75f)))
                            isSelected -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F8FF)))
                            else       -> Brush.verticalGradient(listOf(Color.White.copy(0.85f), Color.White.copy(0.7f)))
                        })
                        .border(if (isSelected) 2.dp else 0.5.dp, if (isSelected) primaryColor else primaryColor.copy(0.2f), RoundedCornerShape(12.dp))
                        .pointerInput(index) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                val up = withTimeoutOrNull(300L) {
                                    var lifted = false
                                    while (!lifted) {
                                        val ev = awaitPointerEvent()
                                        val ch = ev.changes.firstOrNull() ?: break
                                        if (ch.changedToUp()) lifted = true
                                    }
                                    true
                                }
                                if (up != null) onSelectIndex(index)
                            }
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFlipped) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(painterResource(R.drawable.tsbehicon), null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("✓", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, fontSize = 13.sp)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(32.dp), color = primaryColor, strokeWidth = 3.dp, trackColor = primaryColor.copy(0.15f))
                                Text("${index + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = if (isSelected) primaryColor else Black.copy(0.6f))
                            }
                            Text(zekr.content.take(12), style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, lineHeight = 10.sp), color = if (isSelected) Black else Black.copy(0.5f), textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (isSelected && !isFlipped) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = 2.dp).size(5.dp).background(primaryColor, CircleShape))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ✅ ZekkrCard — كارد أكبر (210×300) + زر "تفاصيل" واضح جنب زر المشاركة
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
    // ✅ كارد أكبر بشوية
    val cardWidth  = 210.dp
    val cardHeight = 300.dp

    val flipRotation = remember { Animatable(if (isFlipped) 180f else 0f) }
    val density = LocalDensity.current

    LaunchedEffect(isFlipped) {
        if (isFlipped && flipRotation.value < 180f) {
            flipRotation.animateTo(180f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        }
    }

    val progress       = (zekr.count.toInt() - currentCount).toFloat() / zekr.count.toFloat()
    val hasStarted     = currentCount < zekr.count.toInt().coerceAtLeast(1)
    val isComplete     = currentCount == 0 && hasStarted
    val shouldTruncate = zekr.content.length > 140
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
                        if (isShowingBack) listOf(primaryColor, primaryColor.copy(0.8f))
                        else listOf(Color.White, Color(0xFFFAFAFA))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    if (isSelected) 2.dp else 1.dp,
                    primaryColor.copy(if (isSelected) 0.3f else 0.1f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = if (isShowingBack) 180f else 0f }
            ) {
                // ── وجه خلفي ─────────────────────────────────────────────────
                if (isShowingBack) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(30.dp, (-30).dp).background(Brush.radialGradient(listOf(Color.White.copy(0.3f), Color.Transparent)), RoundedCornerShape(60.dp)))
                        Box(modifier = Modifier.size(100.dp).align(Alignment.BottomStart).offset((-30).dp, 30.dp).background(Brush.radialGradient(listOf(Color.White.copy(0.3f), Color.Transparent)), RoundedCornerShape(50.dp)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
                            Box(modifier = Modifier.size(56.dp).background(Color.White, RoundedCornerShape(28.dp)).padding(2.dp), contentAlignment = Alignment.Center) {
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

                // ── وجه أمامي ────────────────────────────────────────────────
                if (!isShowingBack) {
                    Image(painterResource(R.drawable.rightcorner), null, modifier = Modifier.size(36.dp).align(Alignment.TopStart))
                    Image(painterResource(R.drawable.leftcorner),  null, modifier = Modifier.size(36.dp).align(Alignment.TopEnd))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                            .padding(top = if (isComplete) 22.dp else 6.dp, bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ✅ النص — عتبة 140 حرف، 7 سطور
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (shouldTruncate) "${zekr.content.take(140)}…" else zekr.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize   = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 21.sp
                                ),
                                color = Black,
                                textAlign = TextAlign.Center,
                                maxLines = 7,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // ✅ Progress + زرين جنب بعض: تسبيح + تفاصيل
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(60.dp), color = primaryColor, strokeWidth = 5.dp, trackColor = Color(0xFFE0E0E0))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$currentCount", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = primaryColor)
                                    Text(stringResource(R.string.remaining), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Black.copy(0.5f))
                                }
                            }

                            // ✅ صف الزرين: مشاركة + تفاصيل
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // زر المشاركة
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(primaryColor.copy(0.1f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ShareTextIcon(text = "${zekr.content}\n\n${zekr.description}")
                                }

                                // ✅ زر "تفاصيل" — واضح ومرئي دايماً
                                Surface(
                                    onClick = onShowDetail,
                                    shape = RoundedCornerShape(10.dp),
                                    color = primaryColor.copy(0.12f),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "تفاصيل",
                                            tint = primaryColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "تفاصيل",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── بادج "مكتمل" ──────────────────────────────────────────
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
@Composable
private fun SelectionFrame(primaryColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 224.dp, height = 316.dp)) {
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
    val progress by infiniteTransition.animateFloat(0f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(piece.duration.toInt(), easing = androidx.compose.animation.core.FastOutSlowInEasing, delayMillis = piece.startDelay.toInt()), RepeatMode.Restart), label = "p${piece.id}")
    val rotation by infiniteTransition.animateFloat(0f, 720f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(piece.duration.toInt(), easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart), label = "r${piece.id}")
    val offsetX = if (progress < 0.4f) lerp(piece.startX, piece.endX, progress / 0.4f) else piece.endX + (sin((progress - 0.4f) * 12) * 30).toFloat()
    val offsetY = if (progress < 0.4f) lerp(piece.startY, piece.endY, progress / 0.4f) else piece.endY + (progress - 0.4f).let { it * it * 1500f }
    val alpha   = if (progress > 0.75f) 1f - (progress - 0.75f) / 0.25f else 1f
    Box(modifier = Modifier.offset(offsetX.dp, offsetY.dp).size(14.dp).graphicsLayer { rotationZ = rotation; this.alpha = alpha }.background(piece.color, RoundedCornerShape(3.dp)))
}

private fun lerp(start: Float, stop: Float, fraction: Float) = start + (stop - start) * fraction