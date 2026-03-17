package com.hikingtrailnavigator.app.service

import com.hikingtrailnavigator.app.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * UNIT TESTS for RiskAssessmentService
 *
 * What: Tests individual functions in isolation - no Android framework needed.
 * Why: Verifies the risk scoring algorithm produces correct results for
 *      different trail configurations (easy vs expert, with/without weather, danger zones).
 * How: Runs on JVM (fast, no device needed). We create test data and assert expected outputs.
 * SRS: Covers FR-206 (Weather Risk Integration) and FR-208 (Danger Zone Alerts)
 */
class RiskAssessmentServiceTest {

    private lateinit var service: RiskAssessmentService

    // Helper to create a test trail with configurable parameters
    private fun createTrail(
        difficulty: Difficulty = Difficulty.Easy,
        elevationGain: Int = 200,
        coverageStatus: CoverageStatus = CoverageStatus.Full,
        coordinates: List<LatLng> = listOf(LatLng(11.0, 76.9))
    ) = Trail(
        id = "test-1",
        name = "Test Trail",
        description = "A test trail",
        difficulty = difficulty,
        distance = 5.0,
        estimatedDuration = "2 hours",
        elevationGain = elevationGain,
        rating = 4.0,
        coordinates = coordinates,
        startPoint = LatLng(11.0, 76.9),
        endPoint = LatLng(11.1, 77.0),
        hazards = listOf("Slippery rocks"),
        region = "Coimbatore",
        popularity = 50,
        coverageStatus = coverageStatus,
        elevationProfile = listOf(ElevationPoint(0.0, 100), ElevationPoint(5.0, 300))
    )

    @Before
    fun setUp() {
        // Create a fresh instance before each test (no Hilt needed for unit tests)
        service = RiskAssessmentService()
    }

    // ==================== TEST 1: Easy trail with no dangers ====================
    @Test
    fun `easy trail with full coverage should have low risk`() {
        val trail = createTrail(
            difficulty = Difficulty.Easy,
            elevationGain = 200,
            coverageStatus = CoverageStatus.Full
        )

        val result = service.assessTrailRisk(trail, emptyList())

        // Easy(5) + elevation<500(5) + Full coverage(0) = 10 → "low"
        assertEquals("low", result.level)
        assertEquals(10, result.score)
        assertTrue(result.factors.isEmpty()) // No risk factors for easy + full coverage
    }

    // ==================== TEST 2: Hard trail with no coverage ====================
    @Test
    fun `hard trail with no coverage should have high risk`() {
        val trail = createTrail(
            difficulty = Difficulty.Hard,
            elevationGain = 1500,
            coverageStatus = CoverageStatus.None
        )

        val result = service.assessTrailRisk(trail, emptyList())

        // Hard(30) + elevation>1000(20) + None coverage(25) = 75 → "critical"
        assertEquals("critical", result.level)
        assertEquals(75, result.score)
        assertTrue(result.factors.contains("Difficult terrain"))
        assertTrue(result.factors.contains("Limited network coverage"))
    }

    // ==================== TEST 3: Expert trail = highest difficulty score ====================
    @Test
    fun `expert difficulty should add 45 points`() {
        val trail = createTrail(difficulty = Difficulty.Expert)

        val result = service.assessTrailRisk(trail, emptyList())

        // Expert(45) + elevation<500(5) + Full(0) = 50
        assertEquals(50, result.score)
    }

    // ==================== TEST 4: Danger zones near trail add risk ====================
    @Test
    fun `nearby danger zones should increase risk score`() {
        val trail = createTrail(
            coordinates = listOf(LatLng(11.0168, 76.9558)) // PSG iTech location
        )
        val dangerZones = listOf(
            DangerZone(
                id = "dz-1",
                name = "Elephant Corridor",
                center = LatLng(11.0170, 76.9560), // Very close to trail
                radius = 1000.0,
                type = DangerType.Wildlife,
                severity = Severity.High,
                description = "Active elephant corridor",
                verified = true
            )
        )

        val result = service.assessTrailRisk(trail, dangerZones)

        // Easy(5) + elevation<500(5) + Full(0) + 1 danger zone(10) = 20 → "medium"
        assertEquals("medium", result.level)
        assertTrue(result.factors.contains("1 danger zone(s) nearby"))
    }

    // ==================== TEST 5: Weather with high rain increases risk ====================
    @Test
    fun `bad weather should increase risk score`() {
        val trail = createTrail()
        val weather = WeatherData(
            temperature = 20,
            humidity = 90,
            windSpeed = 45, // High wind
            conditions = "Stormy",
            rainProbability = 80, // High rain
            uvIndex = 9, // High UV
            alerts = listOf(
                WeatherAlert("Storm Warning", "Heavy storm approaching", "High")
            )
        )

        val result = service.assessTrailRisk(trail, emptyList(), weather)

        // Easy(5) + elev(5) + Full(0) + rain>60(15) + wind>40(10) + UV>8(5) + 1 alert(10) = 50
        assertEquals(50, result.score)
        assertTrue(result.factors.contains("High rain probability"))
        assertTrue(result.factors.contains("Weather alerts active"))
    }

    // ==================== TEST 6: Moderate trail with partial coverage ====================
    @Test
    fun `moderate trail with partial coverage should be medium risk`() {
        val trail = createTrail(
            difficulty = Difficulty.Moderate,
            elevationGain = 600,
            coverageStatus = CoverageStatus.Partial
        )

        val result = service.assessTrailRisk(trail, emptyList())

        // Moderate(15) + elevation>500(10) + Partial(10) = 35 → "medium"
        assertEquals("medium", result.level)
        assertEquals(35, result.score)
    }

    // ==================== TEST 7: Risk level thresholds ====================
    @Test
    fun `risk levels should follow correct thresholds`() {
        // Score < 20 = low
        val lowTrail = createTrail(Difficulty.Easy, 100, CoverageStatus.Full)
        assertEquals("low", service.assessTrailRisk(lowTrail, emptyList()).level)

        // Score 20-39 = medium
        val medTrail = createTrail(Difficulty.Moderate, 600, CoverageStatus.Partial)
        assertEquals("medium", service.assessTrailRisk(medTrail, emptyList()).level)

        // Score >= 60 = critical
        val critTrail = createTrail(Difficulty.Expert, 1500, CoverageStatus.None)
        assertEquals("critical", service.assessTrailRisk(critTrail, emptyList()).level)
    }

    // ==================== TEST 8: No weather = no weather factors ====================
    @Test
    fun `null weather should not add any weather factors`() {
        val trail = createTrail()

        val result = service.assessTrailRisk(trail, emptyList(), null)

        assertFalse(result.factors.any { it.contains("rain") || it.contains("Weather") })
    }
}
