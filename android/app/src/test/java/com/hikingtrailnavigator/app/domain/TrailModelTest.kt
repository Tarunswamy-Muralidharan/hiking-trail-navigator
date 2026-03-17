package com.hikingtrailnavigator.app.domain

import com.hikingtrailnavigator.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * UNIT TESTS for Domain Models
 *
 * What: Tests data model creation, enum values, and entity conversion.
 * Why: Domain models are the foundation of the app. Incorrect model behavior
 *      would cascade errors throughout all features.
 * How: Runs on JVM. Creates model instances and verifies properties.
 * SRS: Validates data structures used across all functional requirements.
 */
class TrailModelTest {

    // ==================== Trail Model Tests ====================

    @Test
    fun `trail should store all properties correctly`() {
        val trail = Trail(
            id = "trail-1",
            name = "Vellingiri Hills Trek",
            description = "Sacred trek to the Vellingiri peak",
            difficulty = Difficulty.Hard,
            distance = 22.0,
            estimatedDuration = "8-10 hours",
            elevationGain = 1500,
            rating = 4.5,
            coordinates = listOf(LatLng(11.0, 76.9)),
            startPoint = LatLng(10.95, 76.82),
            endPoint = LatLng(11.01, 76.78),
            hazards = listOf("Steep terrain", "Leeches", "Elephants"),
            region = "Coimbatore",
            popularity = 95,
            coverageStatus = CoverageStatus.None,
            elevationProfile = listOf(ElevationPoint(0.0, 500), ElevationPoint(22.0, 2000))
        )

        assertEquals("trail-1", trail.id)
        assertEquals("Vellingiri Hills Trek", trail.name)
        assertEquals(Difficulty.Hard, trail.difficulty)
        assertEquals(22.0, trail.distance, 0.01)
        assertEquals(1500, trail.elevationGain)
        assertEquals(CoverageStatus.None, trail.coverageStatus)
        assertEquals(3, trail.hazards.size)
    }

    // ==================== Difficulty Enum Tests ====================

    @Test
    fun `difficulty enum should have exactly 4 values`() {
        val values = Difficulty.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(Difficulty.Easy))
        assertTrue(values.contains(Difficulty.Moderate))
        assertTrue(values.contains(Difficulty.Hard))
        assertTrue(values.contains(Difficulty.Expert))
    }

    @Test
    fun `difficulty valueOf should work for valid names`() {
        assertEquals(Difficulty.Easy, Difficulty.valueOf("Easy"))
        assertEquals(Difficulty.Hard, Difficulty.valueOf("Hard"))
    }

    // ==================== CoverageStatus Enum Tests ====================

    @Test
    fun `coverage status should have 3 values`() {
        assertEquals(3, CoverageStatus.values().size)
    }

    // ==================== DangerZone Model Tests ====================

    @Test
    fun `danger zone should store radius in meters`() {
        val zone = DangerZone(
            id = "dz-1",
            name = "Elephant Corridor",
            center = LatLng(10.98, 76.79),
            radius = 2000.0, // 2km
            type = DangerType.Wildlife,
            severity = Severity.High,
            description = "Active elephant migration path",
            verified = true
        )

        assertEquals(2000.0, zone.radius, 0.01)
        assertEquals(DangerType.Wildlife, zone.type)
        assertEquals(Severity.High, zone.severity)
        assertTrue(zone.verified)
    }

    // ==================== LatLng Tests ====================

    @Test
    fun `LatLng equality should work for same coordinates`() {
        val a = LatLng(11.0168, 76.9558)
        val b = LatLng(11.0168, 76.9558)
        assertEquals(a, b) // data class equals
    }

    @Test
    fun `LatLng should store Coimbatore coordinates correctly`() {
        val coimbatore = LatLng(11.0168, 76.9558)
        assertTrue(coimbatore.latitude > 10 && coimbatore.latitude < 12)
        assertTrue(coimbatore.longitude > 76 && coimbatore.longitude < 78)
    }

    // ==================== HazardReport Tests ====================

    @Test
    fun `hazard report should have zero confirmations by default`() {
        val report = HazardReport(
            type = "Landslide",
            severity = "High",
            latitude = 11.0,
            longitude = 76.9,
            description = "Trail blocked by landslide"
        )

        assertEquals(0, report.confirmations)
    }

    // ==================== Emergency Contact Tests ====================

    @Test
    fun `emergency contact should store all fields`() {
        val contact = EmergencyContact(
            id = "ec-1",
            name = "TN Forest Department",
            phone = "18004251600",
            relation = "Emergency Service"
        )

        assertEquals("TN Forest Department", contact.name)
        assertEquals("18004251600", contact.phone)
    }

    // ==================== WeatherData Tests ====================

    @Test
    fun `weather data should have empty alerts by default`() {
        val weather = WeatherData(
            temperature = 24,
            humidity = 78,
            windSpeed = 12,
            conditions = "Clear",
            rainProbability = 10,
            uvIndex = 5
        )

        assertTrue(weather.alerts.isEmpty())
    }
}
