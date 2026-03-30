package com.hikingtrailnavigator.app.domain.model

// ── UML User Hierarchy (Exp 3: Class Diagram) ──

enum class UserRole { Hiker, ForestOfficer, Admin }

open class User(
    open val userId: String,
    open val name: String,
    open val email: String,
    open val phoneNumber: String,
    open val role: UserRole
)

data class Hiker(
    override val userId: String,
    override val name: String,
    override val email: String,
    override val phoneNumber: String,
    val experienceLevel: String = "Beginner",
    val emergencyContact: String = "",
    val currentTrailId: String? = null
) : User(userId, name, email, phoneNumber, UserRole.Hiker)

data class ForestOfficer(
    override val userId: String,
    override val name: String,
    override val email: String,
    override val phoneNumber: String,
    val badgeNumber: String,
    val assignedRegion: String
) : User(userId, name, email, phoneNumber, UserRole.ForestOfficer) {
    fun respondToAlert(alertId: String): Boolean = true
    fun rescueHiker(hikerId: String): Boolean = true
}

data class Admin(
    override val userId: String,
    override val name: String,
    override val email: String,
    override val phoneNumber: String,
    val assignedRegion: String = ""
) : User(userId, name, email, phoneNumber, UserRole.Admin) {
    fun manageTrails(): Boolean = true
    fun manageDangerZones(): Boolean = true
}

// ── UML HikeSession (Exp 3: Class Diagram) ──

enum class HikeStatus { NotStarted, Active, Paused, Completed, Emergency }

data class HikeSession(
    val sessionId: String,
    val hikerId: String,
    val trailId: String,
    val trailName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: HikeStatus = HikeStatus.NotStarted
) {
    fun startHike(): HikeSession = copy(status = HikeStatus.Active, startTime = System.currentTimeMillis())
    fun endHike(): HikeSession = copy(status = HikeStatus.Completed, endTime = System.currentTimeMillis())
    fun trackLocation(): Boolean = status == HikeStatus.Active
}

// ── UML Location (Exp 3: Class Diagram) ──

data class Location(
    val id: String = "",
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String = ""
)

// ── UML SOSAlert (Exp 3: Class Diagram) ──

data class SOSAlert(
    val alertId: String,
    val sessionId: String = "",
    val hikerId: String = "",
    val hikerName: String = "",
    val trailId: String = "",
    val trailName: String = "",
    val alertType: String = "SOS_BUTTON",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdTime: Long = System.currentTimeMillis(),
    val status: String = "active",
    val message: String = ""
) {
    fun resolve(): SOSAlert = copy(status = "resolved")
}

// ── UML Notification (Exp 3: Class Diagram) ──

data class Notification(
    val notificationId: String,
    val alertId: String = "",
    val recipientId: String = "",
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, sent, read
)

// ── UML SafetyCheckIn (Exp 3: Class Diagram) ──

data class SafetyCheckIn(
    val checkInId: String,
    val sessionId: String,
    val scheduledTime: Long,
    val responseStatus: String = "pending" // pending, confirmed, missed
)

// ── Trail and existing models ──

data class Trail(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: Difficulty,
    val distance: Double, // km
    val estimatedDuration: String,
    val elevationGain: Int, // meters
    val rating: Double,
    val coordinates: List<LatLng>,
    val startPoint: LatLng,
    val endPoint: LatLng,
    val hazards: List<String>,
    val region: String,
    val popularity: Int,
    val coverageStatus: CoverageStatus,
    val elevationProfile: List<ElevationPoint>,
    val schedule: String = "Open all days",
    val checkInIntervalMinutes: Int = 30
)

data class LatLng(val latitude: Double, val longitude: Double)

data class ElevationPoint(val distance: Double, val elevation: Int)

enum class Difficulty { Easy, Moderate, Hard, Expert }

enum class CoverageStatus { Full, Partial, None }

data class DangerZone(
    val id: String,
    val name: String,
    val center: LatLng,
    val radius: Double, // meters
    val type: DangerType,
    val severity: Severity,
    val description: String,
    val verified: Boolean
)

enum class DangerType { Wildlife, Landslide, Restricted, Flood, Terrain }
enum class Severity { Low, Medium, High, Critical }

data class NoCoverageZone(
    val id: String,
    val name: String,
    val center: LatLng,
    val radius: Double,
    val description: String
)

data class HazardReport(
    val id: String = "",
    val type: String,
    val severity: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val reportedAt: Long = System.currentTimeMillis(),
    val confirmations: Int = 0,
    val expiresAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // 7 days default
    val isVerified: Boolean = false
)

data class HikeActivity(
    val id: String,
    val trailId: String,
    val trailName: String,
    val startTime: Long,
    val endTime: Long,
    val distance: Double,
    val duration: Long, // millis
    val elevationGain: Int,
    val route: List<LatLng>,
    val checkIns: Int,
    val userDifficultyRating: Int = 0 // 0=not rated, 1-5 scale
)

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String
)

// FR-210: Low activity / unexplored area
data class LowActivityZone(
    val id: String,
    val name: String,
    val center: LatLng,
    val radius: Double, // meters
    val activityLevel: String, // "unexplored", "low", "moderate"
    val description: String
)

data class UserPreferences(
    val checkInInterval: Int = 60,
    val fallDetectionEnabled: Boolean = true,
    val silentSOSEnabled: Boolean = true,
    val deviationAlertDistance: Int = 100,
    val gpsAccuracy: String = "high",
    val locationShareEnabled: Boolean = true
)

data class WeatherData(
    val temperature: Int,
    val humidity: Int,
    val windSpeed: Int,
    val conditions: String,
    val rainProbability: Int,
    val uvIndex: Int,
    val alerts: List<WeatherAlert> = emptyList()
)

data class WeatherAlert(
    val title: String,
    val description: String,
    val severity: String
)
