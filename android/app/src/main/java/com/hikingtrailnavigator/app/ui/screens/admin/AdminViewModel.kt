package com.hikingtrailnavigator.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.local.dao.ActiveHikerDao
import com.hikingtrailnavigator.app.data.local.dao.RouteWarningDao
import com.hikingtrailnavigator.app.data.local.entity.ActiveHikerSessionEntity
import com.hikingtrailnavigator.app.data.local.entity.RouteWarningEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val username: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val error: String = "",
    val activeHikers: List<ActiveHikerSessionEntity> = emptyList(),
    val routeWarnings: List<RouteWarningEntity> = emptyList()
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val activeHikerDao: ActiveHikerDao,
    private val routeWarningDao: RouteWarningDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

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

    fun deactivateWarning(id: String) {
        viewModelScope.launch {
            routeWarningDao.deactivate(id)
        }
    }
}
