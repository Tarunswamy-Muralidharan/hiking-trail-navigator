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
import com.hikingtrailnavigator.app.ui.components.*
import com.hikingtrailnavigator.app.ui.theme.*

private val hazardTypes = listOf("Wildlife", "Landslide", "Flooding", "Fallen Tree", "Trail Damage", "Poor Visibility", "Other")
private val severityLevels = listOf("Low", "Medium", "High", "Critical")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HazardReportScreen(
    onBack: () -> Unit,
    viewModel: HazardReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Report Hazard", onBack = onBack)

        if (uiState.isSubmitted) {
            // Success state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text("Hazard Reported!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Thank you for helping keep trails safe.", color = OnSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.resetForm() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Report Another")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back to Safety")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hazard Type
                item {
                    Text("Hazard Type", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hazardTypes.forEach { type ->
                            FilterChip(
                                selected = uiState.hazardType == type,
                                onClick = { viewModel.updateType(type) },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Severity
                item {
                    Text("Severity", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        severityLevels.forEach { level ->
                            val color = when (level) {
                                "Low" -> RiskLow
                                "Medium" -> RiskMedium
                                "High" -> RiskHigh
                                "Critical" -> RiskCritical
                                else -> RiskLow
                            }
                            FilterChip(
                                selected = uiState.severity == level,
                                onClick = { viewModel.updateSeverity(level) },
                                label = { Text(level) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.15f),
                                    selectedLabelColor = color
                                )
                            )
                        }
                    }
                }

                // Description
                item {
                    Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Describe the hazard...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Location info
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MyLocation, null, tint = Primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Using your current location",
                                fontSize = 14.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Submit
                item {
                    Button(
                        onClick = { viewModel.submitReport() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.hazardType.isNotBlank() && uiState.description.isNotBlank() && !uiState.isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Warning)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Report", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Existing hazards
                if (uiState.existingHazards.isNotEmpty()) {
                    item { SectionTitle("Recent Reports") }
                    items(uiState.existingHazards.take(5)) { hazard ->
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ReportProblem, null, tint = Warning)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(hazard.type, fontWeight = FontWeight.SemiBold)
                                        Text(hazard.description, fontSize = 13.sp, color = OnSurfaceVariant, maxLines = 1)
                                    }
                                    RiskBadge(hazard.severity)
                                }
                                // FR-212: Community validation - confirm button
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${hazard.confirmations} confirmation${if (hazard.confirmations != 1) "s" else ""}",
                                        fontSize = 12.sp,
                                        color = if (hazard.confirmations >= 3) Primary else OnSurfaceVariant
                                    )
                                    TextButton(onClick = { viewModel.confirmHazard(hazard.id) }) {
                                        Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Confirm", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
