package com.elsharif.dailyseventy.presentation.components.maps

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.elsharif.dailyseventy.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

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
    AndroidView(
        { mapViewState },
        modifier
    ) { mapView ->





        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID;
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        mapView.controller.zoomTo(zoomState)
        onLoad?.invoke(mapView)
        if (isDark) {
            mapView.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS);
        } else {
            mapView.overlayManager.tilesOverlay.setColorFilter(null);
        }
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                onMapClick?.invoke(p)
                zoomState = mapView.zoomLevelDouble
                return true
            }

            override fun longPressHelper(p: GeoPoint) = false
        }


        val evOverlay = MapEventsOverlay(mReceive)
        mapView.overlays.add(evOverlay)

        currentLocation?.let {
            val locationMarker = Marker(mapView)
            locationMarker.position = it
            mapView.overlays.clear()
            mapView.overlays.add(evOverlay)
            mapView.overlays.add(locationMarker)
            mapView.controller.setCenter(it)
            mapView.controller.animateTo(it)
        }
    }
}
