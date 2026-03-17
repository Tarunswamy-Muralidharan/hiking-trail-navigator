package com.hikingtrailnavigator.app.service

import com.hikingtrailnavigator.app.domain.model.WeatherAlert
import com.hikingtrailnavigator.app.domain.model.WeatherData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * UNIT TESTS for WeatherService
 *
 * What: Tests weather risk level calculation logic.
 * Why: Weather risk scoring affects trail safety recommendations (FR-206).
 *      Wrong risk levels could lead users into dangerous conditions.
 * How: Runs on JVM. Creates WeatherData with known values, verifies risk levels.
 * SRS: Covers FR-206 (Weather-Based Risk Assessment)
 */
class WeatherServiceTest {

    private lateinit var service: WeatherService

    @Before
    fun setUp() {
        service = WeatherService()
    }

    // ==================== Weather Risk Level Tests ====================

    @Test
    fun `calm weather should return low risk`() {
        val weather = WeatherData(
            temperature = 25,
            humidity = 60,
            windSpeed = 10,
            conditions = "Clear",
            rainProbability = 10,
            uvIndex = 4,
            alerts = emptyList()
        )

        assertEquals("low", service.getWeatherRiskLevel(weather))
    }

    @Test
    fun `moderate rain and wind should return medium risk`() {
        val weather = WeatherData(
            temperature = 22,
            humidity = 80,
            windSpeed = 25, // >20 = +10
            conditions = "Cloudy",
            rainProbability = 50, // >40 = +15
            uvIndex = 5,
            alerts = emptyList()
        )

        // wind>20(10) + rain>40(15) = 25 → "medium"
        assertEquals("medium", service.getWeatherRiskLevel(weather))
    }

    @Test
    fun `storm conditions should return high risk`() {
        val weather = WeatherData(
            temperature = 18,
            humidity = 95,
            windSpeed = 50, // >40 = +25
            conditions = "Storm",
            rainProbability = 90, // >70 = +30
            uvIndex = 2,
            alerts = listOf(
                WeatherAlert("Cyclone Warning", "Severe storm", "Critical")
            ) // 1 alert = +15
        )

        // wind>40(25) + rain>70(30) + 1 alert(15) = 70 → "high"
        assertEquals("high", service.getWeatherRiskLevel(weather))
    }

    @Test
    fun `high UV alone should contribute to risk score`() {
        val weather = WeatherData(
            temperature = 35,
            humidity = 40,
            windSpeed = 5,
            conditions = "Sunny",
            rainProbability = 0,
            uvIndex = 9, // >8 = +15
            alerts = emptyList()
        )

        // Only UV>8(15) = 15 → "low" (below 25)
        assertEquals("low", service.getWeatherRiskLevel(weather))
    }

    @Test
    fun `multiple weather alerts should accumulate risk`() {
        val weather = WeatherData(
            temperature = 20,
            humidity = 85,
            windSpeed = 15,
            conditions = "Rainy",
            rainProbability = 30,
            uvIndex = 3,
            alerts = listOf(
                WeatherAlert("Flash Flood", "Warning", "High"),
                WeatherAlert("Landslide Risk", "Warning", "High"),
                WeatherAlert("Lightning", "Warning", "Medium")
            ) // 3 alerts = +45
        )

        // 3 alerts(45) = 45 → "medium" (45 < 50)
        assertEquals("medium", service.getWeatherRiskLevel(weather))
    }

    @Test
    fun `worst case weather should be high risk`() {
        val weather = WeatherData(
            temperature = 15,
            humidity = 100,
            windSpeed = 60,   // >40 = +25
            conditions = "Cyclone",
            rainProbability = 100, // >70 = +30
            uvIndex = 10,     // >8 = +15
            alerts = listOf(
                WeatherAlert("Cyclone", "Extreme", "Critical"),
                WeatherAlert("Flood", "Severe", "Critical")
            ) // 2 alerts = +30
        )

        // 25 + 30 + 15 + 30 = 100 → "high"
        assertEquals("high", service.getWeatherRiskLevel(weather))
    }
}
