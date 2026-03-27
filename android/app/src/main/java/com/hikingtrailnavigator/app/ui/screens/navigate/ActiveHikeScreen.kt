package com.hikingtrailnavigator.app.ui.screens.navigate

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun ActiveHikeScreen(
    onBack: () -> Unit,
    onHikeComplete: () -> Unit,
    viewModel: ActiveHikeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trail = uiState.trail

    // Show loading until trail data is available
    if (trail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
                Spacer(Modifier.height(12.dp))
                Text("Loading trail...", color = Color(0xFF757575))
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map follows user's current GPS location
        key(trail.id) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                centerLat = trail.startPoint.latitude,
                centerLng = trail.startPoint.longitude,
                zoomLevel = 16.0,
                useSatellite = true,
                showMyLocation = true,
                followMyLocation = true,
            markers = buildList {
                // Danger zone center markers
                uiState.allDangerZones.forEach { zone ->
                    add(MapMarker(
                        position = zone.center,
                        title = zone.name,
                        snippet = "${zone.severity} - ${zone.description}",
                        color = AndroidColor.rgb(211, 47, 47)
                    ))
                }
            },
            polylines = buildList {
                // User's actual walked path in ORANGE
                if (uiState.route.isNotEmpty()) {
                    add(MapPolyline(
                        points = uiState.route,
                        color = AndroidColor.rgb(255, 111, 0),
                        width = 5f
                    ))
                }
            },
            circles = buildList {
                // All danger zones as red circles
                uiState.allDangerZones.forEach { zone ->
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
        } // end key(trail.id)

        // Stats bar at top (FR-102: distance remaining, ETA, pace)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = trail.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HikeStat("Time", formatDuration(uiState.elapsedTime))
                    HikeStat("Distance", String.format("%.2f km", uiState.distanceTraveled))
                    HikeStat("Elevation", "${uiState.elevationGained}m")
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HikeStat("Remaining", String.format("%.1f km", uiState.distanceRemaining))
                    HikeStat("ETA", uiState.estimatedTimeRemaining)
                    HikeStat("Pace", uiState.avgPace)
                }
            }
        }

        // Deviation warning (FR-205)
        if (uiState.isDeviating) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 170.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(containerColor = DangerLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Danger)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "You are ${uiState.deviationDistance.toInt()}m off trail!",
                        color = Danger,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // No-coverage zone indicator
        if (uiState.insideNoCoverageZone) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = if (uiState.isDeviating) 220.dp else 170.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(containerColor = WarningLight)
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SignalCellularOff, null, tint = Warning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("No network coverage", fontSize = 13.sp, color = Warning)
                }
            }
        }

        // Map controls (satellite toggle)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { viewModel.toggleSatellite() },
                containerColor = Color.White,
                contentColor = Color(0xFF424242)
            ) {
                Icon(
                    if (uiState.useSatellite) Icons.Default.Map else Icons.Default.Satellite,
                    contentDescription = "Toggle map type"
                )
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
                    Box(modifier = Modifier.size(12.dp, 4.dp).padding(0.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color(0xFFFF6F00))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Your path", fontSize = 9.sp, color = Color(0xFF424242))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).padding(0.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFFD32F2F))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Danger zone", fontSize = 9.sp, color = Color(0xFF424242))
                }
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = viewModel::togglePause,
                containerColor = if (uiState.isPaused) Primary else Secondary
            ) {
                Icon(
                    if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    null, tint = Color.White
                )
            }
            FloatingActionButton(
                onClick = viewModel::showEndHikeDialog,
                containerColor = Danger,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Stop, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            FloatingActionButton(
                onClick = { /* Navigate to SOS */ },
                containerColor = Danger.copy(alpha = 0.8f)
            ) {
                Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // ===== DIALOGS =====

        // Safety Check-In Dialog (FR-201)
        if (uiState.showCheckInDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        if (uiState.checkInEscalationLevel > 0) "URGENT: Safety Check-In"
                        else "Safety Check-In"
                    )
                },
                text = {
                    Column {
                        Text(
                            if (uiState.checkInEscalationLevel >= 2)
                                "You have not responded! Emergency contacts are being notified."
                            else if (uiState.checkInEscalationLevel == 1)
                                "You missed your check-in! Please respond immediately or emergency contacts will be notified."
                            else
                                "Are you doing okay? Please confirm you're safe."
                        )
                        if (uiState.checkInEscalationLevel > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Escalation level: ${uiState.checkInEscalationLevel}/2",
                                color = Danger,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::acknowledgeCheckIn,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("I'm Safe")
                    }
                }
            )
        }

        // Fall Detection Dialog (FR-203)
        if (uiState.showFallDetectedDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("Fall Detected!", color = Danger)
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("A possible fall was detected. Are you okay?")
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${uiState.fallCountdown}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Danger
                        )
                        Text(
                            "seconds until auto-SOS",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::dismissFallAlert,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("I'm Fine")
                    }
                }
            )
        }

        // Danger Zone Alert Dialog (FR-208)
        if (uiState.showDangerZoneAlert && uiState.insideDangerZone != null) {
            val zone = uiState.insideDangerZone!!
            AlertDialog(
                onDismissRequest = viewModel::dismissDangerZoneAlert,
                icon = { Icon(Icons.Default.Warning, null, tint = Danger, modifier = Modifier.size(40.dp)) },
                title = { Text("Danger Zone!", color = Danger) },
                text = {
                    Column {
                        Text(zone.name, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Type: ${zone.type.name}", fontSize = 14.sp)
                        Text("Severity: ${zone.severity.name}", fontSize = 14.sp, color = Danger)
                        Spacer(Modifier.height(8.dp))
                        Text(zone.description, fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::dismissDangerZoneAlert,
                        colors = ButtonDefaults.buttonColors(containerColor = Danger)
                    ) {
                        Text("I Understand")
                    }
                }
            )
        }

        // No-Coverage Zone Alert (FR-209)
        if (uiState.showNoCoverageAlert) {
            AlertDialog(
                onDismissRequest = viewModel::dismissNoCoverageAlert,
                icon = { Icon(Icons.Default.SignalCellularOff, null, tint = Warning, modifier = Modifier.size(40.dp)) },
                title = { Text("No Network Coverage") },
                text = {
                    Text("You are entering an area with no cellular network. " +
                         "Safety features will continue to work offline. " +
                         "SOS alerts will be sent once connectivity is restored.")
                },
                confirmButton = {
                    Button(onClick = viewModel::dismissNoCoverageAlert) {
                        Text("Got It")
                    }
                }
            )
        }

        // End hike confirmation
        if (uiState.showEndDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissEndDialog,
                title = { Text("End Hike?") },
                text = {
                    Column {
                        Text("Hike Summary:")
                        Spacer(Modifier.height(8.dp))
                        Text("Distance: ${String.format("%.2f km", uiState.distanceTraveled)}")
                        Text("Duration: ${formatDuration(uiState.elapsedTime)}")
                        Text("Elevation gain: ${uiState.elevationGained}m")
                        Text("Avg pace: ${uiState.avgPace}")
                        Text("Check-ins: ${uiState.checkInsCompleted}")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.endHike(onHikeComplete) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("End Hike")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissEndDialog) {
                        Text("Continue")
                    }
                }
            )
        }
    }
}

@Composable
private fun HikeStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary)
        Text(label, fontSize = 10.sp, color = OnSurfaceVariant)
    }
}
