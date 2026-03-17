package com.hikingtrailnavigator.app.ui.screens.navigate

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun NavigateScreen(
    onStartHike: (String) -> Unit,
    onViewHistory: () -> Unit,
    viewModel: NavigateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Navigate")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats summary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
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

            // History button
            item {
                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Activity History")
                }
            }

            // Choose a trail
            item {
                SectionTitle("Choose a Trail")
            }

            items(uiState.trails) { trail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartHike(trail.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trail.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${trail.distance}km \u2022 ${trail.estimatedDuration}",
                                fontSize = 13.sp,
                                color = OnSurfaceVariant
                            )
                        }
                        DifficultyBadge(trail.difficulty)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PlayArrow,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
