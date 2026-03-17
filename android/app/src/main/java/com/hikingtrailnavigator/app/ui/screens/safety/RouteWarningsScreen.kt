package com.hikingtrailnavigator.app.ui.screens.safety

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
import com.hikingtrailnavigator.app.ui.components.HikerTopBar
import androidx.compose.ui.graphics.Color
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun RouteWarningsScreen(
    onBack: () -> Unit,
    viewModel: RouteWarningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Route Warnings", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add warning section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddAlert, null, tint = Warning)
                            Spacer(Modifier.width(8.dp))
                            Text("Report a Route Warning", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            "Help other hikers by reporting dangerous routes",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        // Warning type chips
                        Text("Type:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Landslide", "Flooding", "Wildlife", "Trail Blocked", "Dangerous").forEach { type ->
                                FilterChip(
                                    selected = uiState.selectedType == type,
                                    onClick = { viewModel.updateType(type) },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::updateDescription,
                            placeholder = { Text("Describe the danger (e.g., 'Landslide blocked the path after 3rd hill')") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.reporterName,
                            onValueChange = viewModel::updateReporterName,
                            placeholder = { Text("Your name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = viewModel::submitWarning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Warning),
                            enabled = uiState.selectedType.isNotEmpty() && uiState.description.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Send, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Warning", fontWeight = FontWeight.SemiBold)
                        }

                        if (uiState.submitted) {
                            Spacer(Modifier.height(8.dp))
                            Text("Warning submitted! Other hikers will see this.", color = Primary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Existing warnings
            item {
                Spacer(Modifier.height(4.dp))
                Text("Active Route Warnings from Hikers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Crowdsourced by the hiking community", fontSize = 13.sp, color = OnSurfaceVariant)
            }

            if (uiState.warnings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No active warnings", color = OnSurfaceVariant)
                            Text("All routes are clear!", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            items(uiState.warnings) { warning ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning, null,
                                    tint = when (warning.warningType) {
                                        "Wildlife" -> Danger
                                        "Flooding", "Landslide" -> Warning
                                        else -> Color(0xFFE65100)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(warning.warningType, fontWeight = FontWeight.Bold)
                            }

                            // Upvote button
                            TextButton(onClick = { viewModel.upvoteWarning(warning.id) }) {
                                Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${warning.upvotes}")
                            }
                        }

                        Text(warning.description, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Reported by: ${warning.reportedBy}",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            Text(
                                formatTimeAgo(warning.reportedAt),
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }

                        if (warning.upvotes >= 3) {
                            Spacer(Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DangerLight),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Confirmed by community",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = Danger,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> "${minutes / 1440}d ago"
    }
}
