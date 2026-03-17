package com.hikingtrailnavigator.app.ui.screens.navigate

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.domain.model.HikeActivity
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityHistoryScreen(
    onBack: () -> Unit,
    viewModel: NavigateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Activity History", onBack = onBack)

        // Summary stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Total Hikes", "${uiState.totalHikes}")
                StatCard("Total km", String.format("%.1f", uiState.totalDistance))
                StatCard("Total Time", formatDuration(uiState.totalDuration))
            }
        }

        if (uiState.activities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Hiking,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No hikes yet", color = OnSurfaceVariant)
                    Text("Start your first trail!", fontSize = 13.sp, color = OnSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.activities) { activity ->
                    ActivityCard(activity)
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: HikeActivity) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(activity.trailName, fontWeight = FontWeight.SemiBold)
                Text(
                    dateFormat.format(Date(activity.startTime)),
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(String.format("%.1f km", activity.distance), fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(formatDuration(activity.duration), fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terrain, null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("${activity.elevationGain}m", fontSize = 13.sp)
                }
            }
        }
    }
}
