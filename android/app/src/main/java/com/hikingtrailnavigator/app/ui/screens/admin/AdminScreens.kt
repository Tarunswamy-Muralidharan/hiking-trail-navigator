package com.hikingtrailnavigator.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.HikerTopBar
import com.hikingtrailnavigator.app.ui.theme.*

// ============ Admin Login ============

@Composable
fun AdminLoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Admin Login", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AdminPanelSettings,
                null,
                tint = Primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Admin Control Panel", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Forest Officers & Trail Admins", color = OnSurfaceVariant, fontSize = 14.sp)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (uiState.error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(uiState.error, color = Danger, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Login", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Default: admin / admin123",
                color = OnSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

// ============ Admin Dashboard ============

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Admin Dashboard", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Live Overview", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AdminStat("Active Hikers", "${uiState.activeHikers.size}")
                            AdminStat("Missed Check-ins", "${uiState.activeHikers.count { it.missedCheckIns > 0 }}")
                            AdminStat("Alerts", "${uiState.activeHikers.count { it.missedCheckIns >= 2 }}")
                        }
                    }
                }
            }

            // Section header
            item {
                Text(
                    "Active Hikers on Trails",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (uiState.activeHikers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Hiking, null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No active hikers", color = OnSurfaceVariant)
                            Text("Hikers will appear here when they start a trek", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            items(uiState.activeHikers) { hiker ->
                val timeSinceCheckIn = (System.currentTimeMillis() - hiker.lastCheckInTime) / 60000
                val isAlert = hiker.missedCheckIns >= 2
                val isWarning = hiker.missedCheckIns == 1

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isAlert -> Color(0xFFFFEBEE)
                            isWarning -> Color(0xFFFFF3E0)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isAlert -> Danger
                                                isWarning -> Warning
                                                else -> Primary
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(hiker.hikerName, fontWeight = FontWeight.SemiBold)
                                    Text(hiker.trailName, fontSize = 13.sp, color = OnSurfaceVariant)
                                }
                            }

                            if (isAlert) {
                                Icon(Icons.Default.Warning, null, tint = Danger)
                            } else if (isWarning) {
                                Icon(Icons.Default.Info, null, tint = Warning)
                            } else {
                                Icon(Icons.Default.CheckCircle, null, tint = Primary)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("GPS Location", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(
                                    "${String.format("%.5f", hiker.lastLat)}, ${String.format("%.5f", hiker.lastLng)}",
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Last Check-in", fontSize = 11.sp, color = OnSurfaceVariant)
                                Text(
                                    if (timeSinceCheckIn < 1) "Just now"
                                    else "${timeSinceCheckIn}m ago",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        timeSinceCheckIn > 60 -> Danger
                                        timeSinceCheckIn > 30 -> Warning
                                        else -> Primary
                                    }
                                )
                            }
                        }

                        if (hiker.missedCheckIns > 0) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAlert) Danger.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning, null,
                                        tint = if (isAlert) Danger else Warning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "${hiker.missedCheckIns} missed check-in(s) - ${if (isAlert) "NEEDS ATTENTION" else "monitoring"}",
                                        fontSize = 12.sp,
                                        color = if (isAlert) Danger else Warning,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Route warnings section
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Crowdsourced Route Warnings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (uiState.routeWarnings.isEmpty()) {
                item {
                    Text("No active route warnings", color = OnSurfaceVariant, fontSize = 13.sp)
                }
            }

            items(uiState.routeWarnings) { warning ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ReportProblem, null,
                            tint = Warning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(warning.warningType, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(warning.description, fontSize = 12.sp, color = OnSurfaceVariant)
                            Text(
                                "By: ${warning.reportedBy} | ${warning.upvotes} upvotes",
                                fontSize = 11.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.deactivateWarning(warning.id) }) {
                            Icon(Icons.Default.Close, null, tint = Danger, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Primary)
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

