package com.elsharif.dailyseventy.presentation.travel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import com.elsharif.dailyseventy.BuildConfig
import com.elsharif.dailyseventy.R
import kotlin.math.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// ══════════════════════════════════════════════════════════════════════════════
//  TRAVEL SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    onBackClick: () -> Unit,
    viewModel  : TravelViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.travel_mode_title), fontWeight = FontWeight.Bold)
                        Text(
                            if (state.isActive) {
                                val emoji = if (state.activeDistanceKm in 1..<LOCAL_TRANSPORT_KM) "🚂" else "✈️"
                                "$emoji ${stringResource(R.string.travel_mode_active)} · ${state.destination}"
                            } else stringResource(R.string.travel_mode_inactive),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isActive) Color(0xFF0288D1)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding      = PaddingValues(bottom = 40.dp)
        ) {

            // ── 1. بحث المدينة
            item {
                CitySearchWithMapCard(
                    query         = state.searchQuery,
                    results       = state.searchResults,
                    isSearching   = state.isSearching,
                    selectedCity  = state.selectedCity,
                    error         = state.searchError,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onCitySelect  = { city ->
                        viewModel.onCitySelected(city)
                        focusManager.clearFocus()
                    },
                    onClear = viewModel::clearSearch
                )
            }

            // ── 2. الحكم الشرعي
            item {
                AnimatedVisibility(
                    visible = state.fiqhResult != null,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    state.fiqhResult?.let { fiqh ->
                        ShafiiFiqhCard(
                            fiqh                   = fiqh,
                            selectedCity           = state.selectedCity,
                            userCity               = state.userCityName,
                            showDetails            = state.showFiqhDetails,
                            isActive               = state.isActive,
                            onToggleDetails        = viewModel::toggleFiqhDetails,
                            onActivate             = viewModel::activateWithCity,
                            onDeactivate           = viewModel::toggleTravelMode,
                            onChangeDestination    = viewModel::changeDestination,
                            onShowChangeDestDialog = viewModel::showChangeDestinationDialog,
                            onDismissChangeDialog  = viewModel::dismissChangeDestinationDialog,
                            showChangeDestDialog   = state.showChangeDestDialog
                        )
                    }
                }
            }

            // ── 3. كارد السفر النشط
            if (state.isActive) {
                item {
                    TravelStatusCard(
                        destination = state.destination,
                        userCity    = state.userCityName,
                        timeDiff    = state.timeDiff,
                        distance    = state.activeDistanceKm,
                        duration    = state.activeDuration,
                        onToggle    = viewModel::toggleTravelMode
                    )
                }
            }

            // ── 4. أوقات الصلاة
            item {
                SectionTitle(
                    title = stringResource(R.string.prayer_times),
                    subtitle = if (state.fiqhResult?.isSafar == true || state.isActive)
                        stringResource(R.string.shortened_combined)
                    else stringResource(R.string.normal)
                )
            }
            items(state.prayerTimes) { prayer ->
                PrayerCard(prayer = prayer)
            }

            // ── 5. بوصلة القبلة
            item {
                QiblaCompassCard(
                    userLat         = state.userLat,
                    userLng         = state.userLng,
                    distanceToKaaba = state.distanceToKaaba
                )
            }

            // ── 6. الأحكام الشرعية للمسافر
            item {
                IslamicRulingsCard()
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  QIBLA COMPASS CARD
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun QiblaCompassCard(
    userLat        : Double,
    userLng        : Double,
    distanceToKaaba: Int
) {
    val context = LocalContext.current

    var currentHeading by remember { mutableFloatStateOf(0f) }
    var qiblaHeading   by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(userLat, userLng) {
        if (userLat != 0.0 && userLng != 0.0) {
            val dLng = Math.toRadians(KAABA_LNG_CONST - userLng)
            val lat1 = Math.toRadians(userLat)
            val lat2 = Math.toRadians(KAABA_LAT_CONST)
            val y    = sin(dLng) * cos(lat2)
            val x    = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
            qiblaHeading = ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
        }
    }

    val sensorManager     = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager }
    val lastAccelerometer = remember { FloatArray(3) }
    val lastMagnetometer  = remember { FloatArray(3) }
    var accSet by remember { mutableStateOf(false) }
    var magSet by remember { mutableStateOf(false) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val alpha = 0.05f
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in event.values.indices)
                            lastAccelerometer[i] += alpha * (event.values[i] - lastAccelerometer[i])
                        accSet = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in event.values.indices)
                            lastMagnetometer[i] += alpha * (event.values[i] - lastMagnetometer[i])
                        magSet = true
                    }
                }
                if (accSet && magSet) {
                    val r = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        currentHeading = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    val needleRotation = qiblaHeading - currentHeading
    val angleToQibla   = (qiblaHeading - currentHeading + 360) % 360
    val isFacingQibla  = angleToQibla <= 10f || angleToQibla >= 350f

    val compassRingColor by animateColorAsState(
        if (isFacingQibla) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
        tween(600), label = "ring"
    )
    val needleColor by animateColorAsState(
        if (isFacingQibla) Color(0xFF4CAF50) else Color(0xFF1565C0),
        tween(600), label = "needle"
    )
    val rotationAnim by animateFloatAsState(
        targetValue   = needleRotation,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "qibla"
    )

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Explore, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.qibla_direction),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                AnimatedVisibility(
                    visible = isFacingQibla,
                    enter   = fadeIn() + scaleIn(),
                    exit    = fadeOut() + scaleOut()
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF4CAF50)) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🕋", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.facing_qibla),
                                style      = MaterialTheme.typography.labelMedium,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(Modifier.size(200.dp), Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r  = size.width / 2f - 6f

                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFF8F9FA), Color(0xFFECEFF1)),
                            Offset(cx, cy), r
                        ), r
                    )
                    drawCircle(compassRingColor, r, style = Stroke(4f))

                    listOf(0f, 90f, 180f, 270f).forEachIndexed { i, angle ->
                        val rad  = Math.toRadians((angle - 90).toDouble())
                        val dotR = if (i == 0) 4f else 2.5f
                        drawCircle(
                            if (i == 0) Color(0xFFF44336) else Color(0xFF9E9E9E), dotR,
                            Offset(
                                cx + (cos(rad) * (r - 14f)).toFloat(),
                                cy + (sin(rad) * (r - 14f)).toFloat()
                            )
                        )
                    }

                    for (deg in 0 until 360 step 30) {
                        val rad = Math.toRadians((deg - 90).toDouble())
                        val cos = cos(rad).toFloat()
                        val sin = sin(rad).toFloat()
                        val len = if (deg % 90 == 0) 10f else 5f
                        drawLine(
                            Color(0xFFBDBDBD), strokeWidth = 1.5f,
                            start = Offset(cx + cos * (r - len - 2f), cy + sin * (r - len - 2f)),
                            end   = Offset(cx + cos * (r - 2f), cy + sin * (r - 2f))
                        )
                    }
                }

                Box(Modifier.size(130.dp).rotate(rotationAnim), Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        val northPath = Path().apply {
                            moveTo(cx, cy - size.height * 0.43f)
                            lineTo(cx - 9f, cy + 4f)
                            lineTo(cx, cy - size.height * 0.05f)
                            lineTo(cx + 9f, cy + 4f)
                            close()
                        }
                        drawPath(northPath, needleColor.copy(alpha = 0.15f), style = Stroke(2f))
                        drawPath(northPath, needleColor)

                        val southPath = Path().apply {
                            moveTo(cx, cy + size.height * 0.43f)
                            lineTo(cx - 7f, cy - 4f)
                            lineTo(cx, cy + size.height * 0.05f)
                            lineTo(cx + 7f, cy - 4f)
                            close()
                        }
                        drawPath(southPath, Color(0xFFBDBDBD))

                        drawCircle(Color.White, 9f, Offset(cx, cy))
                        drawCircle(needleColor, 6f, Offset(cx, cy))
                        drawCircle(Color.White, 3f, Offset(cx, cy))
                    }
                }

                Text(stringResource(R.string.north), Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
                    fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF44336))
                Text(stringResource(R.string.south), Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
                    fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                Text(stringResource(R.string.east), Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                    fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF9E9E9E))
                Text(stringResource(R.string.west), Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
                    fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF9E9E9E))
            }

            Spacer(Modifier.height(16.dp))

            QiblaDirectionBar(
                angleToQibla  = angleToQibla,
                isFacingQibla = isFacingQibla,
                qiblaColor    = compassRingColor
            )

            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${qiblaHeading.toInt()}°",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color      = compassRingColor)
                    Text(getDirectionText(qiblaHeading),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.qibla_direction),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(Modifier.height(50.dp).width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val dist = if (distanceToKaaba > 0) "$distanceToKaaba ${stringResource(R.string.km)}" else "—"
                    Text(dist,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.distance_to_kaaba),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun QiblaDirectionBar(
    angleToQibla  : Float,
    isFacingQibla : Boolean,
    qiblaColor    : Color
) {
    val normalizedAngle = if (angleToQibla > 180f) angleToQibla - 360f else angleToQibla
    val position = ((normalizedAngle + 180f) / 360f).coerceIn(0f, 1f)
    val animPos  by animateFloatAsState(position, tween(300), label = "pos")

    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.056f)
                    .align(Alignment.Center)
                    .background(Color(0xFF4CAF50).copy(0.3f))
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (animPos * 100 - 1).dp.coerceAtLeast(0.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(qiblaColor)
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(stringResource(R.string.qibla_bar_left), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (isFacingQibla) stringResource(R.string.facing_qibla)
                else {
                    val deg = normalizedAngle.toInt()
                    if (deg < 0) stringResource(R.string.turn_left, -deg)
                    else stringResource(R.string.turn_right, deg)
                },
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = if (isFacingQibla) FontWeight.Bold else FontWeight.Normal,
                color      = if (isFacingQibla) Color(0xFF2E7D32)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(stringResource(R.string.qibla_bar_right), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private const val KAABA_LAT_CONST    = 21.422487
private const val KAABA_LNG_CONST    = 39.826206
private const val LOCAL_TRANSPORT_KM = 1000



// ══════════════════════════════════════════════════════════════════════════════
//  CITY SEARCH + MAP
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchWithMapCard(
    query        : String,
    results      : List<CityResult>,
    isSearching  : Boolean,
    selectedCity : CityResult?,
    error        : String?,
    onQueryChange: (String) -> Unit,
    onCitySelect : (CityResult) -> Unit,
    onClear      : () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        stringResource(R.string.search_destination),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.city_search_instruction), // need to add this resource
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = query,
                onValueChange = onQueryChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text(stringResource(R.string.city_hint)) },
                leadingIcon   = {
                    if (isSearching)
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else
                        Icon(Icons.Default.TravelExplore, null)
                },
                trailingIcon = {
                    if (query.isNotEmpty())
                        IconButton(onClick = onClear) { Icon(Icons.Default.Close, null) }
                },
                shape      = RoundedCornerShape(14.dp),
                singleLine = true
            )

            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            AnimatedVisibility(visible = results.isNotEmpty()) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    results.forEach { city ->
                        CityResultRow(
                            city       = city,
                            isSelected = selectedCity?.nameAr == city.nameAr,
                            onClick    = { onCitySelect(city) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedCity != null && results.isEmpty(),
                enter   = fadeIn() + expandVertically()
            ) {
                selectedCity?.let { city ->
                    Column {
                        Spacer(Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(0.45f)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(city.nameAr,
                                        fontWeight = FontWeight.SemiBold,
                                        style      = MaterialTheme.typography.bodyMedium)
                                    Text("${city.nameEn} · ${city.countryAr}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${city.distanceKm} ${stringResource(R.string.km)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (city.isTravelShafii) Color(0xFF2E7D32)
                                    else Color(0xFFC62828))
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(14.dp))) {
                            key(city.lat, city.lng) {
                                AndroidView(
                                    factory = { ctx ->
                                        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
                                        val mv = org.osmdroid.views.MapView(ctx).apply {
                                            setTileSource(TileSourceFactory.MAPNIK)
                                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                                            isClickable = false
                                            setMultiTouchControls(false)
                                            if (isDark) {
                                                overlayManager.tilesOverlay.setColorFilter(
                                                    org.osmdroid.views.overlay.TilesOverlay.INVERT_COLORS
                                                )
                                            }
                                            val dest   = GeoPoint(city.lat, city.lng)
                                            val marker = Marker(this).apply {
                                                position = dest
                                                title    = city.nameAr
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            }
                                            overlays.add(marker)
                                            controller.setZoom(10.0)
                                            controller.setCenter(dest)
                                        }
                                        mv
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CityResultRow(city: CityResult, isSelected: Boolean, onClick: () -> Unit) {
    val isSafar = city.isTravelShafii
    Surface(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        color    = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(0.35f)
        else Color.Transparent
    ) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).background(
                    if (isSafar) Color(0xFF2E7D32).copy(0.10f) else Color(0xFFC62828).copy(0.08f),
                    CircleShape
                ), Alignment.Center
            ) {
                Text(
                    if (!isSafar) "🚗"
                    else if (city.isLocalTransport) "🚂"
                    else "✈️",
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(city.nameAr,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("${city.nameEn} · ${city.countryAr}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${city.distanceKm} ${stringResource(R.string.km)}",
                    fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = if (isSafar) Color(0xFF2E7D32) else Color(0xFFC62828))
                Text(if (isSafar) stringResource(R.string.travel_sharia_distance) else stringResource(R.string.travel_not_sharia_distance),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSafar) Color(0xFF2E7D32) else Color(0xFFC62828))
            }
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 4.dp))
}

// ══════════════════════════════════════════════════════════════════════════════
//  SHAFI'I FIQH CARD
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ShafiiFiqhCard(
    fiqh                   : FiqhResult,
    selectedCity           : CityResult?,
    userCity               : String,
    showDetails            : Boolean,
    isActive               : Boolean,
    showChangeDestDialog   : Boolean,
    onToggleDetails        : () -> Unit,
    onActivate             : () -> Unit,
    onDeactivate           : () -> Unit,
    onChangeDestination    : () -> Unit,
    onShowChangeDestDialog : () -> Unit,
    onDismissChangeDialog  : () -> Unit
) {
    val isSafar   = fiqh.isSafar
    val mainColor = if (isSafar) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val bgColor   = if (isSafar) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val isDark    = MaterialTheme.colorScheme.background.luminance() < 0.2f

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDark) mainColor.copy(0.15f) else bgColor
        )
    ) {
        Column(Modifier.padding(18.dp)) {

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(mainColor.copy(0.14f), CircleShape),
                        Alignment.Center) {
                        Text(
                            when {
                                !isSafar -> "🏠"
                                selectedCity?.isLocalTransport == true -> "🚂"
                                else -> "✈️"
                            },
                            fontSize = 22.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(if (isSafar) stringResource(R.string.travel_sharia) else stringResource(R.string.travel_not_sharia), // need plain resources
                            fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = mainColor)
                        Text(stringResource(R.string.shafii_madhab),
                            style = MaterialTheme.typography.bodySmall,
                            color = mainColor.copy(0.70f))
                    }
                }
                DistanceMeter(fiqh.distanceKm, SHAFII_TRAVEL_KM, mainColor)
            }

            if (selectedCity != null) {
                Spacer(Modifier.height(12.dp))
                RouteRow(from = userCity, to = selectedCity.nameAr, color = mainColor)
            }

            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = mainColor.copy(0.10f)) {
                Text(
                    if (isSafar) stringResource(R.string.rulings_permitted_summary)
                    else stringResource(R.string.rulings_not_permitted_summary, fiqh.distanceKm, SHAFII_TRAVEL_KM.toInt()),
                    Modifier.fillMaxWidth().padding(12.dp),
                    fontWeight = FontWeight.Bold, color = mainColor, textAlign = TextAlign.Center
                )
            }

            if (isSafar) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "🧎" to stringResource(R.string.rulings_qasr_label),
                        "🔗" to stringResource(R.string.rulings_jam_label),
                        "🌴" to stringResource(R.string.rulings_fitr_label),
                        "💧" to stringResource(R.string.rulings_mash_label)
                    ).forEach { (emoji, label) ->
                        Surface(
                            shape    = RoundedCornerShape(10.dp),
                            color    = mainColor.copy(0.10f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, fontSize = 14.sp)
                                Text(label,
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = mainColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onToggleDetails, Modifier.fillMaxWidth()) {
                Text(if (showDetails) stringResource(R.string.hide_details) else stringResource(R.string.show_details), color = mainColor)
                Icon(if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, Modifier.size(18.dp), tint = mainColor)
            }

            AnimatedVisibility(visible = showDetails, enter = fadeIn() + expandVertically()) {
                Column {
                    HorizontalDivider(color = mainColor.copy(0.20f))
                    Spacer(Modifier.height(8.dp))
                    fiqh.details.forEach { line ->
                        Text(line,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 3.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF8E1)) {
                        Row(Modifier.padding(10.dp)) {
                            Icon(Icons.Default.Info, null,
                                tint     = Color(0xFFF57F17),
                                modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.distance_is_approx),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE65100))
                        }
                    }
                }
            }

            if (isSafar && selectedCity != null) {
                Spacer(Modifier.height(10.dp))

                if (isActive) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick  = onShowChangeDestDialog,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = mainColor)
                        ) {
                            Icon(Icons.Default.SyncAlt, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.change_destination), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick  = onDeactivate,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                            border   = BorderStroke(1.dp, Color(0xFFC62828))
                        ) {
                            Icon(Icons.Default.FlightLand, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.deactivate_travel), maxLines = 1)
                        }
                    }
                } else {
                    Button(
                        onClick  = onActivate,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = mainColor)
                    ) {
                        Icon(Icons.Default.FlightTakeoff, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.activate_travel, selectedCity.nameAr))
                    }
                }

                if (showChangeDestDialog) {
                    AlertDialog(
                        onDismissRequest = onDismissChangeDialog,
                        icon  = { Text("✈️", fontSize = 28.sp) },
                        title = {
                            Text(stringResource(R.string.change_destination),
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center,
                                modifier   = Modifier.fillMaxWidth())
                        },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier            = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.change_destination_confirm, selectedCity.nameAr),
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center)
                                Spacer(Modifier.height(8.dp))
                                Surface(shape = RoundedCornerShape(10.dp), color = mainColor.copy(0.10f)) {
                                    Text(
                                        "${selectedCity.nameAr}  •  ${selectedCity.distanceKm} ${stringResource(R.string.km)}",
                                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Bold,
                                        color      = mainColor,
                                        textAlign  = TextAlign.Center
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(stringResource(R.string.change_destination_confirm_note),
                                    style     = MaterialTheme.typography.labelSmall,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = onChangeDestination,
                                colors  = ButtonDefaults.buttonColors(containerColor = mainColor),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.SyncAlt, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismissChangeDialog) {
                                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  DISTANCE METER
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DistanceMeter(distanceKm: Int, minKm: Double, color: Color) {
    val progress = (distanceKm / minKm).coerceIn(0.0, 1.0).toFloat()
    val animProg by animateFloatAsState(progress, tween(900, easing = FastOutSlowInEasing), label = "dm")
    Box(Modifier.size(58.dp), Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 4f
            drawCircle(color.copy(0.12f), r, style = Stroke(5.5f))
            drawArc(
                color.copy(if (animProg >= 1f) 1f else 0.70f),
                -90f, animProg * 360f, false,
                Offset(center.x - r, center.y - r),
                androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(5.5f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(distanceKm.toString(),
                fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color)
            Text(stringResource(R.string.km), fontSize = 8.sp, color = color.copy(0.65f))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ROUTE ROW
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RouteRow(from: String, to: String, color: Color) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment       = Alignment.CenterVertically,
        horizontalArrangement   = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MyLocation, null, tint = color, modifier = Modifier.size(16.dp))
            Text(from,
                style     = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color     = color,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center)
            Text(stringResource(R.string.your_current_location),
                style     = MaterialTheme.typography.labelSmall,
                color     = color.copy(0.6f),
                textAlign = TextAlign.Center)
        }
        Row(Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = color.copy(0.30f))
            Icon(Icons.Default.FlightTakeoff, null, tint = color, modifier = Modifier.size(18.dp))
            HorizontalDivider(Modifier.weight(1f), color = color.copy(0.30f))
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Place, null, tint = color, modifier = Modifier.size(16.dp))
            Text(to,
                style     = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color     = color,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center)
            Text(stringResource(R.string.destination),
                style     = MaterialTheme.typography.labelSmall,
                color     = color.copy(0.6f),
                textAlign = TextAlign.Center)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SECTION TITLE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(subtitle,
                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style      = MaterialTheme.typography.labelSmall,
                color      = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  TRAVEL STATUS CARD
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TravelStatusCard(
    destination: String,
    userCity   : String,
    timeDiff   : String,
    distance   : Int,
    duration   : String,
    onToggle   : () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box {
            Box(
                Modifier.fillMaxWidth().height(190.dp).background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF01579B), Color(0xFF0288D1), Color(0xFF29B6F6).copy(0.7f))
                    )
                )
            )
            Column(Modifier.padding(20.dp)) {

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        if (distance in 1..<LOCAL_TRANSPORT_KM) stringResource(R.string.travel_status_active_train)
                        else stringResource(R.string.travel_status_active_plane),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White)
                    Switch(
                        checked         = true,
                        onCheckedChange = { onToggle() },
                        colors          = SwitchDefaults.colors(
                            checkedTrackColor = Color.White.copy(0.3f),
                            checkedThumbColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(0.15f)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MyLocation, null,
                                tint     = Color.White.copy(0.85f),
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(userCity.ifBlank { stringResource(R.string.your_current_location) },
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                textAlign  = TextAlign.Center)
                            Text(stringResource(R.string.travel_status_departure),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.65f))
                        }

                        Column(Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(0.4f))
                                Icon(Icons.Default.FlightTakeoff, null,
                                    tint     = Color.White,
                                    modifier = Modifier.size(20.dp))
                                HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(0.4f))
                            }
                            Text(if (distance > 0) "$distance ${stringResource(R.string.km)}" else "—",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color.White.copy(0.85f),
                                fontWeight = FontWeight.SemiBold)
                        }

                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Place, null,
                                tint     = Color.White.copy(0.85f),
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(destination.ifBlank { stringResource(R.string.destination) },
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                textAlign  = TextAlign.Center)
                            Text(stringResource(R.string.travel_status_destination),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.65f))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    TravelChip(Icons.Default.Schedule,
                        if (timeDiff == "0" || timeDiff.isBlank()) stringResource(R.string.same_time) else "+${timeDiff}${stringResource(R.string.hours)}",
                        stringResource(R.string.time_difference))
                    TravelChip(Icons.Default.AccessTime, duration.ifBlank { "—" }, stringResource(R.string.duration))
                    TravelChip(Icons.Default.CheckCircle, stringResource(R.string.active), stringResource(R.string.status))
                }
            }
        }
    }
}

