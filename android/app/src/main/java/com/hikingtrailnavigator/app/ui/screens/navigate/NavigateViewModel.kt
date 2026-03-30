package com.hikingtrailnavigator.app.ui.screens.navigate

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.local.entity.toSosEntity
import com.hikingtrailnavigator.app.data.repository.*
import com.hikingtrailnavigator.app.domain.model.*
import com.hikingtrailnavigator.app.service.ConnectivityService
import com.hikingtrailnavigator.app.service.EmergencyService
import com.hikingtrailnavigator.app.service.FallDetectionService
import com.hikingtrailnavigator.app.service.GeofencingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NavigateUiState(
    val trails: List<Trail> = emptyList(),
    val activities: List<HikeActivity> = emptyList(),
    val totalHikes: Int = 0,
    val totalDistance: Double = 0.0,
    val totalDuration: Long = 0
)

@HiltViewModel
class NavigateViewModel @Inject constructor(
    private val trailRepository: TrailRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigateUiState())
    val uiState: StateFlow<NavigateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            trailRepository.getAllTrails().collect { trails ->
                _uiState.update { it.copy(trails = trails) }
            }
        }
        viewModelScope.launch {
            activityRepository.getAllActivities().collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
        viewModelScope.launch {
            val (count, dist, dur) = activityRepository.getStats()
            _uiState.update { it.copy(totalHikes = count, totalDistance = dist, totalDuration = dur) }
        }
    }
}

data class ActiveHikeUiState(
    val trail: Trail? = null,
    val elapsedTime: Long = 0,
    val distanceTraveled: Double = 0.0,
    val distanceRemaining: Double = 0.0,
    val estimatedTimeRemaining: String = "",
    val elevationGained: Int = 0,
    val currentSpeed: Double = 0.0, // km/h
    val avgPace: String = "", // min/km
    val currentLocation: LatLng? = null,
    val route: List<LatLng> = emptyList(),
    val isPaused: Boolean = false,
    val isDeviating: Boolean = false,
    val deviationDistance: Double = 0.0,
    val checkInsCompleted: Int = 0,
    val showCheckInDialog: Boolean = false,
    val showEndDialog: Boolean = false,
    // Fall detection
    val showFallDetectedDialog: Boolean = false,
    val fallCountdown: Int = 30,
    // Danger zone alerts
    val insideDangerZone: DangerZone? = null,
    val showDangerZoneAlert: Boolean = false,
    // No-coverage zone alerts
    val insideNoCoverageZone: Boolean = false,
    val showNoCoverageAlert: Boolean = false,
    // Check-in escalation
    val checkInMissed: Boolean = false,
    val checkInEscalationLevel: Int = 0,
    // All danger zones for map display
    val allDangerZones: List<DangerZone> = emptyList(),
    // Satellite toggle
    val useSatellite: Boolean = true,
    // Offline map status
    val offlineMapStatus: String = "",
    // FR-102: Turn-by-turn navigation
    val nextWaypointIndex: Int = 0,
    val distanceToNextWaypoint: Double = 0.0, // meters
    val bearingToNextWaypoint: String = "", // e.g. "NW", "SE"
    // FR-105: Post-hike difficulty feedback
    val showDifficultyRating: Boolean = false,
    val userDifficultyRating: Int = 0,
    // FR-210: Low activity zones
    val allLowActivityZones: List<LowActivityZone> = emptyList(),
    val insideLowActivityZone: LowActivityZone? = null,
    val showLowActivityAlert: Boolean = false,
    // FR-211: Layer toggle states
    val showDangerZoneLayer: Boolean = true,
    val showNoCoverageLayer: Boolean = true,
    val showLowActivityLayer: Boolean = true,
    val showLayerPanel: Boolean = false,
    // Real-time device connectivity (separate from zone-based no-coverage)
    val isDeviceOffline: Boolean = false
)

