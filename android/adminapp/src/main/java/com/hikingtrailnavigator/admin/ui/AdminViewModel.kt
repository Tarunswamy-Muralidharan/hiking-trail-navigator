package com.hikingtrailnavigator.admin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.admin.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val sosAlerts: List<SosAlert> = emptyList(),
    val activeSessions: List<HikeSession> = emptyList(),
    val trails: List<Trail> = emptyList(),
    val activeHikers: List<ActiveHiker> = emptyList(),
    val hazardReports: List<HazardReport> = emptyList(),
    val routeWarnings: List<RouteWarning> = emptyList(),
    val isLoading: Boolean = true,
    val error: String = "",
    val lastRefresh: Long = 0,
    val currentTab: Int = 0
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val reader = HikerDataReader(application.contentResolver)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refresh()
        // Auto-refresh every 10 seconds
        viewModelScope.launch {
            while (true) {
                delay(10_000)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val alerts = reader.getSosAlerts()
                val sessions = reader.getHikeSessions()
                val trails = reader.getTrails()
                val hikers = reader.getActiveHikers()
                val hazards = reader.getHazardReports()
                val warnings = reader.getRouteWarnings()

                _uiState.update {
                    it.copy(
                        sosAlerts = alerts,
                        activeSessions = sessions,
                        trails = trails,
                        activeHikers = hikers,
                        hazardReports = hazards,
                        routeWarnings = warnings,
                        isLoading = false,
                        error = "",
                        lastRefresh = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Cannot connect to Hiker app. Make sure it's installed and has been opened at least once."
                    )
                }
            }
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reader.resolveSosAlert(alertId)
            refresh()
        }
    }

    fun deleteTrail(trailId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reader.deleteTrail(trailId)
            refresh()
        }
    }

    fun verifyHazard(hazardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reader.verifyHazard(hazardId)
            refresh()
        }
    }

    fun rejectHazard(hazardId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reader.rejectHazard(hazardId)
            refresh()
        }
    }

    fun deactivateRouteWarning(warningId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reader.deactivateRouteWarning(warningId)
            refresh()
        }
    }

    fun setTab(tab: Int) {
        _uiState.update { it.copy(currentTab = tab) }
    }
}
