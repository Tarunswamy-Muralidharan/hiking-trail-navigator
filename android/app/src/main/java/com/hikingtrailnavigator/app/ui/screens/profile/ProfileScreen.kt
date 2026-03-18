package com.hikingtrailnavigator.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.screens.navigate.formatDuration
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onEmergencyContactsClick: () -> Unit,
    onAdminClick: () -> Unit = {},
    onRouteWarningsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Profile")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // User profile card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            color = Primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            uiState.userName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Hiking enthusiast",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // Stats
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard("Hikes", "${uiState.totalHikes}")
                        StatCard("Distance", String.format("%.1f km", uiState.totalDistance))
                        StatCard("Time", formatDuration(uiState.totalDuration))
                    }
                }
            }

            // Safety summary
            item { SectionTitle("Safety") }

            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Contacts, null, tint = Primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Emergency Contacts", fontWeight = FontWeight.SemiBold)
                            Text("${uiState.emergencyContactCount} contacts saved", fontSize = 13.sp, color = OnSurfaceVariant)
                        }
                        if (uiState.emergencyContactCount == 0) {
                            Text("Add", color = Danger, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Preferences summary
            item { SectionTitle("Preferences") }

            item {
                PreferenceRow(
                    icon = Icons.Default.Timer,
                    title = "Check-in Interval",
                    value = "${uiState.preferences.checkInInterval} min"
                )
            }
            item {
                PreferenceRow(
                    icon = Icons.Default.Sensors,
                    title = "Fall Detection",
                    value = if (uiState.preferences.fallDetectionEnabled) "Enabled" else "Disabled"
                )
            }
            item {
                PreferenceRow(
                    icon = Icons.Default.VolumeOff,
                    title = "Silent SOS",
                    value = if (uiState.preferences.silentSOSEnabled) "Enabled" else "Disabled"
                )
            }

            // Menu items
            item { SectionTitle("Account") }

            item {
                ProfileMenuItem(
                    icon = Icons.Default.Contacts,
                    title = "Emergency Contacts",
                    onClick = onEmergencyContactsClick
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    onClick = onSettingsClick
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.ReportProblem,
                    title = "Route Warnings (Crowdsource)",
                    onClick = onRouteWarningsClick
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin Control Panel",
                    onClick = onAdminClick
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    onClick = { }
                )
            }

            // Logout
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.SemiBold)
                }
            }

            // App info
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hiking Trail Navigator v1.0.0",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PreferenceRow(icon: ImageVector, title: String, value: String) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Primary)
            Spacer(Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f))
            Text(value, color = OnSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Primary)
            Spacer(Modifier.width(12.dp))
            Text(title, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
        }
    }
}
