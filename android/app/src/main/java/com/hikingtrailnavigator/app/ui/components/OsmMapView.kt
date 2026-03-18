package com.hikingtrailnavigator.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hikingtrailnavigator.app.domain.model.LatLng
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

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

// ESRI World Imagery satellite tile source (free, covers India)
private val ESRI_SATELLITE = object : XYTileSource(
    "ESRI World Imagery",
    0, 19, 256, ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}$zoom/$y/$x"
    }
}

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    centerLat: Double = 13.0,
    centerLng: Double = 75.5,
    zoomLevel: Double = 10.0,
    cameraMoveKey: Int = 0,
    useSatellite: Boolean = false,
    showMyLocation: Boolean = false,
    markers: List<MapMarker> = emptyList(),
    polylines: List<MapPolyline> = emptyList(),
    circles: List<MapCircle> = emptyList(),
    onMapClick: ((LatLng) -> Unit)? = null
) {
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var lastCameraMoveKey by remember { mutableIntStateOf(cameraMoveKey) }
    var lastSatelliteState by remember { mutableStateOf(useSatellite) }

    DisposableEffect(Unit) {
        onDispose {
            myLocationOverlay?.disableMyLocation()
            myLocationOverlay?.disableFollowLocation()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(if (useSatellite) ESRI_SATELLITE else TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(zoomLevel)
                controller.setCenter(GeoPoint(centerLat, centerLng))

                setUseDataConnection(true)
                isTilesScaledToDpi = true

                if (showMyLocation) {
                    val mapViewRef = this
                    val locationProvider = GpsMyLocationProvider(ctx).apply {
                        locationUpdateMinTime = 3000
                        locationUpdateMinDistance = 3f
                    }
                    val locOverlay = MyLocationNewOverlay(locationProvider, mapViewRef).apply {
                        enableMyLocation()
                        enableFollowLocation()

                        // Custom blue dot
                        val dotSize = 48
                        val personBmp = Bitmap.createBitmap(dotSize, dotSize, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(personBmp)
                        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = AndroidColor.argb(60, 25, 118, 210)
                            style = Paint.Style.FILL
                        }
                        canvas.drawCircle(dotSize / 2f, dotSize / 2f, dotSize / 2f, outerPaint)
                        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = AndroidColor.rgb(25, 118, 210)
                            style = Paint.Style.FILL
                        }
                        canvas.drawCircle(dotSize / 2f, dotSize / 2f, dotSize / 3.5f, innerPaint)
                        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = AndroidColor.WHITE
                            style = Paint.Style.STROKE
                            strokeWidth = 4f
                        }
                        canvas.drawCircle(dotSize / 2f, dotSize / 2f, dotSize / 3.5f, borderPaint)
                        setPersonIcon(personBmp)
                        setDirectionIcon(personBmp)

                        runOnFirstFix {
                            val loc = myLocation
                            if (loc != null) {
                                mapViewRef.post {
                                    mapViewRef.controller.animateTo(loc, 18.0, 1000L)
                                }
                            }
                        }
                    }
                    overlays.add(locOverlay)
                    myLocationOverlay = locOverlay
                }

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
            // Switch tile source if satellite toggle changed
            if (useSatellite != lastSatelliteState) {
                lastSatelliteState = useSatellite
                mapView.setTileSource(if (useSatellite) ESRI_SATELLITE else TileSourceFactory.MAPNIK)
            }

            // Keep special overlays
            val locOverlay = mapView.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
            val clickOverlay = if (onMapClick != null) mapView.overlays.firstOrNull {
                it !is Marker && it !is Polyline && it !is Polygon && it !is MyLocationNewOverlay
            } else null

            mapView.overlays.clear()
            locOverlay?.let { mapView.overlays.add(it) }
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

            // Add circles
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
            markers.filter { it.title != "You are here" }.forEach { m ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(m.position.latitude, m.position.longitude)
                    title = m.title
                    snippet = m.snippet
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }

            // ONLY move camera when explicitly requested via cameraMoveKey change
            if (cameraMoveKey != lastCameraMoveKey) {
                lastCameraMoveKey = cameraMoveKey
                locOverlay?.disableFollowLocation()
                mapView.controller.animateTo(GeoPoint(centerLat, centerLng), zoomLevel, 500L)
            }

            mapView.invalidate()
        }
    )
}

fun LatLng.toGeoPoint() = GeoPoint(latitude, longitude)
