package com.hikingtrailnavigator.app.ui.screens.trails

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun TrailDetailScreen(
    onBack: () -> Unit,
    onStartHike: (String) -> Unit,
    viewModel: TrailDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trail = uiState.trail

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = trail?.name ?: "Trail Details", onBack = onBack)

        if (trail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Map preview (OpenStreetMap)
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    centerLat = trail.startPoint.latitude,
                    centerLng = trail.startPoint.longitude,
                    zoomLevel = 12.0,
                    markers = listOf(
                        MapMarker(position = trail.startPoint, title = "Start"),
                        MapMarker(position = trail.endPoint, title = "End")
                    ),
                    polylines = if (trail.coordinates.isNotEmpty()) {
                        listOf(
                            MapPolyline(
                                points = trail.coordinates,
                                color = AndroidColor.rgb(46, 125, 50),
                                width = 5f
                            )
                        )
                    } else emptyList()
                )
            }

            // Stats row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("Distance", "${trail.distance} km")
                    StatCard("Duration", trail.estimatedDuration)
                    StatCard("Elevation", "${trail.elevationGain}m")
                    StatCard("Rating", "${trail.rating}")
                }
            }

            // Difficulty & region
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DifficultyBadge(trail.difficulty)
                Text(trail.region, color = OnSurfaceVariant, fontSize = 14.sp)
            }

            // Description
            SectionTitle("Description")
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = trail.description,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                color = OnSurface
            )
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(if (expanded) "Show less" else "Read more")
            }

            // Elevation Profile
            SectionTitle("Elevation Profile")
            if (trail.elevationProfile.isNotEmpty()) {
                ElevationChart(
                    profile = trail.elevationProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            // Risk Assessment
            SectionTitle("Risk Assessment")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (uiState.riskLevel) {
                        "low" -> RiskLow.copy(alpha = 0.1f)
                        "medium" -> RiskMedium.copy(alpha = 0.1f)
                        "high" -> RiskHigh.copy(alpha = 0.1f)
                        else -> RiskCritical.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Risk Score", fontWeight = FontWeight.SemiBold)
                        RiskBadge(uiState.riskLevel)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uiState.riskScore / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = when (uiState.riskLevel) {
                            "low" -> RiskLow
                            "medium" -> RiskMedium
                            "high" -> RiskHigh
                            else -> RiskCritical
                        },
                        trackColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.riskFactors.forEach { factor ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("  \u2022  ", color = OnSurfaceVariant, fontSize = 13.sp)
                            Text(factor, fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // Hazards
            if (trail.hazards.isNotEmpty()) {
                SectionTitle("Known Hazards")
                trail.hazards.forEach { hazard ->
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Warning, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(hazard, fontSize = 14.sp)
                    }
                }
            }

            // Coverage status
            SectionTitle("Network Coverage")
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (trail.coverageStatus) {
                        com.hikingtrailnavigator.app.domain.model.CoverageStatus.Full -> Icons.Default.SignalCellular4Bar
                        com.hikingtrailnavigator.app.domain.model.CoverageStatus.Partial -> Icons.Default.SignalCellularAlt
                        com.hikingtrailnavigator.app.domain.model.CoverageStatus.None -> Icons.Default.SignalCellularOff
                    },
                    null,
                    tint = when (trail.coverageStatus) {
                        com.hikingtrailnavigator.app.domain.model.CoverageStatus.Full -> RiskLow
                        com.hikingtrailnavigator.app.domain.model.CoverageStatus.Partial -> RiskMedium
                        com.hikingtrailnavigator.app.domain.model.CoverageStatus.None -> Danger
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${trail.coverageStatus.name} Coverage",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Hike button
            Button(
                onClick = { onStartHike(trail.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Start Hike", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ElevationChart(
    profile: List<com.hikingtrailnavigator.app.domain.model.ElevationPoint>,
    modifier: Modifier = Modifier
) {
    val color = Primary
    Canvas(modifier = modifier) {
        if (profile.isEmpty()) return@Canvas
        val maxElev = profile.maxOf { it.elevation }.toFloat()
        val minElev = profile.minOf { it.elevation }.toFloat()
        val maxDist = profile.maxOf { it.distance }.toFloat()
        val range = (maxElev - minElev).coerceAtLeast(1f)

        val path = Path()
        profile.forEachIndexed { index, point ->
            val x = (point.distance.toFloat() / maxDist) * size.width
            val y = size.height - ((point.elevation - minElev) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, color, style = Stroke(width = 3f))
    }
}
