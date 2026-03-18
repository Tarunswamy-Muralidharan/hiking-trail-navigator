package com.hikingtrailnavigator.app.ui.screens.home

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun HomeScreen(
    onTrailClick: (String) -> Unit,
    onSosClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // OpenStreetMap
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            centerLat = uiState.mapCenterLat,
            centerLng = uiState.mapCenterLng,
            zoomLevel = uiState.mapZoom,
            showMyLocation = true,
            useSatellite = uiState.useSatellite,
            cameraMoveKey = uiState.cameraMoveKey,
            markers = buildList {
                // Trail markers
                addAll(uiState.trails.map { trail ->
                    MapMarker(
                        position = trail.startPoint,
                        title = trail.name,
                        snippet = "${trail.difficulty.name} - ${trail.distance}km"
                    )
                })
                // Selected place marker (blue)
                uiState.selectedPlace?.let { place ->
                    add(
                        MapMarker(
                            position = place.location,
                            title = place.name,
                            snippet = place.subtitle,
                            color = AndroidColor.rgb(33, 150, 243)
                        )
                    )
                }
                // Hazard markers
                if (uiState.showHazards) {
                    addAll(uiState.hazardReports.map { hazard ->
                        MapMarker(
                            position = com.hikingtrailnavigator.app.domain.model.LatLng(
                                hazard.latitude, hazard.longitude
                            ),
                            title = hazard.type,
                            snippet = "${hazard.severity} - ${hazard.description}",
                            color = AndroidColor.rgb(245, 127, 23)
                        )
                    })
                }
                // Danger zone center markers (red, with zone name)
                if (uiState.showDangerZones) {
                    addAll(uiState.dangerZones.map { zone ->
                        MapMarker(
                            position = zone.center,
                            title = zone.name,
                            snippet = "${zone.severity} - ${zone.description}",
                            color = AndroidColor.rgb(211, 47, 47)
                        )
                    })
                }
            },
            polylines = uiState.trails.map { trail ->
                MapPolyline(
                    points = trail.coordinates,
                    color = AndroidColor.rgb(46, 125, 50), // green safe path
                    width = 6f
                )
            },
            circles = buildList {
                if (uiState.showDangerZones) {
                    addAll(uiState.dangerZones.map { zone ->
                        MapCircle(
                            center = zone.center, radiusMeters = zone.radius,
                            fillColor = AndroidColor.argb(50, 211, 47, 47),
                            strokeColor = AndroidColor.rgb(211, 47, 47)
                        )
                    })
                }
                if (uiState.showNoCoverageZones) {
                    addAll(uiState.noCoverageZones.map { zone ->
                        MapCircle(
                            center = zone.center, radiusMeters = zone.radius,
                            fillColor = AndroidColor.argb(50, 117, 117, 117),
                            strokeColor = AndroidColor.rgb(117, 117, 117)
                        )
                    })
                }
                if (uiState.showUnexplored) {
                    addAll(uiState.unexploredAreas.map { area ->
                        MapCircle(
                            center = area.center, radiusMeters = area.radius,
                            fillColor = AndroidColor.argb(40, 156, 39, 176),
                            strokeColor = AndroidColor.rgb(156, 39, 176),
                            strokeWidth = 1.5f
                        )
                    })
                }
            }
        )

        // Offline banner
        if (uiState.isOffline) {
            Card(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(containerColor = WarningLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiOff, null, tint = Warning, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Offline - using cached maps", fontSize = 12.sp, color = Warning)
                }
            }
        }

        // Search bar + suggestions overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (uiState.isOffline) 48.dp else 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search places, trails...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Search suggestions dropdown
            if (uiState.showSuggestions && uiState.searchSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column {
                        uiState.searchSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSuggestionSelected(suggestion) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (suggestion.trailId != null) Icons.Default.Terrain
                                    else Icons.Default.LocationOn,
                                    null,
                                    tint = if (suggestion.trailId != null) Primary else Color(0xFF1565C0),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        suggestion.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        suggestion.subtitle,
                                        fontSize = 12.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            if (suggestion != uiState.searchSuggestions.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0xFFEEEEEE)
                                )
                            }
                        }
                    }
                }
            }

            // Layer toggle chips (only show when no suggestions visible)
            if (!uiState.showSuggestions) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = uiState.showDangerZones,
                        onClick = viewModel::toggleDangerZones,
                        label = { Text("Danger", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Warning, null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DangerLight)
                    )
                    FilterChip(
                        selected = uiState.showNoCoverageZones,
                        onClick = viewModel::toggleNoCoverageZones,
                        label = { Text("Coverage", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.SignalCellularOff, null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = uiState.showHazards,
                        onClick = viewModel::toggleHazards,
                        label = { Text("Hazards", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.ReportProblem, null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarningLight)
                    )
                    FilterChip(
                        selected = uiState.showUnexplored,
                        onClick = viewModel::toggleUnexplored,
                        label = { Text("Unexplored", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Explore, null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }

        // Risk level badge
        if (!uiState.showSuggestions) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 120.dp, end = 16.dp)
            ) {
                RiskBadge(uiState.riskLevel)
            }
        }

        // Selected place card with Start Hike option
        val selectedPlace = uiState.selectedPlace
        if (selectedPlace != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 72.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                if (selectedPlace.trailId != null) Icons.Default.Terrain
                                else Icons.Default.LocationOn,
                                null,
                                tint = Primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    selectedPlace.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    selectedPlace.subtitle,
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
                        }
                    }

                    // GPS coordinates
                    Text(
                        "${String.format("%.4f", selectedPlace.location.latitude)}, ${String.format("%.4f", selectedPlace.location.longitude)}",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 38.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // If it's a trail, show trail info + start hike
                    if (selectedPlace.trailId != null) {
                        val trail = uiState.trails.find { it.id == selectedPlace.trailId }
                        if (trail != null) {
                            // Trail stats row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MiniStat("Distance", "${trail.distance}km")
                                MiniStat("Duration", trail.estimatedDuration)
                                MiniStat("Elevation", "+${trail.elevationGain}m")
                                MiniStat("Rating", "${trail.rating}")
                            }

                            // Schedule info
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (trail.schedule.contains("Closed"))
                                            Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule, null,
                                    tint = if (trail.schedule.contains("Closed"))
                                        Color(0xFFE65100) else Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    trail.schedule,
                                    fontSize = 12.sp,
                                    color = Color(0xFF424242),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // View details
                                OutlinedButton(
                                    onClick = { onTrailClick(trail.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Details")
                                }
                                // Start hike
                                Button(
                                    onClick = { onTrailClick(trail.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Start Hike")
                                }
                            }
                        }
                    } else {
                        // It's a place, show nearest trail
                        val nearestTrail = viewModel.getNearestTrail(selectedPlace.location)
                        if (nearestTrail != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F8FF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Nearest Trail", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(nearestTrail.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                            Text(
                                                "${nearestTrail.difficulty.name} - ${nearestTrail.distance}km - ${nearestTrail.estimatedDuration}",
                                                fontSize = 12.sp, color = OnSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onTrailClick(nearestTrail.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("View Trail")
                                }
                                Button(
                                    onClick = { onTrailClick(nearestTrail.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Start Hike")
                                }
                            }
                        }
                    }
                }
            }
        }

        // No-coverage zone warning (show when no place selected)
        if (selectedPlace == null) {
            val noCoverageWarning = uiState.noCoverageWarning
            if (noCoverageWarning != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 80.dp, bottom = 72.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SignalCellularOff, null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Low Network Area", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFFE65100))
                            Text(noCoverageWarning, fontSize = 11.sp, color = Color(0xFF795548))
                        }
                    }
                }
            }
        }

        // Map controls + SOS buttons
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Satellite / Map toggle
            SmallFloatingActionButton(
                onClick = { viewModel.toggleSatellite() },
                containerColor = Color.White,
                contentColor = Color(0xFF424242)
            ) {
                Icon(
                    if (uiState.useSatellite) Icons.Default.Map else Icons.Default.Satellite,
                    contentDescription = if (uiState.useSatellite) "Map view" else "Satellite view"
                )
            }

            // My Location button
            SmallFloatingActionButton(
                onClick = { viewModel.centerOnMyLocation() },
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }

            // SOS Button
            FloatingActionButton(
                onClick = onSosClick,
                containerColor = Danger
            ) {
                Text("SOS", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Trail count info (show when no place selected)
        if (selectedPlace == null && uiState.trails.isNotEmpty()) {
            Card(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Text(
                    text = "${uiState.trails.size} trails available",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Primary)
        Text(label, fontSize = 10.sp, color = OnSurfaceVariant)
    }
}
