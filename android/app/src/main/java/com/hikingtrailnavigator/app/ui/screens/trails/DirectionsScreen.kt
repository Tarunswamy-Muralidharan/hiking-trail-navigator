package com.hikingtrailnavigator.app.ui.screens.trails

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.repository.TrailRepository
import com.hikingtrailnavigator.app.domain.model.DangerZone
import com.hikingtrailnavigator.app.domain.model.Trail
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DirectionsUiState(
    val trail: Trail? = null,
    val dangerZones: List<DangerZone> = emptyList()
)

@HiltViewModel
class DirectionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trailRepository: TrailRepository
) : ViewModel() {

    private val trailId: String = savedStateHandle["trailId"] ?: ""

    private val _uiState = MutableStateFlow(DirectionsUiState())
    val uiState: StateFlow<DirectionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val trail = trailRepository.getTrailById(trailId)
            _uiState.update { it.copy(trail = trail) }
        }
        viewModelScope.launch {
            trailRepository.getDangerZones().collect { zones ->
                _uiState.update { it.copy(dangerZones = zones) }
            }
        }
    }
}

@Composable
fun DirectionsScreen(
    onBack: () -> Unit,
    onStartHike: (String) -> Unit,
    viewModel: DirectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trail = uiState.trail

    if (trail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
                Spacer(Modifier.height(12.dp))
                Text("Loading directions...", color = Color(0xFF757575))
            }
        }
        return
    }

    val trailCenterLat = (trail.startPoint.latitude + trail.endPoint.latitude) / 2
    val trailCenterLng = (trail.startPoint.longitude + trail.endPoint.longitude) / 2

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen satellite map centered on trail
        key(trail.id) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                centerLat = trailCenterLat,
                centerLng = trailCenterLng,
                zoomLevel = 13.5,
                useSatellite = true,
                showMyLocation = true,
                followMyLocation = false,
                markers = buildList {
                    // Trail start/end markers
                    add(MapMarker(
                        position = trail.startPoint,
                        title = "Start",
                        snippet = "Trail start point",
                        color = AndroidColor.rgb(0, 200, 83)
                    ))
                    if (trail.startPoint != trail.endPoint) {
                        add(MapMarker(
                            position = trail.endPoint,
                            title = "End",
                            snippet = "Trail end point",
                            color = AndroidColor.RED
                        ))
                    }
                    // Danger zone markers
                    uiState.dangerZones.forEach { zone ->
                        add(MapMarker(
                            position = zone.center,
                            title = zone.name,
                            snippet = "${zone.severity} - ${zone.description}",
                            color = AndroidColor.rgb(211, 47, 47)
                        ))
                    }
                },
                polylines = if (trail.coordinates.isNotEmpty()) {
                    listOf(
                        // Outer glow
                        MapPolyline(
                            points = trail.coordinates,
                            color = AndroidColor.argb(100, 0, 200, 83),
                            width = 16f
                        ),
                        // Inner bright green safe route
                        MapPolyline(
                            points = trail.coordinates,
                            color = AndroidColor.rgb(0, 200, 83),
                            width = 7f
                        )
                    )
                } else emptyList(),
                circles = buildList {
                    // Danger zone circles
                    uiState.dangerZones.forEach { zone ->
                        add(MapCircle(
                            center = zone.center,
                            radiusMeters = zone.radius,
                            fillColor = AndroidColor.argb(60, 211, 47, 47),
                            strokeColor = AndroidColor.rgb(211, 47, 47),
                            strokeWidth = 2.5f
                        ))
                    }
                }
            )
        }

        // Top bar with trail name and back button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF424242))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            trail.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${trail.distance} km  |  ${trail.estimatedDuration}  |  ${trail.elevationGain}m gain",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }

        // "Safe Route" badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 90.dp),
            color = Color(0xFF00C853),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Route, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Safe Route", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Legend card
        Card(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp, 4.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color(0xFF00C853))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Safe route", fontSize = 9.sp, color = Color(0xFF424242))
                }
                if (uiState.dangerZones.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Color(0xFFD32F2F))
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Danger zone", fontSize = 9.sp, color = Color(0xFF424242))
                    }
                }
            }
        }

        // Bottom: Start Hike button
        Button(
            onClick = { onStartHike(trail.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp)
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text("Start Hike", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
