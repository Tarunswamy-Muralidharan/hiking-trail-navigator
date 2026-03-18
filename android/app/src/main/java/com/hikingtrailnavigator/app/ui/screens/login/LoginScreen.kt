package com.hikingtrailnavigator.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hikingtrailnavigator.app.ui.theme.*

@Composable
fun LoginScreen(
    onHikerLogin: (String) -> Unit,
    onAdminLogin: () -> Unit
) {
    var showHikerForm by remember { mutableStateOf(false) }
    var hikerName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo area
            Icon(
                Icons.Default.Terrain,
                null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Hiking Trail Navigator",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "PSG iTech Neelambur",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(48.dp))

            if (!showHikerForm) {
                // Role selection
                Text(
                    "Choose your role",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(24.dp))

                // Hiker button
                Button(
                    onClick = { showHikerForm = true },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Default.Hiking, null, tint = Primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("I'm a Hiker", color = Primary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Start exploring trails", color = OnSurfaceVariant, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Admin button
                OutlinedButton(
                    onClick = onAdminLogin,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.6f)))
                    )
                ) {
                    Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("I'm an Admin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Manage trails & monitor hikers", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            } else {
                // Hiker name form
                Text(
                    "Enter your name",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = hikerName,
                    onValueChange = { hikerName = it; nameError = "" },
                    label = { Text("Your Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                if (nameError.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(nameError, color = Color(0xFFFFCDD2), fontSize = 13.sp)
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (hikerName.isBlank()) {
                            nameError = "Please enter your name"
                        } else {
                            onHikerLogin(hikerName.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Default.DirectionsWalk, null, tint = Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Hiking", color = Primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(12.dp))

                TextButton(onClick = { showHikerForm = false }) {
                    Text("Back", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}