@HiltViewModel
class ActiveHikeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trailRepository: TrailRepository,
    private val activityRepository: ActivityRepository,
    private val geofencingService: GeofencingService,
    private val fallDetectionService: FallDetectionService,
    private val emergencyService: EmergencyService,
    private val sosAlertDao: com.hikingtrailnavigator.app.data.local.dao.SosAlertDao,
    private val sessionManager: com.hikingtrailnavigator.app.service.SessionManager,
    private val sosNotificationService: com.hikingtrailnavigator.app.service.SosNotificationService,
    private val hikeSessionRepository: HikeSessionRepository,
    private val safetyCheckInRepository: SafetyCheckInRepository,
    private val notificationRepository: NotificationRepository,
    private val connectivityService: ConnectivityService
) : ViewModel() {

    private val trailId: String = savedStateHandle["trailId"] ?: ""

    private val _uiState = MutableStateFlow(ActiveHikeUiState())
    val uiState: StateFlow<ActiveHikeUiState> = _uiState.asStateFlow()

    private var startTime = System.currentTimeMillis()
    private var lastLocation: Location? = null
    private var checkInTimerJob: Job? = null
    private var fallCountdownJob: Job? = null
    private var checkInIntervalMs = 60 * 60 * 1000L // 1 hour default
    private var dangerZones: List<DangerZone> = emptyList()
    private var noCoverageZones: List<NoCoverageZone> = emptyList()
    private var lowActivityZones: List<LowActivityZone> = emptyList()
    // UML HikeSession tracking
    private var currentSessionId: String = UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            val trail = trailRepository.getTrailById(trailId)
            _uiState.update { it.copy(trail = trail) }
            // Use configurable check-in interval from trail
            trail?.let {
                checkInIntervalMs = it.checkInIntervalMinutes * 60 * 1000L
            }

            // UML State Chart: LoggedIn -> selectTrail() -> SelectingTrail -> startHike() -> Hiking
            // Create and persist HikeSession (UML: HikeSession.startHike())
            val session = HikeSession(
                sessionId = currentSessionId,
                hikerId = sessionManager.getUserId().ifEmpty { "local_user" },
                trailId = trailId,
                trailName = trail?.name ?: "",
                startTime = startTime,
                status = HikeStatus.Active
            )
            hikeSessionRepository.startSession(session)
            sessionManager.saveActiveSessionId(currentSessionId)
        }

        // Load danger zones, no-coverage zones, and low-activity zones for proactive alerts
        viewModelScope.launch {
            trailRepository.getDangerZones().collect { zones ->
                dangerZones = zones
                _uiState.update { it.copy(allDangerZones = zones) }
            }
        }
        viewModelScope.launch {
            trailRepository.getNoCoverageZones().collect { zones ->
                noCoverageZones = zones
            }
        }
        // FR-210: Load low activity zones
        viewModelScope.launch {
            trailRepository.getLowActivityZones().collect { zones ->
                lowActivityZones = zones
                _uiState.update { it.copy(allLowActivityZones = zones) }
            }
        }

        // Start fall detection monitoring
        fallDetectionService.startMonitoring()
        viewModelScope.launch {
            fallDetectionService.fallDetected.collect { detected ->
                if (detected && !_uiState.value.showFallDetectedDialog) {
                    onFallDetected()
                }
            }
        }

        // Observe real-time device connectivity (FR-209: offline warning during hike)
        viewModelScope.launch {
            connectivityService.isOnline.collect { online ->
                _uiState.update { it.copy(isDeviceOffline = !online) }
            }
        }

        // Start periodic check-in timer (FR-201)
        startCheckInTimer()

        // Start elapsed time ticker
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isPaused) {
                    _uiState.update {
                        it.copy(elapsedTime = System.currentTimeMillis() - startTime)
                    }
                }
            }
        }
    }

    // ===== FR-201: Periodic Safety Check-In =====

    private fun startCheckInTimer() {
        checkInTimerJob?.cancel()
        checkInTimerJob = viewModelScope.launch {
            while (true) {
                delay(checkInIntervalMs)
                if (!_uiState.value.isPaused) {
                    triggerCheckIn()
                }
            }
        }
    }

    private var currentCheckInId: String? = null

    private fun triggerCheckIn() {
        // UML: SafetyCheckIn - schedule and track check-in
        val checkInId = UUID.randomUUID().toString()
        currentCheckInId = checkInId
        _uiState.update { it.copy(showCheckInDialog = true, checkInMissed = false, checkInEscalationLevel = 0) }

        viewModelScope.launch {
            // Persist SafetyCheckIn (UML: SafetyCheckIn entity)
            safetyCheckInRepository.scheduleCheckIn(
                SafetyCheckIn(
                    checkInId = checkInId,
                    sessionId = currentSessionId,
                    scheduledTime = System.currentTimeMillis(),
                    responseStatus = "pending"
                )
            )
        }

        // Escalation: if no response within 2 minutes, escalate
        viewModelScope.launch {
            delay(120_000) // 2 minutes
            if (_uiState.value.showCheckInDialog) {
                _uiState.update { it.copy(checkInEscalationLevel = 1, checkInMissed = true) }
                // Mark check-in as missed
                safetyCheckInRepository.markMissed(checkInId)

                // Second escalation: 2 more minutes -> auto SOS
                delay(120_000)
                if (_uiState.value.showCheckInDialog) {
                    _uiState.update { it.copy(checkInEscalationLevel = 2) }
                    // UML State Chart: Hiking -> DangerDetected -> triggerSOS() -> AlertSent
                    hikeSessionRepository.updateStatus(currentSessionId, HikeStatus.Emergency)
                    val loc = _uiState.value.currentLocation ?: return@launch
                    emergencyService.sendSosToContacts(loc)
                    val trail = _uiState.value.trail
                    val hikerName = sessionManager.getHikerName()
                    val msg = "Missed safety check-in - no response"
                    val alertId = UUID.randomUUID().toString()
                    val sosAlert = SOSAlert(
                        alertId = alertId, sessionId = currentSessionId,
                        hikerId = sessionManager.getUserId(), hikerName = hikerName,
                        trailId = trail?.id ?: "", trailName = trail?.name ?: "",
                        alertType = "CHECKIN_MISSED",
                        latitude = loc.latitude, longitude = loc.longitude,
                        message = msg
                    )
                    sosAlertDao.insert(sosAlert.toSosEntity())
                    // UML: Notification entity persisted
                    notificationRepository.sendNotification(
                        Notification(
                            notificationId = UUID.randomUUID().toString(),
                            alertId = alertId, recipientId = "officer_1",
                            message = "MISSED CHECK-IN: $hikerName on ${trail?.name}",
                            status = "sent"
                        )
                    )
                    sosNotificationService.sendSosAlert(
                        hikerName = hikerName, alertType = "CHECKIN_MISSED",
                        latitude = loc.latitude, longitude = loc.longitude, message = msg
                    )
                }
            }
        }
    }

    fun acknowledgeCheckIn() {
        // UML: SafetyCheckIn.responseStatus = "confirmed"
        currentCheckInId?.let { id ->
            viewModelScope.launch { safetyCheckInRepository.confirmCheckIn(id) }
        }
        _uiState.update {
            it.copy(
                showCheckInDialog = false,
                checkInsCompleted = it.checkInsCompleted + 1,
                checkInMissed = false,
                checkInEscalationLevel = 0
            )
        }
    }

    // ===== FR-203: Fall Detection =====

    private fun onFallDetected() {
        _uiState.update { it.copy(showFallDetectedDialog = true, fallCountdown = 30) }
        emergencyService.triggerSOSVibration()

        // Start countdown - if user doesn't respond in 30s, auto-SOS
        fallCountdownJob?.cancel()
        fallCountdownJob = viewModelScope.launch {
            for (i in 30 downTo 0) {
                _uiState.update { it.copy(fallCountdown = i) }
                delay(1000)
                if (!_uiState.value.showFallDetectedDialog) return@launch
            }
            // No response - escalate to SOS
            // UML State Chart: Hiking -> DangerDetected -> triggerSOS() -> AlertSent
            hikeSessionRepository.updateStatus(currentSessionId, HikeStatus.Emergency)
            val loc = _uiState.value.currentLocation ?: return@launch
            emergencyService.sendSosToContacts(loc)
            val trail = _uiState.value.trail
            val hikerName = sessionManager.getHikerName()
            val fallMsg = "Fall detected - no response after 30s"
            val alertId = UUID.randomUUID().toString()
            val sosAlert = SOSAlert(
                alertId = alertId, sessionId = currentSessionId,
                hikerId = sessionManager.getUserId(), hikerName = hikerName,
                trailId = trail?.id ?: "", trailName = trail?.name ?: "",
                alertType = "FALL_DETECTED",
                latitude = loc.latitude, longitude = loc.longitude,
                message = fallMsg
            )
            sosAlertDao.insert(sosAlert.toSosEntity())
            notificationRepository.sendNotification(
                Notification(
                    notificationId = UUID.randomUUID().toString(),
                    alertId = alertId, recipientId = "officer_1",
                    message = "FALL DETECTED: $hikerName on ${trail?.name}",
                    status = "sent"
                )
            )
            sosNotificationService.sendSosAlert(
                hikerName = hikerName, alertType = "FALL_DETECTED",
                latitude = loc.latitude, longitude = loc.longitude, message = fallMsg
            )
            _uiState.update { it.copy(showFallDetectedDialog = false) }
        }
    }

    fun dismissFallAlert() {
        _uiState.update { it.copy(showFallDetectedDialog = false) }
        fallCountdownJob?.cancel()
        fallDetectionService.resetFallDetection()
    }

    // ===== Location Updates with Zone Checks (FR-208, FR-209) =====

    fun onLocationUpdate(lat: Double, lng: Double, altitude: Double) {
        val newPoint = LatLng(lat, lng)
        val currentState = _uiState.value
        val trail = currentState.trail ?: return

        if (currentState.isPaused) return

        // UML: Location entity - persist tracked location
        viewModelScope.launch {
            hikeSessionRepository.addLocation(
                com.hikingtrailnavigator.app.domain.model.Location(
                    id = UUID.randomUUID().toString(),
                    latitude = lat, longitude = lng,
                    timestamp = System.currentTimeMillis(),
                    sessionId = currentSessionId
                )
            )
        }

        val newRoute = currentState.route + newPoint

        // Calculate distance traveled
        var newDistance = currentState.distanceTraveled
        if (currentState.route.isNotEmpty()) {
            val last = currentState.route.last()
            newDistance += calculateHaversine(last, newPoint)
        }

        // Distance remaining & ETA (FR-102)
        val totalTrailDist = trail.distance
        val distRemaining = (totalTrailDist - newDistance).coerceAtLeast(0.0)
        val elapsed = System.currentTimeMillis() - startTime
        val avgSpeedKmh = if (elapsed > 0) newDistance / (elapsed / 3600000.0) else 0.0
        val etaMs = if (avgSpeedKmh > 0.1) (distRemaining / avgSpeedKmh * 3600000).toLong() else 0L
        val etaStr = if (etaMs > 0) formatDuration(etaMs) else "--"

        // Pace (FR-104)
        val paceMinPerKm = if (newDistance > 0.01) (elapsed / 60000.0) / newDistance else 0.0
        val paceStr = if (paceMinPerKm > 0) String.format("%.1f min/km", paceMinPerKm) else "--"

        // Current speed
        val loc = Location("").apply {
            latitude = lat; longitude = lng; this.altitude = altitude
        }
        val speedKmh = if (lastLocation != null) {
            val dt = 5.0 / 3600.0 // ~5 seconds between updates
            val segDist = calculateHaversine(
                LatLng(lastLocation!!.latitude, lastLocation!!.longitude), newPoint
            )
            if (dt > 0) segDist / dt else 0.0
        } else 0.0

        // Elevation gain
        var newElevation = currentState.elevationGained
        lastLocation?.let { prev ->
            if (loc.altitude > prev.altitude) {
                newElevation += (loc.altitude - prev.altitude).toInt()
            }
        }
        lastLocation = loc

        // Check trail deviation (FR-205)
        val deviation = geofencingService.getDistanceFromTrail(newPoint, trail.coordinates)
        val isDeviating = deviation > 100

        // Check danger zones (FR-208) - proactive alert
        val currentDangerZone = dangerZones.firstOrNull { zone ->
            geofencingService.isInsideZone(newPoint, zone.center, zone.radius)
        }
        val enteredNewDangerZone = currentDangerZone != null && currentState.insideDangerZone?.id != currentDangerZone.id

        // FR-208: Vibrate on entering danger zone
        if (enteredNewDangerZone) {
            emergencyService.triggerSOSVibration()
        }

        // Check no-coverage zones (FR-209) - proactive alert
        val inNoCoverage = noCoverageZones.any { zone ->
            geofencingService.isInsideZone(newPoint, zone.center, zone.radius)
        }
        val enteredNoCoverage = inNoCoverage && !currentState.insideNoCoverageZone

        // FR-210: Check low activity zones
        val currentLowActivityZone = lowActivityZones.firstOrNull { zone ->
            geofencingService.isInsideZone(newPoint, zone.center, zone.radius)
        }
        val enteredLowActivityZone = currentLowActivityZone != null && currentState.insideLowActivityZone?.id != currentLowActivityZone.id

        // FR-102: Turn-by-turn - find nearest upcoming waypoint
        val coords = trail.coordinates
        var nextIdx = currentState.nextWaypointIndex
        if (nextIdx < coords.size) {
            val distToNext = geofencingService.haversineMeters(newPoint, coords[nextIdx])
            if (distToNext < 30 && nextIdx < coords.size - 1) {
                nextIdx++ // Move to next waypoint when within 30m
            }
        }
        val distToWaypoint = if (nextIdx < coords.size) {
            geofencingService.haversineMeters(newPoint, coords[nextIdx])
        } else 0.0
        val bearingStr = if (nextIdx < coords.size) {
            getBearingDirection(newPoint, coords[nextIdx])
        } else "Arrived"

        _uiState.update {
            it.copy(
                currentLocation = newPoint,
                route = newRoute,
                distanceTraveled = newDistance,
                distanceRemaining = distRemaining,
                estimatedTimeRemaining = etaStr,
                elevationGained = newElevation,
                currentSpeed = speedKmh,
                avgPace = paceStr,
                elapsedTime = System.currentTimeMillis() - startTime,
                isDeviating = isDeviating,
                deviationDistance = deviation,
                insideDangerZone = currentDangerZone,
                showDangerZoneAlert = enteredNewDangerZone || it.showDangerZoneAlert,
                insideNoCoverageZone = inNoCoverage,
                showNoCoverageAlert = enteredNoCoverage || it.showNoCoverageAlert,
                // FR-102: Turn-by-turn
                nextWaypointIndex = nextIdx,
                distanceToNextWaypoint = distToWaypoint,
                bearingToNextWaypoint = bearingStr,
                // FR-210: Low activity zone
                insideLowActivityZone = currentLowActivityZone,
                showLowActivityAlert = enteredLowActivityZone || it.showLowActivityAlert
            )
        }
    }

    fun dismissDangerZoneAlert() {
        _uiState.update { it.copy(showDangerZoneAlert = false) }
    }

    fun dismissNoCoverageAlert() {
        _uiState.update { it.copy(showNoCoverageAlert = false) }
    }

    // FR-210: Dismiss low activity zone alert
    fun dismissLowActivityAlert() {
        _uiState.update { it.copy(showLowActivityAlert = false) }
    }

    fun toggleSatellite() {
        _uiState.update { it.copy(useSatellite = !it.useSatellite) }
    }

    // FR-211: Layer toggle controls
    fun toggleLayerPanel() {
        _uiState.update { it.copy(showLayerPanel = !it.showLayerPanel) }
    }
    fun toggleDangerZoneLayer() {
        _uiState.update { it.copy(showDangerZoneLayer = !it.showDangerZoneLayer) }
    }
    fun toggleNoCoverageLayer() {
        _uiState.update { it.copy(showNoCoverageLayer = !it.showNoCoverageLayer) }
    }
    fun toggleLowActivityLayer() {
        _uiState.update { it.copy(showLowActivityLayer = !it.showLowActivityLayer) }
    }

    // FR-105: Post-hike difficulty rating
    fun showDifficultyRating() {
        _uiState.update { it.copy(showDifficultyRating = true) }
    }
    fun setDifficultyRating(rating: Int) {
        _uiState.update { it.copy(userDifficultyRating = rating) }
    }
    fun dismissDifficultyRating() {
        _uiState.update { it.copy(showDifficultyRating = false) }
    }

    fun togglePause() {
        val newPaused = !_uiState.value.isPaused
        _uiState.update { it.copy(isPaused = newPaused) }
        // UML State Chart: update session status
        viewModelScope.launch {
            hikeSessionRepository.updateStatus(
                currentSessionId,
                if (newPaused) HikeStatus.Paused else HikeStatus.Active
            )
        }
    }

    fun showCheckIn() {
        _uiState.update { it.copy(showCheckInDialog = true) }
    }

    fun showEndHikeDialog() {
        _uiState.update { it.copy(showEndDialog = true) }
    }

    fun dismissEndDialog() {
        _uiState.update { it.copy(showEndDialog = false) }
    }

    fun endHike(onComplete: () -> Unit) {
        // Show difficulty rating dialog first
        _uiState.update { it.copy(showEndDialog = false, showDifficultyRating = true) }
        pendingOnComplete = onComplete
    }

    private var pendingOnComplete: (() -> Unit)? = null

    fun submitRatingAndEndHike() {
        val state = _uiState.value
        val trail = state.trail ?: return
        val onComplete = pendingOnComplete ?: return

        fallDetectionService.stopMonitoring()
        checkInTimerJob?.cancel()

        viewModelScope.launch {
            hikeSessionRepository.endSession(currentSessionId)
            sessionManager.clearActiveSession()

            val activity = HikeActivity(
                id = UUID.randomUUID().toString(),
                trailId = trail.id,
                trailName = trail.name,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                distance = state.distanceTraveled,
                duration = state.elapsedTime,
                elevationGain = state.elevationGained,
                route = state.route,
                checkIns = state.checkInsCompleted,
                userDifficultyRating = state.userDifficultyRating
            )
            activityRepository.saveActivity(activity)
            _uiState.update { it.copy(showDifficultyRating = false) }
            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        fallDetectionService.stopMonitoring()
        checkInTimerJob?.cancel()
        fallCountdownJob?.cancel()
    }

    // FR-102: Calculate compass bearing direction between two points
    private fun getBearingDirection(from: LatLng, to: LatLng): String {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val y = Math.sin(dLng) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng)
        val bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
        return when {
            bearing < 22.5 || bearing >= 337.5 -> "N"
            bearing < 67.5 -> "NE"
            bearing < 112.5 -> "E"
            bearing < 157.5 -> "SE"
            bearing < 202.5 -> "S"
            bearing < 247.5 -> "SW"
            bearing < 292.5 -> "W"
            else -> "NW"
        }
    }

    private fun calculateHaversine(p1: LatLng, p2: LatLng): Double {
        val r = 6371000.0
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c / 1000.0 // km
    }
}
