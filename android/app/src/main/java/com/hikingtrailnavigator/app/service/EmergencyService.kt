package com.hikingtrailnavigator.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.hikingtrailnavigator.app.data.repository.EmergencyContactRepository
import com.hikingtrailnavigator.app.domain.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: EmergencyContactRepository
) {
    // Tamil Nadu Forest department and disaster management contacts
    private val forestOfficerContacts = listOf(
        Pair("TN Forest Dept. Helpline", "1800-425-1600"),
        Pair("Disaster Mgmt (SDMA)", "1070")
    )

    fun callEmergency(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: SecurityException) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }

    /**
     * Get current GPS location. Works offline since GPS doesn't need internet.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLng? {
        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
            if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                // Fallback: try last known location
                val lastLocation = fusedClient.lastLocation.await()
                if (lastLocation != null) {
                    LatLng(lastLocation.latitude, lastLocation.longitude)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Send SOS SMS to all user emergency contacts AND forest officers.
     * SMS works without internet - uses cell network directly.
     */
    suspend fun sendSosToAllContacts(location: LatLng) {
        val userContacts = contactRepository.getAllContacts().first()
        val locationLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        val message = "SOS EMERGENCY! I need help while hiking.\n" +
                "GPS Location: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}\n" +
                "Map: $locationLink\n" +
                "Sent via Hiking Trail Navigator"

        // Send to user's emergency contacts
        userContacts.forEach { contact ->
            sendSms(contact.phone, message)
        }

        // Send to forest officers and admin
        forestOfficerContacts.forEach { (_, phone) ->
            sendSms(phone, message)
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    suspend fun sendSosToContacts(location: LatLng) {
        sendSosToAllContacts(location)
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            // Split long messages into parts
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } catch (_: Exception) {
            // SMS permission may not be granted or no SIM card
        }
    }

    fun triggerSOSVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // SOS pattern: 3 short, 3 long, 3 short
        val pattern = longArrayOf(0, 200, 100, 200, 100, 200, 200, 500, 100, 500, 100, 500, 200, 200, 100, 200, 100, 200)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }
}
