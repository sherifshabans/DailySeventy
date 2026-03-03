package com.elsharif.dailyseventy.presentation.components.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsharif.dailyseventy.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.net.URL
import java.net.URLEncoder

// ─────────────────────────────────────────────────────────────────────────────
// Data class للنتائج
// ─────────────────────────────────────────────────────────────────────────────
data class SearchResult(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

// ─────────────────────────────────────────────────────────────────────────────
// MapView مع Search Bar فوقيه
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    currentLocation: GeoPoint? = null,
    onLoad: ((map: MapView) -> Unit)? = null,
    onMapClick: ((GeoPoint) -> Unit)? = null
) {
    val mapViewState = rememberMapViewWithLifecycle()
    val isDark = isSystemInDarkTheme()
    var zoomState by remember { mutableDoubleStateOf(12.0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ── Search State ──────────────────────────────────────────────────────────
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // الموقع الحالي للخريطة (يتغير عند السيرش أو الكليك)
    var activeLocation by remember { mutableStateOf(currentLocation) }

    // ─────────────────────────────────────────────────────────────────────────
    // دالة السيرش — Nominatim (مجاني بدون API key)
    // ─────────────────────────────────────────────────────────────────────────
    fun searchPlaces(query: String) {
        if (query.length < 2) {
            searchResults = emptyList()
            showResults = false
            return
        }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(600) // debounce
            isSearching = true
            try {
                val results = withContext(Dispatchers.IO) {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=6&accept-language=ar,en"
                    val response = URL(url).openConnection().apply {
                        setRequestProperty("User-Agent", BuildConfig.APPLICATION_ID)
                        connectTimeout = 8000
                        readTimeout = 8000
                    }.getInputStream().bufferedReader().readText()

                    val arr = JSONArray(response)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        SearchResult(
                            displayName = obj.getString("display_name"),
                            lat = obj.getDouble("lat"),
                            lon = obj.getDouble("lon")
                        )
                    }
                }
                searchResults = results
                showResults = results.isNotEmpty()
            } catch (e: Exception) {
                searchResults = emptyList()
                showResults = false
            } finally {
                isSearching = false
            }
        }
    }

    Column(modifier = modifier) {

        // ── Search Bar ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                // حقل البحث
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isDark) Color(0xFF1E1E2E) else Color.White
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            searchPlaces(it)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    // لا تخفي النتائج فوراً عشان المستخدم يقدر يضغط
                                }
                            },
                        textStyle = TextStyle(
                            color = if (isDark) Color.White else Color(0xFF1A1A2E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                searchPlaces(searchQuery)
                            }
                        ),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "ابحث عن مكان...",
                                    style = TextStyle(
                                        color = Color.Gray.copy(0.7f),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            inner()
                        }
                    )
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                searchResults = emptyList()
                                showResults = false
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // قائمة النتائج
                if (showResults && searchResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .background(if (isDark) Color(0xFF1E1E2E) else Color.White)
                            .heightIn(max = 200.dp)
                    ) {
                        LazyColumn {
                            items(searchResults) { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val point = GeoPoint(result.lat, result.lon)
                                            activeLocation = point
                                            onMapClick?.invoke(point)
                                            searchQuery = result.displayName
                                                .split(",")
                                                .take(2)
                                                .joinToString(", ")
                                            showResults = false
                                            focusManager.clearFocus()
                                            // تحريك الخريطة للموقع المختار
                                            mapViewState.controller.animateTo(point)
                                            mapViewState.controller.setZoom(14.0)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = result.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = if (isDark) Color.White.copy(0.9f) else Color(0xFF1A1A2E)
                                        ),
                                        maxLines = 2
                                    )
                                }
                                if (result != searchResults.last()) {
                                    HorizontalDivider(
                                        color = Color.Gray.copy(0.15f),
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── الخريطة ───────────────────────────────────────────────────────────
        androidx.compose.ui.viewinterop.AndroidView(
            { mapViewState },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { mapView ->
            Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
            mapView.controller.zoomTo(zoomState)
            onLoad?.invoke(mapView)

            if (isDark) {
                mapView.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
            } else {
                mapView.overlayManager.tilesOverlay.setColorFilter(null)
            }

            val mReceive: MapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    activeLocation = p
                    onMapClick?.invoke(p)
                    zoomState = mapView.zoomLevelDouble
                    // أخفي نتائج السيرش لو كانت ظاهرة
                    showResults = false
                    return true
                }

                override fun longPressHelper(p: GeoPoint) = false
            }

            val evOverlay = MapEventsOverlay(mReceive)

            // استخدم activeLocation (اللي بتتغير بالسيرش أو الكليك)
            val locationToShow = activeLocation ?: currentLocation
            locationToShow?.let {
                val locationMarker = Marker(mapView)
                locationMarker.position = it
                mapView.overlays.clear()
                mapView.overlays.add(evOverlay)
                mapView.overlays.add(locationMarker)
                mapView.controller.setCenter(it)
                mapView.controller.animateTo(it)
            } ?: run {
                mapView.overlays.clear()
                mapView.overlays.add(evOverlay)
            }
        }
    }
}