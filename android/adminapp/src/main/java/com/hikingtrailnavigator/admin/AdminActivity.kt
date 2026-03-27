package com.hikingtrailnavigator.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hikingtrailnavigator.admin.ui.AdminApp
import com.hikingtrailnavigator.admin.ui.theme.AdminTheme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminTheme {
                AdminApp()
            }
        }
    }
}
