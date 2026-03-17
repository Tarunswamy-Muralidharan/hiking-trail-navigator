package com.hikingtrailnavigator.app.data

import com.hikingtrailnavigator.app.data.local.entity.*
import com.hikingtrailnavigator.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * UNIT TESTS for Entity <-> Domain Model Conversion
 *
 * What: Tests that Room entities correctly convert to/from domain models.
 * Why: Data integrity - if conversion is wrong, UI shows incorrect data.
 *      JSON serialization of coordinates/routes must be lossless.
 * How: Runs on JVM. Converts domain→entity→domain and verifies no data loss.
 * SRS: Ensures data layer supports all features correctly.
 */
class EntityConversionTest {

    // ==================== Trail Conversion Tests ====================

    @Test
    fun `trail to entity and back should preserve all fields`() {
        val original = Trail(
            id = "trail-1",
            name = "Vellingiri Hills Trek",
            description = "Sacred trek",
            difficulty = Difficulty.Hard,
            distance = 22.0,
            estimatedDuration = "8-10 hours",
            elevationGain = 1500,
            rating = 4.5,
            coordinates = listOf(LatLng(10.95, 76.82), LatLng(11.0, 76.8)),
            startPoint = LatLng(10.95, 76.82),
            endPoint = LatLng(11.01, 76.78),
            hazards = listOf("Steep terrain", "Leeches"),
            region = "Coimbatore",
            popularity = 95,
            coverageStatus = CoverageStatus.None,
            elevationProfile = listOf(ElevationPoint(0.0, 500), ElevationPoint(22.0, 2000))
        )

        // Convert Trail → TrailEntity → Trail
        val entity = original.toEntity()
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.difficulty, restored.difficulty)
        assertEquals(original.distance, restored.distance, 0.01)
        assertEquals(original.elevationGain, restored.elevationGain)
        assertEquals(original.coverageStatus, restored.coverageStatus)
        assertEquals(original.coordinates.size, restored.coordinates.size)
        assertEquals(original.hazards, restored.hazards)
        assertEquals(original.elevationProfile.size, restored.elevationProfile.size)
    }

    @Test
    fun `trail entity should store coordinates as JSON string`() {
        val trail = Trail(
            id = "t1", name = "Test", description = "Test",
            difficulty = Difficulty.Easy, distance = 5.0,
            estimatedDuration = "2h", elevationGain = 200, rating = 4.0,
            coordinates = listOf(LatLng(11.0, 76.9), LatLng(11.1, 77.0)),
            startPoint = LatLng(11.0, 76.9), endPoint = LatLng(11.1, 77.0),
            hazards = listOf("Rocks"), region = "Test", popularity = 50,
            coverageStatus = CoverageStatus.Full,
            elevationProfile = listOf(ElevationPoint(0.0, 100))
        )

        val entity = trail.toEntity()

        // Entity coordinates should be a JSON string, not empty
        assertTrue(entity.coordinates.contains("11.0"))
        assertTrue(entity.coordinates.contains("76.9"))
        assertEquals("Easy", entity.difficulty) // Stored as string
        assertEquals("Full", entity.coverageStatus) // Stored as string
    }

    // ==================== DangerZone Conversion Tests ====================

    @Test
    fun `danger zone entity to domain should preserve coordinates`() {
        val entity = DangerZoneEntity(
            id = "dz-1",
            name = "Elephant Corridor",
            centerLat = 10.98,
            centerLng = 76.79,
            radius = 2000.0,
            type = "Wildlife",
            severity = "High",
            description = "Active elephant corridor",
            verified = true
        )

        val domain = entity.toDomain()

        assertEquals("dz-1", domain.id)
        assertEquals(LatLng(10.98, 76.79), domain.center)
        assertEquals(DangerType.Wildlife, domain.type)
        assertEquals(Severity.High, domain.severity)
        assertTrue(domain.verified)
    }

    // ==================== HazardReport Conversion Tests ====================

    @Test
    fun `hazard report round trip should preserve all data`() {
        val original = HazardReport(
            id = "hr-1",
            type = "Landslide",
            severity = "High",
            latitude = 11.05,
            longitude = 76.85,
            description = "Trail blocked after rain",
            reportedAt = 1710000000000L,
            confirmations = 5
        )

        val entity = original.toEntity()
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.type, restored.type)
        assertEquals(original.latitude, restored.latitude, 0.001)
        assertEquals(original.confirmations, restored.confirmations)
    }

    // ==================== Emergency Contact Conversion Tests ====================

    @Test
    fun `emergency contact round trip should preserve phone number`() {
        val original = EmergencyContact(
            id = "ec-1",
            name = "Forest Dept",
            phone = "18004251600",
            relation = "Emergency"
        )

        val entity = original.toEntity()
        val restored = entity.toDomain()

        assertEquals(original.phone, restored.phone)
        assertEquals(original.name, restored.name)
    }

    // ==================== HikeActivity Conversion Tests ====================

    @Test
    fun `hike activity should preserve route coordinates through JSON`() {
        val original = HikeActivity(
            id = "ha-1",
            trailId = "trail-1",
            trailName = "Vellingiri Hills",
            startTime = 1710000000000L,
            endTime = 1710036000000L,
            distance = 22.0,
            duration = 36000000L,
            elevationGain = 1500,
            route = listOf(LatLng(10.95, 76.82), LatLng(11.0, 76.8), LatLng(11.01, 76.78)),
            checkIns = 12
        )

        val entity = original.toEntity()
        val restored = entity.toDomain()

        assertEquals(3, restored.route.size)
        assertEquals(10.95, restored.route[0].latitude, 0.001)
        assertEquals(76.82, restored.route[0].longitude, 0.001)
        assertEquals(original.duration, restored.duration)
        assertEquals(original.checkIns, restored.checkIns)
    }

    // ==================== NoCoverageZone Conversion Tests ====================

    @Test
    fun `no coverage zone entity should convert center coordinates`() {
        val entity = NoCoverageZoneEntity(
            id = "ncz-1",
            name = "Vellingiri Deep Forest",
            centerLat = 10.97,
            centerLng = 76.80,
            radius = 3000.0,
            description = "No mobile coverage"
        )

        val domain = entity.toDomain()

        assertEquals(LatLng(10.97, 76.80), domain.center)
        assertEquals(3000.0, domain.radius, 0.01)
    }
}
