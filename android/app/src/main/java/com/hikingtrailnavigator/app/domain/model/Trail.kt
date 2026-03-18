package com.hikingtrailnavigator.app.domain.model

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
    val confirmations: Int = 0
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
    val checkIns: Int
)

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String
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
