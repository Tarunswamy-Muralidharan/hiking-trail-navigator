package com.hikingtrailnavigator.app.service

import com.hikingtrailnavigator.app.domain.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class GeofencingService @Inject constructor() {

    fun getDistanceFromTrail(point: LatLng, trailCoordinates: List<LatLng>): Double {
        if (trailCoordinates.isEmpty()) return 0.0
        return trailCoordinates.minOf { haversineMeters(point, it) }
    }

    fun isInsideZone(point: LatLng, center: LatLng, radiusMeters: Double): Boolean {
        return haversineMeters(point, center) <= radiusMeters
    }

    fun haversineMeters(p1: LatLng, p2: LatLng): Double {
        val r = 6371000.0
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
