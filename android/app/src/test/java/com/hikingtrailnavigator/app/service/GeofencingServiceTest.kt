package com.hikingtrailnavigator.app.service

import com.hikingtrailnavigator.app.domain.model.LatLng
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * UNIT TESTS for GeofencingService
 *
 * What: Tests geofencing logic - distance calculations, zone detection.
 * Why: Critical for safety features (FR-205 trail deviation, FR-208 danger zone alerts,
 *      FR-209 no-coverage warnings). Incorrect distances could mean missed warnings.
 * How: Runs on JVM. Uses known GPS coordinates and verifies distance calculations.
 * SRS: Covers FR-205, FR-208, FR-209
 */
class GeofencingServiceTest {

    private lateinit var service: GeofencingService

    @Before
    fun setUp() {
        service = GeofencingService()
    }

    // ==================== Haversine Distance Tests ====================

    @Test
    fun `same point should return zero distance`() {
        val point = LatLng(11.0168, 76.9558) // PSG iTech
        val distance = service.haversineMeters(point, point)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `known distance between PSG iTech and Marudhamalai should be approximately correct`() {
        val psgITech = LatLng(11.0168, 76.9558)
        val marudhamalai = LatLng(11.0453, 76.9340) // ~4km away

        val distance = service.haversineMeters(psgITech, marudhamalai)

        // Should be approximately 3.5-4.5 km
        assertTrue("Distance should be > 3000m, was $distance", distance > 3000)
        assertTrue("Distance should be < 5000m, was $distance", distance < 5000)
    }

    @Test
    fun `distance should be symmetric - A to B equals B to A`() {
        val pointA = LatLng(11.0168, 76.9558)
        val pointB = LatLng(11.4064, 76.6932) // Ooty

        val distAB = service.haversineMeters(pointA, pointB)
        val distBA = service.haversineMeters(pointB, pointA)

        assertEquals(distAB, distBA, 0.001) // Should be exactly equal
    }

    // ==================== Zone Detection Tests ====================

    @Test
    fun `point inside zone should return true`() {
        val center = LatLng(11.0168, 76.9558)
        val pointInside = LatLng(11.0169, 76.9559) // ~15m away

        assertTrue(service.isInsideZone(pointInside, center, 1000.0)) // 1km radius
    }

    @Test
    fun `point outside zone should return false`() {
        val center = LatLng(11.0168, 76.9558)
        val pointFarAway = LatLng(11.4064, 76.6932) // Ooty, ~45km away

        assertFalse(service.isInsideZone(pointFarAway, center, 1000.0))
    }

    @Test
    fun `point exactly on zone boundary should return true`() {
        val center = LatLng(0.0, 0.0)
        val distance = service.haversineMeters(center, LatLng(0.009, 0.0)) // ~1km
        val radius = distance

        // Point at exactly the radius should be inside (<=)
        assertTrue(service.isInsideZone(LatLng(0.009, 0.0), center, radius))
    }

    // ==================== Trail Distance Tests ====================

    @Test
    fun `distance from trail should return minimum distance to nearest coordinate`() {
        val trailCoordinates = listOf(
            LatLng(11.0, 76.9),
            LatLng(11.01, 76.91),
            LatLng(11.02, 76.92)
        )
        val nearSecondPoint = LatLng(11.011, 76.911) // Close to second coordinate

        val distance = service.getDistanceFromTrail(nearSecondPoint, trailCoordinates)

        // Should be closest to the second coordinate (~150m)
        assertTrue("Distance should be < 500m, was $distance", distance < 500)
    }

    @Test
    fun `empty trail coordinates should return zero`() {
        val distance = service.getDistanceFromTrail(LatLng(11.0, 76.9), emptyList())
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `point far from trail should indicate deviation (FR-205)`() {
        val trailCoordinates = listOf(
            LatLng(11.0, 76.9),
            LatLng(11.01, 76.91)
        )
        val deviatedPoint = LatLng(11.05, 76.95) // Far from trail

        val distance = service.getDistanceFromTrail(deviatedPoint, trailCoordinates)

        // FR-205: deviation threshold is 100m
        assertTrue("Deviated point should be > 100m from trail", distance > 100)
    }
}
