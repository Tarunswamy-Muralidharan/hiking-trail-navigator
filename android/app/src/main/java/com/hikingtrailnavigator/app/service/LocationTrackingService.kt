package com.hikingtrailnavigator.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.hikingtrailnavigator.app.MainActivity
import com.hikingtrailnavigator.app.R
import com.hikingtrailnavigator.app.domain.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject lateinit var geofencingService: GeofencingService

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var isTracking = false

    // Store route points locally for offline access
    private val routePoints = mutableListOf<LatLng>()

    companion object {
        const val CHANNEL_ID = "location_tracking"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "START_TRACKING"
        const val ACTION_STOP = "STOP_TRACKING"

        // Shared route for the current session (accessible from ViewModels)
        @Volatile
        var currentRoute: List<LatLng> = emptyList()
            private set

        @Volatile
        var lastKnownLocation: LatLng? = null
            private set

        @Volatile
        var totalDistance: Double = 0.0
            private set

        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (isTracking) return
        isTracking = true
        isServiceRunning = true
        routePoints.clear()
        totalDistance = 0.0

        val notification = buildNotification("Tracking your hike...")
        startForeground(NOTIFICATION_ID, notification)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).setMinUpdateDistanceMeters(5f).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val newPoint = LatLng(location.latitude, location.longitude)

                    // Calculate distance from last point
                    if (routePoints.isNotEmpty()) {
                        val lastPoint = routePoints.last()
                        totalDistance += geofencingService.haversineMeters(lastPoint, newPoint)
                    }

                    routePoints.add(newPoint)
                    currentRoute = routePoints.toList()
                    lastKnownLocation = newPoint

                    // Update notification with distance
                    val distanceKm = String.format("%.2f", totalDistance / 1000)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager.notify(NOTIFICATION_ID, buildNotification("Tracking: ${distanceKm}km covered"))

                    // Broadcast for any active listeners
                    val intent = Intent("LOCATION_UPDATE").apply {
                        setPackage(packageName)
                        putExtra("latitude", location.latitude)
                        putExtra("longitude", location.longitude)
                        putExtra("altitude", location.altitude)
                        putExtra("speed", location.speed)
                    }
                    sendBroadcast(intent)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun stopTracking() {
        isTracking = false
        isServiceRunning = false
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hiking Trail Navigator")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when the app is tracking your location during a hike"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
