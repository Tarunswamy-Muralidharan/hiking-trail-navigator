package com.hikingtrailnavigator.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hikingtrailnavigator.app.domain.model.Difficulty
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
    onManageTrails: () -> Unit,
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
                            AdminStat("SOS Alerts", "${uiState.sosAlerts.size}")
                        }
                    }
                }
            }

            // SOS Alerts section
            if (uiState.sosAlerts.isNotEmpty()) {
                item {
                    Text(
                        "SOS Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Danger,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(uiState.sosAlerts) { alert ->
                    val timeSince = (System.currentTimeMillis() - alert.timestamp) / 60000
                    val timeStr = if (timeSince < 1) "Just now" else "${timeSince}m ago"
                    val alertColor = when (alert.alertType) {
                        "SOS_BUTTON" -> Danger
                        "FALL_DETECTED" -> Danger
                        else -> Warning
                    }
                    val alertIcon = when (alert.alertType) {
                        "SOS_BUTTON" -> Icons.Default.Warning
                        "FALL_DETECTED" -> Icons.Default.PersonOff
                        else -> Icons.Default.AccessTime
                    }
                    val alertLabel = when (alert.alertType) {
                        "SOS_BUTTON" -> "SOS BUTTON PRESSED"
                        "FALL_DETECTED" -> "FALL DETECTED"
                        else -> "CHECK-IN MISSED"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = alertColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(alertIcon, null, tint = alertColor, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(alertLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = alertColor)
                                        Text(alert.hikerName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    }
                                }
                                Text(timeStr, fontSize = 12.sp, color = alertColor, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.height(6.dp))
                            if (alert.trailName.isNotEmpty()) {
                                Text("Trail: ${alert.trailName}", fontSize = 13.sp, color = OnSurfaceVariant)
                            }
                            Text(
                                "GPS: ${String.format("%.5f", alert.latitude)}, ${String.format("%.5f", alert.longitude)}",
                                fontSize = 12.sp, color = OnSurfaceVariant
                            )
                            Text(alert.message, fontSize = 12.sp, color = OnSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.resolveAlert(alert.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("Resolve", fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("No active SOS alerts", color = Primary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Manage Trails button
            item {
                Card(
                    onClick = onManageTrails,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Terrain, null, tint = Primary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Manage Trails", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(
                                    "${uiState.trails.size} trails registered",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
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

            // FR-212: Hazard Reports Moderation section
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hazard Reports (Moderation)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (uiState.hazardReports.isEmpty()) {
                item {
                    Text("No hazard reports to review", color = OnSurfaceVariant, fontSize = 13.sp)
                }
            }

            items(uiState.hazardReports) { report ->
                val confidenceLevel = when {
                    report.confirmations >= 3 -> "High"
                    report.confirmations >= 1 -> "Medium"
                    else -> "Low"
                }
                val confidenceColor = when {
                    report.confirmations >= 3 -> Primary
                    report.confirmations >= 1 -> Warning
                    else -> Danger
                }
                val isExpired = report.expiresAt > 0 && report.expiresAt < System.currentTimeMillis()
                val daysLeft = if (report.expiresAt > 0) {
                    ((report.expiresAt - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                } else -1

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (report.isVerified) Color(0xFFE8F5E9)
                        else if (isExpired) Color(0xFFEEEEEE)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReportProblem, null, tint = Warning, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(report.type, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Severity: ${report.severity}", fontSize = 12.sp, color = OnSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Confidence: $confidenceLevel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = confidenceColor
                                )
                                Text(
                                    "${report.confirmations} confirmation(s)",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(report.description, fontSize = 12.sp, color = OnSurfaceVariant)
                        if (daysLeft >= 0) {
                            Text(
                                if (isExpired) "EXPIRED" else "Expires in $daysLeft days",
                                fontSize = 11.sp,
                                color = if (isExpired) Danger else OnSurfaceVariant,
                                fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (report.isVerified) {
                            Text("VERIFIED", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!report.isVerified) {
                                Button(
                                    onClick = { viewModel.verifyHazard(report.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text("Verify", fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.rejectHazard(report.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Text("Reject", fontSize = 12.sp, color = Danger)
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

// ============ Manage Trails ============

@Composable
fun AdminManageTrailsScreen(
    onBack: () -> Unit,
    onAddTrail: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Manage Trails", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Add trail button
            item {
                Button(
                    onClick = onAddTrail,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Trail", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            item {
                Text(
                    "${uiState.trails.size} trails total",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(uiState.trails) { trail ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (trail.difficulty) {
                                        "Easy" -> Primary.copy(alpha = 0.15f)
                                        "Moderate" -> Warning.copy(alpha = 0.15f)
                                        "Hard" -> Danger.copy(alpha = 0.15f)
                                        else -> Color(0xFF9C27B0).copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Terrain, null,
                                tint = when (trail.difficulty) {
                                    "Easy" -> Primary
                                    "Moderate" -> Warning
                                    "Hard" -> Danger
                                    else -> Color(0xFF9C27B0)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                trail.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${trail.difficulty} | ${trail.distance} km | ${trail.region}",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            Text(
                                "Rating: ${trail.rating} | Pop: ${trail.popularity}",
                                fontSize = 11.sp,
                                color = OnSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showDeleteDialog = trail.id }) {
                            Icon(
                                Icons.Default.Delete, null,
                                tint = Danger.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { trailId ->
        val trailName = uiState.trails.find { it.id == trailId }?.name ?: "this trail"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Trail") },
            text = { Text("Are you sure you want to delete \"$trailName\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrail(trailId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ============ Add Trail ============

@Composable
fun AdminAddTrailScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.addTrailState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.resetAddTrailForm()
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HikerTopBar(title = "Add New Trail", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::updateTrailName,
                    label = { Text("Trail Name *") },
                    leadingIcon = { Icon(Icons.Default.Terrain, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateTrailDescription,
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Difficulty selector
            item {
                Text("Difficulty", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = state.difficulty == diff,
                            onClick = { viewModel.updateTrailDifficulty(diff) },
                            label = { Text(diff.name, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (diff) {
                                    Difficulty.Easy -> Primary
                                    Difficulty.Moderate -> Warning
                                    Difficulty.Hard -> Danger
                                    Difficulty.Expert -> Color(0xFF9C27B0)
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.distance,
                        onValueChange = viewModel::updateTrailDistance,
                        label = { Text("Distance (km) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = state.elevationGain,
                        onValueChange = viewModel::updateTrailElevation,
                        label = { Text("Elevation (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.duration,
                        onValueChange = viewModel::updateTrailDuration,
                        label = { Text("Duration") },
                        placeholder = { Text("e.g. 2-3 hours") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.region,
                        onValueChange = viewModel::updateTrailRegion,
                        label = { Text("Region") },
                        placeholder = { Text("Coimbatore") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            item {
                Text("Start Point Coordinates *", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.startLat,
                        onValueChange = viewModel::updateTrailStartLat,
                        label = { Text("Latitude") },
                        placeholder = { Text("11.0168") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = state.startLng,
                        onValueChange = viewModel::updateTrailStartLng,
                        label = { Text("Longitude") },
                        placeholder = { Text("76.9558") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = state.hazards,
                    onValueChange = viewModel::updateTrailHazards,
                    label = { Text("Hazards (comma-separated)") },
                    placeholder = { Text("Rocky terrain, Slippery, Wildlife") },
                    leadingIcon = { Icon(Icons.Default.Warning, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (state.error.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Danger.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = Danger, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(state.error, color = Danger, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = viewModel::saveTrail,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.isSaving) "Saving..." else "Save Trail",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
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
