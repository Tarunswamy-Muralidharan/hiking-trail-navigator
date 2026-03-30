package com.hikingtrailnavigator.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.hikingtrailnavigator.app.MainActivity
import com.hikingtrailnavigator.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sends high-priority Android notifications to alert admin when SOS is triggered.
 * Works locally since both hiker and admin roles run on the same device.
 */
@Singleton
class SosNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SOS_CHANNEL_ID = "sos_alert_channel"
        const val SOS_NOTIFICATION_ID = 3001
    }

    init {
        createSosChannel()
    }

    fun sendSosAlert(
        hikerName: String,
        alertType: String,
        latitude: Double,
        longitude: Double,
        message: String
    ) {
        val typeLabel = when (alertType) {
            "SOS_BUTTON" -> "SOS BUTTON PRESSED"
            "FALL_DETECTED" -> "FALL DETECTED"
            "CHECKIN_MISSED" -> "CHECK-IN MISSED"
            else -> "EMERGENCY ALERT"
        }

        // Intent opens app and navigates to admin dashboard
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "OPEN_ADMIN_SOS")
            putExtra("alert_type", alertType)
        }
        val requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val gpsText = if (latitude != 0.0 || longitude != 0.0) {
            "GPS: ${String.format("%.5f", latitude)}, ${String.format("%.5f", longitude)}"
        } else {
            "Location unavailable"
        }

        val notification = NotificationCompat.Builder(context, SOS_CHANNEL_ID)
            .setContentTitle("ADMIN ALERT: $typeLabel")
            .setContentText("$hikerName needs help! $gpsText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$hikerName needs help!\n$gpsText\n$message\n\nTap to open Admin Dashboard and respond.")
            )
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    private fun createSosChannel() {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val channel = NotificationChannel(
            SOS_CHANNEL_ID,
            "SOS Emergency Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical alerts when a hiker triggers SOS, fall detection, or misses check-ins"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            setSound(
                alarmSound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
