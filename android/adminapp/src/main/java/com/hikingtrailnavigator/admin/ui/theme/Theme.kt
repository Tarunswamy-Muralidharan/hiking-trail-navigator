package com.hikingtrailnavigator.admin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1B5E20)
val PrimaryContainer = Color(0xFFE8F5E9)
val Danger = Color(0xFFD32F2F)
val Warning = Color(0xFFFF8F00)
val OnSurfaceVariant = Color(0xFF666666)

private val LightColors = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    error = Danger
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
