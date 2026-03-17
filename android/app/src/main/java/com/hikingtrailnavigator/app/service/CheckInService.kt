package com.hikingtrailnavigator.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hikingtrailnavigator.app.MainActivity
import com.hikingtrailnavigator.app.R
import com.hikingtrailnavigator.app.data.local.dao.ActiveHikerDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FR-201: Periodic Safety Check-In System
 * Sends notifications every 30 minutes asking the user to confirm they're OK.
 * If missed, escalates to admin dashboard.
 */
@Singleton
class CheckInService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activeHikerDao: ActiveHikerDao
) {
    companion object {
        const val CHANNEL_ID = "check_in_channel"
        const val NOTIFICATION_ID = 2001
        const val CHECK_IN_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
    }

    private var isRunning = false
    private var currentSessionId: String? = null

    fun startCheckIns(sessionId: String) {
        if (isRunning) return
        isRunning = true
        currentSessionId = sessionId
        createNotificationChannel()

        CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                delay(CHECK_IN_INTERVAL_MS)
                if (!isRunning) break
                sendCheckInNotification()
                // Wait 5 minutes for response, then mark as missed
                delay(5 * 60 * 1000L)
                if (isRunning) {
                    currentSessionId?.let { id ->
                        activeHikerDao.incrementMissedCheckIn(id)
                    }
                }
            }
        }
    }

    fun stopCheckIns() {
        isRunning = false
        currentSessionId = null
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun confirmCheckIn() {
        CoroutineScope(Dispatchers.IO).launch {
            currentSessionId?.let { id ->
                val session = activeHikerDao.getById(id)
                if (session != null) {
                    activeHikerDao.checkIn(
                        id, System.currentTimeMillis(),
                        session.lastLat, session.lastLng
                    )
                }
            }
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun sendCheckInNotification() {
        val confirmIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("action", "CHECK_IN_CONFIRM")
        }
        val confirmPending = PendingIntent.getActivity(
            context, 100, confirmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Safety Check-In")
            .setContentText("Tap to confirm you're safe. Admins will be alerted if no response.")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(confirmPending)
            .addAction(R.mipmap.ic_launcher_foreground, "I'm OK", confirmPending)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Safety Check-In",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Periodic safety check-in notifications during hikes"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
