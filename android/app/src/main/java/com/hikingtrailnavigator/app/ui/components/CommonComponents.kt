package com.hikingtrailnavigator.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hikingtrailnavigator.app.domain.model.Difficulty
import com.hikingtrailnavigator.app.domain.model.Severity
import com.hikingtrailnavigator.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikerTopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
fun DifficultyBadge(difficulty: Difficulty) {
    val color = when (difficulty) {
        Difficulty.Easy -> DifficultyEasy
        Difficulty.Moderate -> DifficultyModerate
        Difficulty.Hard -> DifficultyHard
        Difficulty.Expert -> DifficultyExpert
    }
    Badge(color, difficulty.name)
}

@Composable
fun SeverityBadge(severity: Severity) {
    val color = when (severity) {
        Severity.Low -> RiskLow
        Severity.Medium -> RiskMedium
        Severity.High -> RiskHigh
        Severity.Critical -> RiskCritical
    }
    Badge(color, severity.name)
}

@Composable
fun RiskBadge(level: String) {
    val color = when (level.lowercase()) {
        "low" -> RiskLow
        "medium" -> RiskMedium
        "high" -> RiskHigh
        "critical" -> RiskCritical
        else -> RiskLow
    }
    Badge(color, level.replaceFirstChar { it.uppercase() })
}

@Composable
private fun Badge(color: Color, text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
