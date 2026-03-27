package com.hikingtrailnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hikingtrailnavigator.app.domain.model.*

@Entity(tableName = "trails")
@TypeConverters(Converters::class)
data class TrailEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val distance: Double,
    val estimatedDuration: String,
    val elevationGain: Int,
    val rating: Double,
    val coordinates: String, // JSON
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val hazards: String, // JSON
    val region: String,
    val popularity: Int,
    val coverageStatus: String,
    val elevationProfile: String, // JSON
    val schedule: String = "Open all days",
    val checkInIntervalMinutes: Int = 30
)

@Entity(tableName = "danger_zones")
data class DangerZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radius: Double,
    val type: String,
    val severity: String,
    val description: String,
    val verified: Boolean
)

@Entity(tableName = "no_coverage_zones")
data class NoCoverageZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radius: Double,
    val description: String
)

@Entity(tableName = "hazard_reports")
data class HazardReportEntity(
    @PrimaryKey val id: String,
    val type: String,
    val severity: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val reportedAt: Long,
    val confirmations: Int
)

@Entity(tableName = "hike_activities")
@TypeConverters(Converters::class)
data class HikeActivityEntity(
    @PrimaryKey val id: String,
    val trailId: String,
    val trailName: String,
    val startTime: Long,
    val endTime: Long,
    val distance: Double,
    val duration: Long,
    val elevationGain: Int,
    val route: String, // JSON
    val checkIns: Int
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val relation: String
)

@Entity(tableName = "active_hiker_sessions")
data class ActiveHikerSessionEntity(
    @PrimaryKey val id: String,
    val hikerName: String,
    val trailId: String,
    val trailName: String,
    val startTime: Long,
    val lastCheckInTime: Long,
    val lastLat: Double,
    val lastLng: Double,
    val isActive: Boolean,
    val missedCheckIns: Int = 0
)

@Entity(tableName = "sos_alerts")
data class SosAlertEntity(
    @PrimaryKey val id: String,
    val hikerName: String,
    val trailId: String,
    val trailName: String,
    val alertType: String,  // SOS_BUTTON, FALL_DETECTED, CHECKIN_MISSED
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isResolved: Boolean = false,
    val message: String = ""
)

// ── UML: User Entity (supports Hiker, ForestOfficer, Admin via role) ──
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val role: String, // Hiker, ForestOfficer, Admin
    val experienceLevel: String? = null,    // Hiker-specific
    val emergencyContact: String? = null,   // Hiker-specific
    val currentTrailId: String? = null,     // Hiker-specific
    val badgeNumber: String? = null,        // ForestOfficer-specific
    val assignedRegion: String? = null,     // ForestOfficer/Admin-specific
    val passwordHash: String = ""
)

// ── UML: HikeSession Entity ──
@Entity(tableName = "hike_sessions")
data class HikeSessionEntity(
    @PrimaryKey val sessionId: String,
    val hikerId: String,
    val trailId: String,
    val trailName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String = "NotStarted" // NotStarted, Active, Paused, Completed, Emergency
)

// ── UML: Location Entity (tracked points during hike) ──
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val sessionId: String
)

// ── UML: Notification Entity ──
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val alertId: String = "",
    val recipientId: String = "",
    val message: String,
    val timestamp: Long,
    val status: String = "pending" // pending, sent, read
)

// ── UML: SafetyCheckIn Entity ──
@Entity(tableName = "safety_checkins")
data class SafetyCheckInEntity(
    @PrimaryKey val checkInId: String,
    val sessionId: String,
    val scheduledTime: Long,
    val responseStatus: String = "pending" // pending, confirmed, missed
)

@Entity(tableName = "route_warnings")
data class RouteWarningEntity(
    @PrimaryKey val id: String,
    val trailId: String,
    val latitude: Double,
    val longitude: Double,
    val warningType: String,
    val description: String,
    val reportedBy: String,
    val reportedAt: Long,
    val upvotes: Int = 0,
    val isActive: Boolean = true
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }
}

// Extension functions to convert between Entity and Domain
fun TrailEntity.toDomain(): Trail {
    val gson = Gson()
    val coordsType = object : TypeToken<List<LatLng>>() {}.type
    val elevType = object : TypeToken<List<ElevationPoint>>() {}.type
    val hazardsType = object : TypeToken<List<String>>() {}.type

    return Trail(
        id = id,
        name = name,
        description = description,
        difficulty = Difficulty.valueOf(difficulty),
        distance = distance,
        estimatedDuration = estimatedDuration,
        elevationGain = elevationGain,
        rating = rating,
        coordinates = gson.fromJson(coordinates, coordsType),
        startPoint = LatLng(startLat, startLng),
        endPoint = LatLng(endLat, endLng),
        hazards = gson.fromJson(hazards, hazardsType),
        region = region,
        popularity = popularity,
        coverageStatus = CoverageStatus.valueOf(coverageStatus),
        elevationProfile = gson.fromJson(elevationProfile, elevType),
        schedule = schedule,
        checkInIntervalMinutes = checkInIntervalMinutes
    )
}

fun Trail.toEntity(): TrailEntity {
    val gson = Gson()
    return TrailEntity(
        id = id,
        name = name,
        description = description,
        difficulty = difficulty.name,
        distance = distance,
        estimatedDuration = estimatedDuration,
        elevationGain = elevationGain,
        rating = rating,
        coordinates = gson.toJson(coordinates),
        startLat = startPoint.latitude,
        startLng = startPoint.longitude,
        endLat = endPoint.latitude,
        endLng = endPoint.longitude,
        hazards = gson.toJson(hazards),
        region = region,
        popularity = popularity,
        coverageStatus = coverageStatus.name,
        elevationProfile = gson.toJson(elevationProfile),
        schedule = schedule,
        checkInIntervalMinutes = checkInIntervalMinutes
    )
}

