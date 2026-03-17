package com.hikingtrailnavigator.app.service

import com.hikingtrailnavigator.app.domain.model.LatLng
import com.hikingtrailnavigator.app.domain.model.WeatherAlert
import com.hikingtrailnavigator.app.domain.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherService @Inject constructor() {

    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

    // Fetch weather for a location
    // In production, this would call OpenWeatherMap or similar API
    // For now, provides realistic simulated data for Western Ghats region
    suspend fun fetchWeather(location: LatLng): WeatherData {
        // Simulate weather data for Karnataka/Western Ghats
        val weather = WeatherData(
            temperature = 24,
            humidity = 78,
            windSpeed = 12,
            conditions = "Partly Cloudy",
            rainProbability = 35,
            uvIndex = 6,
            alerts = buildList {
                // Simulate seasonal alerts (monsoon warning if applicable)
                val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
                if (month in 5..8) { // June-September monsoon season
                    add(WeatherAlert(
                        title = "Monsoon Season Active",
                        description = "Heavy rainfall expected. Trails may be slippery. Watch for leeches and flash floods.",
                        severity = "High"
                    ))
                }
                if (month in 2..4) { // March-May summer
                    add(WeatherAlert(
                        title = "High Temperature Advisory",
                        description = "Temperatures may exceed 35°C. Carry extra water and avoid midday hiking.",
                        severity = "Medium"
                    ))
                }
            }
        )

        _currentWeather.value = weather
        return weather
    }

    fun getWeatherRiskLevel(weather: WeatherData): String {
        var score = 0
        if (weather.rainProbability > 70) score += 30
        else if (weather.rainProbability > 40) score += 15
        if (weather.windSpeed > 40) score += 25
        else if (weather.windSpeed > 20) score += 10
        if (weather.uvIndex > 8) score += 15
        else if (weather.uvIndex > 6) score += 5
        score += weather.alerts.size * 15

        return when {
            score >= 50 -> "high"
            score >= 25 -> "medium"
            else -> "low"
        }
    }
}
