package com.hikingtrailnavigator.app.ui.screens.safety

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun SafetyDashboardScreen(
    onSosClick: () -> Unit,
    onHazardReportClick: () -> Unit,
    onLiveTrackingClick: () -> Unit,
    onEmergencyContactsClick: () -> Unit,
    viewModel: SafetyDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Safety Center")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Connection status
            if (!uiState.isOnline) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WifiOff, null, tint = Warning)
                            Spacer(Modifier.width(8.dp))
                            Text("You are offline. Some features may be limited.", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Weather / Environmental Alerts (FR-206)
            uiState.weather?.let { weather ->
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (uiState.weatherRiskLevel) {
                                "high" -> DangerLight
                                "medium" -> WarningLight
                                else -> PrimaryContainer
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, null, tint = Primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Weather", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                RiskBadge(uiState.weatherRiskLevel)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${weather.temperature}°C", fontWeight = FontWeight.Bold)
                                    Text("Temp", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${weather.humidity}%", fontWeight = FontWeight.Bold)
                                    Text("Humidity", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${weather.windSpeed} km/h", fontWeight = FontWeight.Bold)
                                    Text("Wind", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${weather.rainProbability}%", fontWeight = FontWeight.Bold)
                                    Text("Rain", fontSize = 11.sp, color = OnSurfaceVariant)
                                }
                            }
                            weather.alerts.forEach { alert ->
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = Warning, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(alert.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(alert.description, fontSize = 12.sp, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // SOS Button - big and prominent
            item {
                Button(
                    onClick = onSosClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("SOS Emergency", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Tap for immediate help", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Quick actions grid
            item {
                SectionTitle("Quick Actions")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SafetyActionCard(
                        icon = Icons.Default.ReportProblem,
                        title = "Report Hazard",
                        subtitle = "${uiState.recentHazards.size} recent",
                        color = Warning,
                        onClick = onHazardReportClick,
                        modifier = Modifier.weight(1f)
                    )
                    SafetyActionCard(
                        icon = Icons.Default.LocationOn,
                        title = "Live Tracking",
                        subtitle = "Share location",
                        color = Primary,
                        onClick = onLiveTrackingClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SafetyActionCard(
                        icon = Icons.Default.Contacts,
                        title = "Emergency Contacts",
                        subtitle = "${uiState.emergencyContactCount} saved",
                        color = PrimaryDark,
                        onClick = onEmergencyContactsClick,
                        modifier = Modifier.weight(1f)
                    )
                    SafetyActionCard(
                        icon = Icons.Default.Call,
                        title = "Call 112",
                        subtitle = "Emergency line",
                        color = Danger,
                        onClick = { /* Direct call handled in SOS */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Danger Zones
            if (uiState.dangerZones.isNotEmpty()) {
                item { SectionTitle("Danger Zones") }
                items(uiState.dangerZones.take(3)) { zone ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Danger)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(zone.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${zone.type.name} - ${zone.description}",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            SeverityBadge(zone.severity)
                        }
                    }
                }
            }

            // No Coverage Zones
            if (uiState.noCoverageZones.isNotEmpty()) {
                item { SectionTitle("No Coverage Areas") }
                items(uiState.noCoverageZones.take(3)) { zone ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SignalCellularOff, null, tint = OnSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(zone.name, fontWeight = FontWeight.SemiBold)
                                Text(zone.description, fontSize = 13.sp, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Recent Hazard Reports
            if (uiState.recentHazards.isNotEmpty()) {
                item { SectionTitle("Recent Hazard Reports") }
                items(uiState.recentHazards) { hazard ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ReportProblem, null, tint = Warning)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(hazard.type, fontWeight = FontWeight.SemiBold)
                                Text(hazard.description, fontSize = 13.sp, color = OnSurfaceVariant, maxLines = 2)
                            }
                            RiskBadge(hazard.severity)
                        }
                    }
                }
            }

            // Bottom spacing
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SafetyActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}