@Composable
private fun TravelChip(
    icon : androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(0.85f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(2.dp))
        Text(value,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            maxLines   = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  PRAYER CARD
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun PrayerCard(prayer: TravelPrayer) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (prayer.isShortened) prayer.color.copy(0.06f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).background(prayer.color.copy(0.15f), CircleShape),
                    Alignment.Center) {
                    Icon(prayer.icon, null, tint = prayer.color, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(prayer.name,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text(prayer.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (prayer.canMerge && prayer.mergeWith != null) {
                        Text(stringResource(R.string.combined_with, prayer.mergeWith),
                            style = MaterialTheme.typography.labelSmall,
                            color = prayer.color.copy(0.80f))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(8.dp), color = prayer.color.copy(0.15f)) {
                    Text("${prayer.rakaat} ${stringResource(R.string.rakats)}",
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style      = MaterialTheme.typography.labelMedium,
                        color      = prayer.color,
                        fontWeight = FontWeight.Bold)
                }
                if (prayer.isShortened) {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.shortened),
                        style = MaterialTheme.typography.labelSmall,
                        color = prayer.color)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  HELPERS
// ══════════════════════════════════════════════════════════════════════════════



@Composable
fun getDirectionText(degrees: Float): String = when {
    degrees >= 337.5 || degrees < 22.5  -> stringResource(R.string.north)
    degrees >= 22.5  && degrees < 67.5  -> stringResource(R.string.north_east)
    degrees >= 67.5  && degrees < 112.5 -> stringResource(R.string.east)
    degrees >= 112.5 && degrees < 157.5 -> stringResource(R.string.south_east)
    degrees >= 157.5 && degrees < 202.5 -> stringResource(R.string.south)
    degrees >= 202.5 && degrees < 247.5 -> stringResource(R.string.south_west)
    degrees >= 247.5 && degrees < 292.5 -> stringResource(R.string.west)
    else                                 -> stringResource(R.string.north_west)
}