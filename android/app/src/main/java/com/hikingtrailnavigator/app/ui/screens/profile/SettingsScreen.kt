package com.hikingtrailnavigator.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.HikerTopBar
import com.hikingtrailnavigator.app.ui.components.SectionTitle
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Settings", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile
            item { SectionTitle("Profile") }
            item {
                OutlinedTextField(
                    value = uiState.userName,
                    onValueChange = { viewModel.updateUserName(it) },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Safety Settings
            item { SectionTitle("Safety") }

            item {
                SettingsToggleRow(
                    title = "Fall Detection",
                    subtitle = "Detect falls using accelerometer",
                    checked = uiState.fallDetectionEnabled,
                    onToggle = { viewModel.toggleFallDetection() }
                )
            }

            item {
                SettingsToggleRow(
                    title = "Silent SOS",
                    subtitle = "Enable discreet SOS without alarm",
                    checked = uiState.silentSOSEnabled,
                    onToggle = { viewModel.toggleSilentSOS() }
                )
            }

            item {
                SettingsToggleRow(
                    title = "Location Sharing",
                    subtitle = "Share location with emergency contacts",
                    checked = uiState.locationShareEnabled,
                    onToggle = { viewModel.toggleLocationShare() }
                )
            }

            // Check-in interval
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Check-in Interval", fontWeight = FontWeight.SemiBold)
                        Text(
                            "How often you'll be prompted to confirm safety",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 60, 120).forEach { interval ->
                                FilterChip(
                                    selected = uiState.checkInInterval == interval,
                                    onClick = { viewModel.updateCheckInInterval(interval) },
                                    label = { Text("${interval}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Deviation alert
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Trail Deviation Alert", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Alert when you stray ${uiState.deviationAlertDistance}m from trail",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = uiState.deviationAlertDistance.toFloat(),
                            onValueChange = { viewModel.updateDeviationDistance(it.toInt()) },
                            valueRange = 50f..500f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("50m", fontSize = 12.sp, color = OnSurfaceVariant)
                            Text("500m", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // GPS Accuracy
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GPS Accuracy", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Higher accuracy uses more battery",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("low", "balanced", "high").forEach { accuracy ->
                                FilterChip(
                                    selected = uiState.gpsAccuracy == accuracy,
                                    onClick = { viewModel.updateGpsAccuracy(accuracy) },
                                    label = { Text(accuracy.replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Save button
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Settings", fontWeight = FontWeight.SemiBold)
                }
                if (uiState.isSaved) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Settings saved!",
                        color = Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 13.sp, color = OnSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
