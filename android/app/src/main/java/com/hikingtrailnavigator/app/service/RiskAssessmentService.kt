package com.hikingtrailnavigator.app.service

import com.hikingtrailnavigator.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskAssessmentService @Inject constructor() {

    fun assessTrailRisk(
        trail: Trail,
        dangerZones: List<DangerZone>,
        weather: WeatherData? = null
    ): RiskAssessment {
        var score = 0

        // Difficulty factor
        score += when (trail.difficulty) {
            Difficulty.Easy -> 5
            Difficulty.Moderate -> 15
            Difficulty.Hard -> 30
            Difficulty.Expert -> 45
        }

        // Elevation gain factor
        score += when {
            trail.elevationGain > 1000 -> 20
            trail.elevationGain > 500 -> 10
            else -> 5
        }

        // Coverage factor
        score += when (trail.coverageStatus) {
            CoverageStatus.Full -> 0
            CoverageStatus.Partial -> 10
            CoverageStatus.None -> 25
        }

        // Nearby danger zones
        val nearbyDangers = dangerZones.filter { zone ->
            trail.coordinates.any { point ->
                haversineMeters(point, zone.center) < zone.radius + 500
            }
        }
        score += nearbyDangers.size * 10

        // Weather factor
        weather?.let {
            if (it.rainProbability > 60) score += 15
            if (it.windSpeed > 40) score += 10
            if (it.uvIndex > 8) score += 5
            score += it.alerts.size * 10
        }

        val level = when {
            score >= 60 -> "critical"
            score >= 40 -> "high"
            score >= 20 -> "medium"
            else -> "low"
        }

        val factors = mutableListOf<String>()
        if (trail.difficulty == Difficulty.Hard || trail.difficulty == Difficulty.Expert) {
            factors.add("Difficult terrain")
        }
        if (trail.coverageStatus != CoverageStatus.Full) {
            factors.add("Limited network coverage")
        }
        if (nearbyDangers.isNotEmpty()) {
            factors.add("${nearbyDangers.size} danger zone(s) nearby")
        }
        weather?.let {
            if (it.rainProbability > 60) factors.add("High rain probability")
            if (it.alerts.isNotEmpty()) factors.add("Weather alerts active")
        }

        return RiskAssessment(level = level, score = score, factors = factors)
    }

    private fun haversineMeters(p1: LatLng, p2: LatLng): Double {
        val r = 6371000.0
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

data class RiskAssessment(
    val level: String,
    val score: Int,
    val factors: List<String>
)
