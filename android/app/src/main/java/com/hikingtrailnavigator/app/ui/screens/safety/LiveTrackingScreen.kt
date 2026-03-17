package com.hikingtrailnavigator.app.ui.screens.safety

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
import com.hikingtrailnavigator.app.ui.components.HikerTopBar
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun LiveTrackingScreen(
    onBack: () -> Unit,
    viewModel: LiveTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Live Tracking", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isTracking) PrimaryContainer else SurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (uiState.isTracking) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        null,
                        tint = if (uiState.isTracking) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (uiState.isTracking) "Tracking Active" else "Tracking Inactive",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (uiState.isTracking) "Your location is being tracked"
                        else "Start tracking to share your location with emergency contacts",
                        textAlign = TextAlign.Center,
                        color = OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Start/Stop tracking
            Button(
                onClick = {
                    if (uiState.isTracking) viewModel.stopTracking()
                    else viewModel.startTracking()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isTracking) Danger else Primary
                )
            ) {
                Icon(
                    if (uiState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (uiState.isTracking) "Stop Tracking" else "Start Tracking",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            // Share location toggle
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, null, tint = Primary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Share Location", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Share real-time location with contacts",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isLocationShared,
                        onCheckedChange = { viewModel.toggleLocationShare() },
                        enabled = uiState.isTracking
                    )
                }
            }

            // Location info
            if (uiState.currentLocation != null) {
                Spacer(Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Current Location", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Lat: ${String.format("%.6f", uiState.currentLocation!!.latitude)}",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            "Lng: ${String.format("%.6f", uiState.currentLocation!!.longitude)}",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // Share link
            if (uiState.shareLink.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Share Link", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            uiState.shareLink,
                            fontSize = 13.sp,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Safety tips
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarningLight)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Warning)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Keep GPS enabled for accurate tracking. Battery usage may increase.",
                        fontSize = 13.sp,
                        color = OnSurface
                    )
                }
            }
        }
    }
}
