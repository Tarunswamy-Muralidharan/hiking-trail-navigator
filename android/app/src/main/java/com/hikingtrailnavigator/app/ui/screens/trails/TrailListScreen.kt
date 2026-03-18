package com.hikingtrailnavigator.app.ui.screens.trails

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
import com.hikingtrailnavigator.app.domain.model.Difficulty
import com.hikingtrailnavigator.app.domain.model.Trail
import com.hikingtrailnavigator.app.ui.components.DifficultyBadge
import com.hikingtrailnavigator.app.ui.components.HikerTopBar
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun TrailListScreen(
    onTrailClick: (String) -> Unit,
    viewModel: TrailListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Discover Trails")

        // Search
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = { Text("Search trails...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        // Difficulty filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedDifficulty == null,
                onClick = { viewModel.onDifficultyFilter(null) },
                label = { Text("All") }
            )
            Difficulty.entries.forEach { diff ->
                FilterChip(
                    selected = uiState.selectedDifficulty == diff,
                    onClick = { viewModel.onDifficultyFilter(diff) },
                    label = { Text(diff.name) }
                )
            }
        }

        // Sort options
        var showSortMenu by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${uiState.trails.size} trails", color = OnSurfaceVariant, fontSize = 14.sp)
            Box {
                TextButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sort: ${uiState.sortBy.name}")
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                viewModel.onSortChange(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Trail list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.trails) { trail ->
                TrailCard(trail = trail, onClick = { onTrailClick(trail.id) })
            }
        }
    }
}

@Composable
fun TrailCard(trail: Trail, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trail.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                DifficultyBadge(trail.difficulty)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(trail.region, fontSize = 13.sp, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TrailStat(Icons.Default.Straighten, "${trail.distance} km")
                TrailStat(Icons.Default.Schedule, trail.estimatedDuration)
                TrailStat(Icons.Default.Terrain, "${trail.elevationGain}m")
                TrailStat(Icons.Default.Star, "${trail.rating}")
            }

            if (trail.hazards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Warning, null, tint = Warning, modifier = Modifier.size(14.dp))
                    Text(
                        "${trail.hazards.size} hazard${if (trail.hazards.size > 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = Warning
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(value, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}