fun DangerZoneEntity.toDomain() = DangerZone(
    id = id, name = name, center = LatLng(centerLat, centerLng),
    radius = radius, type = DangerType.valueOf(type),
    severity = Severity.valueOf(severity), description = description, verified = verified
)

fun NoCoverageZoneEntity.toDomain() = NoCoverageZone(
    id = id, name = name, center = LatLng(centerLat, centerLng),
    radius = radius, description = description
)

fun HazardReportEntity.toDomain() = HazardReport(
    id = id, type = type, severity = severity,
    latitude = latitude, longitude = longitude,
    description = description, reportedAt = reportedAt, confirmations = confirmations
)

fun HazardReport.toEntity() = HazardReportEntity(
    id = id, type = type, severity = severity,
    latitude = latitude, longitude = longitude,
    description = description, reportedAt = reportedAt, confirmations = confirmations
)

fun HikeActivityEntity.toDomain(): HikeActivity {
    val gson = Gson()
    val routeType = object : TypeToken<List<LatLng>>() {}.type
    return HikeActivity(
        id = id, trailId = trailId, trailName = trailName,
        startTime = startTime, endTime = endTime, distance = distance,
        duration = duration, elevationGain = elevationGain,
        route = gson.fromJson(route, routeType), checkIns = checkIns
    )
}

fun HikeActivity.toEntity(): HikeActivityEntity {
    val gson = Gson()
    return HikeActivityEntity(
        id = id, trailId = trailId, trailName = trailName,
        startTime = startTime, endTime = endTime, distance = distance,
        duration = duration, elevationGain = elevationGain,
        route = gson.toJson(route), checkIns = checkIns
    )
}

fun EmergencyContactEntity.toDomain() = EmergencyContact(
    id = id, name = name, phone = phone, relation = relation
)

fun EmergencyContact.toEntity() = EmergencyContactEntity(
    id = id, name = name, phone = phone, relation = relation
)

// ── UML Entity conversions ──

fun UserEntity.toDomainUser(): User = when (role) {
    "ForestOfficer" -> ForestOfficer(
        userId = userId, name = name, email = email, phoneNumber = phoneNumber,
        badgeNumber = badgeNumber ?: "", assignedRegion = assignedRegion ?: ""
    )
    "Admin" -> Admin(
        userId = userId, name = name, email = email, phoneNumber = phoneNumber,
        assignedRegion = assignedRegion ?: ""
    )
    else -> Hiker(
        userId = userId, name = name, email = email, phoneNumber = phoneNumber,
        experienceLevel = experienceLevel ?: "Beginner",
        emergencyContact = emergencyContact ?: "",
        currentTrailId = currentTrailId
    )
}

fun User.toEntity(): UserEntity = UserEntity(
    userId = userId, name = name, email = email, phoneNumber = phoneNumber,
    role = role.name,
    experienceLevel = if (this is Hiker) experienceLevel else null,
    emergencyContact = if (this is Hiker) emergencyContact else null,
    currentTrailId = if (this is Hiker) currentTrailId else null,
    badgeNumber = if (this is ForestOfficer) badgeNumber else null,
    assignedRegion = when (this) {
        is ForestOfficer -> assignedRegion
        is Admin -> assignedRegion
        else -> null
    }
)

fun HikeSessionEntity.toDomain() = HikeSession(
    sessionId = sessionId, hikerId = hikerId, trailId = trailId,
    trailName = trailName, startTime = startTime, endTime = endTime,
    status = HikeStatus.valueOf(status)
)

fun HikeSession.toEntity() = HikeSessionEntity(
    sessionId = sessionId, hikerId = hikerId, trailId = trailId,
    trailName = trailName, startTime = startTime, endTime = endTime,
    status = status.name
)

fun LocationEntity.toDomain() = Location(
    id = id, latitude = latitude, longitude = longitude,
    timestamp = timestamp, sessionId = sessionId
)

fun Location.toEntity() = LocationEntity(
    id = id, latitude = latitude, longitude = longitude,
    timestamp = timestamp, sessionId = sessionId
)

fun SosAlertEntity.toDomainAlert() = SOSAlert(
    alertId = id, hikerName = hikerName, trailId = trailId,
    trailName = trailName, alertType = alertType,
    latitude = latitude, longitude = longitude,
    createdTime = timestamp, status = if (isResolved) "resolved" else "active",
    message = message
)

fun SOSAlert.toSosEntity() = SosAlertEntity(
    id = alertId, hikerName = hikerName, trailId = trailId,
    trailName = trailName, alertType = alertType,
    latitude = latitude, longitude = longitude,
    timestamp = createdTime, isResolved = status == "resolved",
    message = message
)

fun NotificationEntity.toDomain() = Notification(
    notificationId = notificationId, alertId = alertId,
    recipientId = recipientId, message = message,
    timestamp = timestamp, status = status
)

fun Notification.toEntity() = NotificationEntity(
    notificationId = notificationId, alertId = alertId,
    recipientId = recipientId, message = message,
    timestamp = timestamp, status = status
)

fun SafetyCheckInEntity.toDomain() = SafetyCheckIn(
    checkInId = checkInId, sessionId = sessionId,
    scheduledTime = scheduledTime, responseStatus = responseStatus
)

fun SafetyCheckIn.toEntity() = SafetyCheckInEntity(
    checkInId = checkInId, sessionId = sessionId,
    scheduledTime = scheduledTime, responseStatus = responseStatus
)
