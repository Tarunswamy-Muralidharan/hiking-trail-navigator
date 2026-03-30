package com.hikingtrailnavigator.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hikingtrailnavigator.app.service.EmergencyService
import com.hikingtrailnavigator.app.service.SessionManager
import com.hikingtrailnavigator.app.ui.HikerApp
import com.hikingtrailnavigator.app.ui.theme.HikerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var emergencyService: EmergencyService
    @Inject lateinit var sessionManager: SessionManager

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permission result received - app will pick up location on next poll
    }

    // FR-207: Volume button silent SOS - detect 5 rapid presses
    private var volumePressCount = 0
    private var lastVolumePressTime = 0L
    private val volumePressWindowMs = 3000L // 3 seconds for 5 presses
    private var silentSosTriggered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocationPermissions()
        setContent {
            HikerTheme {
                HikerApp()
            }
        }
    }

    // FR-207: Detect rapid volume down presses for silent SOS
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val now = System.currentTimeMillis()
            if (now - lastVolumePressTime > volumePressWindowMs) {
                volumePressCount = 0
                silentSosTriggered = false
            }
            lastVolumePressTime = now
            volumePressCount++

            if (volumePressCount >= 5 && !silentSosTriggered) {
                silentSosTriggered = true
                volumePressCount = 0
                triggerSilentSos()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun triggerSilentSos() {
        CoroutineScope(Dispatchers.IO).launch {
            val location = emergencyService.getCurrentLocation()
            if (location != null) {
                emergencyService.sendSosToAllContacts(location)
            }
        }
    }

    private fun requestLocationPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocation != PackageManager.PERMISSION_GRANTED || coarseLocation != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
