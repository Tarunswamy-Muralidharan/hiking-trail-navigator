package com.hikingtrailnavigator.app.ui.screens.safety

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.HikerTopBar
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun SOSScreen(
    onBack: () -> Unit,
    viewModel: SOSViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "SOS Emergency", onBack = onBack)

        // Offline indicator
        if (uiState.isOffline) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiOff, null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Offline - SOS will use SMS (works without internet)",
                        fontSize = 12.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!uiState.isSosActive) {
                // SOS not active - show activation buttons
                Icon(
                    Icons.Default.Warning,
                    null,
                    tint = Danger,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Emergency SOS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "This will send your exact GPS location to emergency contacts and forest officers via SMS",
                    textAlign = TextAlign.Center,
                    color = OnSurfaceVariant,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(12.dp))

                // Info cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("GPS works offline - no internet needed for location", fontSize = 12.sp, color = Primary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sms, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("SMS alerts sent to contacts + forest officers", fontSize = 12.sp, color = Primary)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Main SOS button
                Button(
                    onClick = { viewModel.activateSos(null) },
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = CircleShape
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SOS", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Tap to activate", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Silent SOS
                OutlinedButton(
                    onClick = { viewModel.activateSilentSos(null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                ) {
                    Icon(Icons.Default.VolumeOff, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Silent SOS (no vibration)")
                }

                Spacer(Modifier.height(12.dp))

                // Call emergency
                OutlinedButton(
                    onClick = { viewModel.callEmergency() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Call, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Call 112 (Emergency)")
                }
            } else {
                // SOS is active
                if (uiState.isLocating) {
                    CircularProgressIndicator(color = Danger, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Getting GPS location...", fontSize = 18.sp, color = Danger)
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Danger.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Danger,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    if (uiState.isSilentMode) "Silent SOS Active" else "SOS Active",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Danger
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.sosMessage,
                    textAlign = TextAlign.Center,
                    color = OnSurfaceVariant,
                    fontSize = 15.sp
                )

                // Show GPS coordinates
                if (uiState.currentLocation != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.GpsFixed, null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("GPS Pinpoint", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1565C0))
                                Text(
                                    "${String.format("%.6f", uiState.currentLocation!!.latitude)}, ${String.format("%.6f", uiState.currentLocation!!.longitude)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }
                    }
                }

                if (uiState.contactsNotified > 0) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = Primary)
                                Spacer(Modifier.width(8.dp))
                                Text("${uiState.contactsNotified} contact(s) notified via SMS")
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Forest, null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Forest officers alerted", fontSize = 13.sp, color = OnSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Cancel SOS
                Button(
                    onClick = { viewModel.cancelSos() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnSurfaceVariant)
                ) {
                    Text("Cancel SOS", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))

                // Call emergency
                Button(
                    onClick = { viewModel.callEmergency() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Icon(Icons.Default.Call, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Call 112", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
