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
    val elevationProfile: String // JSON
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
        elevationProfile = gson.fromJson(elevationProfile, elevType)
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
        elevationProfile = gson.toJson(elevationProfile)
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
