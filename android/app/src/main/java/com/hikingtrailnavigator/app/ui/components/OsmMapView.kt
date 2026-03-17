package com.hikingtrailnavigator.app.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hikingtrailnavigator.app.domain.model.LatLng
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

data class MapMarker(
    val position: LatLng,
    val title: String = "",
    val snippet: String = "",
    val color: Int = AndroidColor.RED
)

data class MapPolyline(
    val points: List<LatLng>,
    val color: Int = AndroidColor.BLUE,
    val width: Float = 5f
)

data class MapCircle(
    val center: LatLng,
    val radiusMeters: Double,
    val fillColor: Int = AndroidColor.argb(50, 255, 0, 0),
    val strokeColor: Int = AndroidColor.RED,
    val strokeWidth: Float = 2f
)

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    centerLat: Double = 13.0,
    centerLng: Double = 75.5,
    zoomLevel: Double = 10.0,
    markers: List<MapMarker> = emptyList(),
    polylines: List<MapPolyline> = emptyList(),
    circles: List<MapCircle> = emptyList(),
    onMapClick: ((LatLng) -> Unit)? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(zoomLevel)
                controller.setCenter(GeoPoint(centerLat, centerLng))

                // Enable offline tile caching
                setUseDataConnection(true)
                isTilesScaledToDpi = true

                if (onMapClick != null) {
                    val clickOverlay = object : org.osmdroid.views.overlay.Overlay() {
                        override fun onSingleTapConfirmed(
                            e: android.view.MotionEvent?,
                            mapView: MapView?
                        ): Boolean {
                            e ?: return false
                            mapView ?: return false
                            val proj = mapView.projection
                            val geoPoint = proj.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                            onMapClick(LatLng(geoPoint.latitude, geoPoint.longitude))
                            return true
                        }
                    }
                    overlays.add(clickOverlay)
                }
            }
        },
        update = { mapView ->
            // Clear previous overlays but keep the click overlay if present
            val clickOverlay = if (onMapClick != null) mapView.overlays.firstOrNull {
                it !is Marker && it !is Polyline && it !is Polygon
            } else null
            mapView.overlays.clear()
            clickOverlay?.let { mapView.overlays.add(it) }

            // Add polylines
            polylines.forEach { line ->
                val polyline = Polyline().apply {
                    outlinePaint.color = line.color
                    outlinePaint.strokeWidth = line.width
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                    setPoints(line.points.map { GeoPoint(it.latitude, it.longitude) })
                }
                mapView.overlays.add(polyline)
            }

            // Add circles (danger zones, no-coverage zones)
            circles.forEach { circle ->
                val polygon = Polygon().apply {
                    points = Polygon.pointsAsCircle(
                        GeoPoint(circle.center.latitude, circle.center.longitude),
                        circle.radiusMeters
                    )
                    fillPaint.color = circle.fillColor
                    outlinePaint.color = circle.strokeColor
                    outlinePaint.strokeWidth = circle.strokeWidth
                }
                mapView.overlays.add(polygon)
            }

            // Add markers
            markers.forEach { m ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(m.position.latitude, m.position.longitude)
                    title = m.title
                    snippet = m.snippet
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }

            // Update camera
            mapView.controller.setCenter(GeoPoint(centerLat, centerLng))
            mapView.controller.setZoom(zoomLevel)
            mapView.invalidate()
        }
    )
}

fun LatLng.toGeoPoint() = GeoPoint(latitude, longitude)
