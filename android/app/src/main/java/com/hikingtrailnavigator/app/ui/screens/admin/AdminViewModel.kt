package com.hikingtrailnavigator.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.local.dao.ActiveHikerDao
import com.hikingtrailnavigator.app.data.local.dao.RouteWarningDao
import com.hikingtrailnavigator.app.data.local.dao.SosAlertDao
import com.hikingtrailnavigator.app.data.local.dao.TrailDao
import com.hikingtrailnavigator.app.data.local.entity.ActiveHikerSessionEntity
import com.hikingtrailnavigator.app.data.local.entity.RouteWarningEntity
import com.hikingtrailnavigator.app.data.local.entity.SosAlertEntity
import com.hikingtrailnavigator.app.data.local.entity.TrailEntity
import com.hikingtrailnavigator.app.data.local.entity.toEntity
import com.hikingtrailnavigator.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AdminUiState(
    val username: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val error: String = "",
    val activeHikers: List<ActiveHikerSessionEntity> = emptyList(),
    val routeWarnings: List<RouteWarningEntity> = emptyList(),
    val trails: List<TrailEntity> = emptyList(),
    val sosAlerts: List<SosAlertEntity> = emptyList()
)

data class AddTrailState(
    val name: String = "",
    val description: String = "",
    val difficulty: Difficulty = Difficulty.Easy,
    val distance: String = "",
    val duration: String = "",
    val elevationGain: String = "",
    val region: String = "",
    val startLat: String = "",
    val startLng: String = "",
    val hazards: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val activeHikerDao: ActiveHikerDao,
    private val routeWarningDao: RouteWarningDao,
    private val trailDao: TrailDao,
    private val sosAlertDao: SosAlertDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _addTrailState = MutableStateFlow(AddTrailState())
    val addTrailState: StateFlow<AddTrailState> = _addTrailState.asStateFlow()

    init {
        viewModelScope.launch {
            activeHikerDao.getActiveHikers().collect { hikers ->
                _uiState.update { it.copy(activeHikers = hikers) }
            }
        }
        viewModelScope.launch {
            routeWarningDao.getAllActiveWarnings().collect { warnings ->
                _uiState.update { it.copy(routeWarnings = warnings) }
            }
        }
        viewModelScope.launch {
            trailDao.getAllTrails().collect { trails ->
                _uiState.update { it.copy(trails = trails) }
            }
        }
        viewModelScope.launch {
            sosAlertDao.getActiveAlerts().collect { alerts ->
                _uiState.update { it.copy(sosAlerts = alerts) }
            }
        }
    }

    fun updateUsername(value: String) { _uiState.update { it.copy(username = value) } }
    fun updatePassword(value: String) { _uiState.update { it.copy(password = value) } }

    fun login() {
        val state = _uiState.value
        if (state.username == "admin" && state.password == "admin123") {
            _uiState.update { it.copy(isLoggedIn = true, error = "") }
        } else {
            _uiState.update { it.copy(error = "Invalid credentials") }
        }
    }

    fun resolveAlert(id: String) {
        viewModelScope.launch { sosAlertDao.resolve(id) }
    }

    fun deactivateWarning(id: String) {
        viewModelScope.launch {
            routeWarningDao.deactivate(id)
        }
    }

    // Add Trail form updates
    fun updateTrailName(v: String) { _addTrailState.update { it.copy(name = v) } }
    fun updateTrailDescription(v: String) { _addTrailState.update { it.copy(description = v) } }
    fun updateTrailDifficulty(v: Difficulty) { _addTrailState.update { it.copy(difficulty = v) } }
    fun updateTrailDistance(v: String) { _addTrailState.update { it.copy(distance = v) } }
    fun updateTrailDuration(v: String) { _addTrailState.update { it.copy(duration = v) } }
    fun updateTrailElevation(v: String) { _addTrailState.update { it.copy(elevationGain = v) } }
    fun updateTrailRegion(v: String) { _addTrailState.update { it.copy(region = v) } }
    fun updateTrailStartLat(v: String) { _addTrailState.update { it.copy(startLat = v) } }
    fun updateTrailStartLng(v: String) { _addTrailState.update { it.copy(startLng = v) } }
    fun updateTrailHazards(v: String) { _addTrailState.update { it.copy(hazards = v) } }

    fun saveTrail() {
        val s = _addTrailState.value
        if (s.name.isBlank() || s.distance.isBlank() || s.startLat.isBlank() || s.startLng.isBlank()) {
            _addTrailState.update { it.copy(error = "Please fill name, distance, and start coordinates") }
            return
        }

        val dist = s.distance.toDoubleOrNull()
        val lat = s.startLat.toDoubleOrNull()
        val lng = s.startLng.toDoubleOrNull()
        val elev = s.elevationGain.toIntOrNull() ?: 0

        if (dist == null || lat == null || lng == null) {
            _addTrailState.update { it.copy(error = "Invalid number format") }
            return
        }

        _addTrailState.update { it.copy(isSaving = true, error = "") }

        val hazardsList = s.hazards.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val trail = Trail(
            id = "trail_${UUID.randomUUID().toString().take(8)}",
            name = s.name,
            description = s.description.ifBlank { "Admin-added trail: ${s.name}" },
            difficulty = s.difficulty,
            distance = dist,
            estimatedDuration = s.duration.ifBlank { "${(dist / 3).toInt() + 1}-${(dist / 3).toInt() + 2} hours" },
            elevationGain = elev,
            rating = 4.0,
            coordinates = listOf(LatLng(lat, lng), LatLng(lat + 0.005, lng + 0.005)),
            startPoint = LatLng(lat, lng),
            endPoint = LatLng(lat + 0.005, lng + 0.005),
            hazards = hazardsList.ifEmpty { listOf("Check local conditions") },
            region = s.region.ifBlank { "Coimbatore" },
            popularity = 50,
            coverageStatus = CoverageStatus.Full,
            elevationProfile = listOf(
                ElevationPoint(0.0, elev / 2),
                ElevationPoint(dist / 2, elev),
                ElevationPoint(dist, elev / 2)
            )
        )

        viewModelScope.launch {
            trailDao.insertTrail(trail.toEntity())
            _addTrailState.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun resetAddTrailForm() {
        _addTrailState.value = AddTrailState()
    }

    fun deleteTrail(trailId: String) {
        viewModelScope.launch {
            trailDao.deleteTrail(trailId)
        }
    }
}
