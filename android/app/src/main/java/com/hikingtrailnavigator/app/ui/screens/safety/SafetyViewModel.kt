package com.hikingtrailnavigator.app.ui.screens.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.remote.HikerApi
import com.hikingtrailnavigator.app.data.remote.SosRequest
import com.hikingtrailnavigator.app.data.repository.EmergencyContactRepository
import com.hikingtrailnavigator.app.data.repository.HazardRepository
import com.hikingtrailnavigator.app.data.repository.TrailRepository
import com.hikingtrailnavigator.app.domain.model.*
import com.hikingtrailnavigator.app.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ============ Safety Dashboard ============

data class SafetyUiState(
    val riskLevel: String = "low",
    val dangerZones: List<DangerZone> = emptyList(),
    val noCoverageZones: List<NoCoverageZone> = emptyList(),
    val recentHazards: List<HazardReport> = emptyList(),
    val emergencyContactCount: Int = 0,
    val isOnline: Boolean = true,
    val fallDetectionEnabled: Boolean = true,
    val weather: WeatherData? = null,
    val weatherRiskLevel: String = "low"
)

@HiltViewModel
class SafetyDashboardViewModel @Inject constructor(
    private val trailRepository: TrailRepository,
    private val hazardRepository: HazardRepository,
    private val contactRepository: EmergencyContactRepository,
    private val connectivityService: ConnectivityService,
    private val weatherService: WeatherService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyUiState())
    val uiState: StateFlow<SafetyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            trailRepository.getDangerZones().collect { zones ->
                _uiState.update { it.copy(dangerZones = zones) }
            }
        }
        viewModelScope.launch {
            trailRepository.getNoCoverageZones().collect { zones ->
                _uiState.update { it.copy(noCoverageZones = zones) }
            }
        }
        viewModelScope.launch {
            hazardRepository.getAllHazards().collect { hazards ->
                _uiState.update { it.copy(recentHazards = hazards.take(5)) }
            }
        }
        viewModelScope.launch {
            val count = contactRepository.getContactCount()
            _uiState.update { it.copy(emergencyContactCount = count) }
        }
        viewModelScope.launch {
            connectivityService.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
        // FR-206: Fetch weather data
        viewModelScope.launch {
            val weather = weatherService.fetchWeather(LatLng(11.0168, 76.9558))
            val riskLevel = weatherService.getWeatherRiskLevel(weather)
            _uiState.update { it.copy(weather = weather, weatherRiskLevel = riskLevel) }
        }
    }
}

// ============ SOS ============

data class SosUiState(
    val isSosActive: Boolean = false,
    val countdown: Int = 5,
    val sosMessage: String = "",
    val currentLocation: LatLng? = null,
    val contactsNotified: Int = 0,
    val isSilentMode: Boolean = false,
    val isLocating: Boolean = false,
    val isOffline: Boolean = false
)

@HiltViewModel
class SOSViewModel @Inject constructor(
    private val emergencyService: EmergencyService,
    private val contactRepository: EmergencyContactRepository,
    private val connectivityService: ConnectivityService,
    private val api: HikerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SosUiState())
    val uiState: StateFlow<SosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectivityService.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
    }

    fun activateSos(location: LatLng?) {
        _uiState.update {
            it.copy(
                isSosActive = true,
                isLocating = location == null,
                currentLocation = location,
                sosMessage = if (location == null) "Getting your GPS location..." else "SOS signal sent! Help is on the way."
            )
        }

        if (!_uiState.value.isSilentMode) {
            emergencyService.triggerSOSVibration()
        }

        viewModelScope.launch {
            // Get real GPS location
            val loc = location ?: emergencyService.getCurrentLocation()
            _uiState.update {
                it.copy(
                    currentLocation = loc,
                    isLocating = false,
                    sosMessage = if (loc != null)
                        "SOS sent with GPS: ${String.format("%.5f", loc.latitude)}, ${String.format("%.5f", loc.longitude)}"
                    else "SOS sent! (Location unavailable)"
                )
            }

            val actualLoc = loc ?: LatLng(0.0, 0.0)

            // 1. Always send SMS (works offline) - to user contacts + forest officers
            emergencyService.sendSosToAllContacts(actualLoc)
            val count = contactRepository.getContactCount() + 2 // +2 for forest dept contacts
            _uiState.update { it.copy(contactsNotified = count) }

            // 2. Try API call if online (non-blocking)
            try {
                api.triggerSos(
                    SosRequest(userId = "local_user", latitude = actualLoc.latitude, longitude = actualLoc.longitude)
                )
            } catch (_: Exception) {
                // Offline or server unreachable - SMS already sent
            }
        }
    }

    fun activateSilentSos(location: LatLng?) {
        _uiState.update {
            it.copy(
                isSosActive = true,
                isSilentMode = true,
                isLocating = location == null,
                currentLocation = location,
                sosMessage = "Getting your location silently..."
            )
        }

        // No vibration for silent SOS

        viewModelScope.launch {
            val loc = location ?: emergencyService.getCurrentLocation()
            _uiState.update {
                it.copy(
                    currentLocation = loc,
                    isLocating = false,
                    sosMessage = "Silent SOS sent. Contacts notified discreetly."
                )
            }

            val actualLoc = loc ?: LatLng(0.0, 0.0)

            emergencyService.sendSosToAllContacts(actualLoc)
            val count = contactRepository.getContactCount() + 2
            _uiState.update { it.copy(contactsNotified = count) }

            try {
                api.triggerSilentSos(
                    SosRequest(userId = "local_user", latitude = actualLoc.latitude, longitude = actualLoc.longitude)
                )
            } catch (_: Exception) {}
        }
    }

    fun cancelSos() {
        _uiState.update { SosUiState() }
        viewModelScope.launch {
            try { api.cancelSos(mapOf("userId" to "local_user")) } catch (_: Exception) {}
        }
    }

    fun callEmergency(number: String = "112") {
        emergencyService.callEmergency(number)
    }
}

// ============ Hazard Report ============

data class HazardReportUiState(
    val hazardType: String = "",
    val severity: String = "Medium",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val existingHazards: List<HazardReport> = emptyList()
)

@HiltViewModel
class HazardReportViewModel @Inject constructor(
    private val hazardRepository: HazardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HazardReportUiState())
    val uiState: StateFlow<HazardReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hazardRepository.getAllHazards().collect { hazards ->
                _uiState.update { it.copy(existingHazards = hazards) }
            }
        }
    }

    fun updateType(type: String) { _uiState.update { it.copy(hazardType = type) } }
    fun updateSeverity(severity: String) { _uiState.update { it.copy(severity = severity) } }
    fun updateDescription(desc: String) { _uiState.update { it.copy(description = desc) } }
    fun updateLocation(lat: Double, lng: Double) { _uiState.update { it.copy(latitude = lat, longitude = lng) } }

    fun submitReport() {
        val state = _uiState.value
        if (state.hazardType.isBlank() || state.description.isBlank()) return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            val report = HazardReport(
                id = UUID.randomUUID().toString(),
                type = state.hazardType,
                severity = state.severity,
                latitude = state.latitude,
                longitude = state.longitude,
                description = state.description
            )
            hazardRepository.reportHazard(report)
            _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
        }
    }

    // FR-212: Community validation - confirm a hazard report
    fun confirmHazard(hazardId: String) {
        viewModelScope.launch {
            hazardRepository.confirmHazard(hazardId)
        }
    }

    fun resetForm() {
        _uiState.update { HazardReportUiState(existingHazards = it.existingHazards) }
    }
}

// ============ Live Tracking ============

data class LiveTrackingUiState(
    val isTracking: Boolean = false,
    val currentLocation: LatLng? = null,
    val trackingDuration: Long = 0,
    val shareLink: String = "",
    val isLocationShared: Boolean = false
)

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val api: HikerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTrackingUiState())
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    fun startTracking() { _uiState.update { it.copy(isTracking = true) } }
    fun stopTracking() { _uiState.update { it.copy(isTracking = false, isLocationShared = false) } }

    fun updateLocation(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)
        _uiState.update { it.copy(currentLocation = location) }

        if (_uiState.value.isLocationShared) {
            viewModelScope.launch {
                try {
                    api.sendLocationUpdate(
                        com.hikingtrailnavigator.app.data.remote.LocationUpdateRequest(
                            userId = "local_user", latitude = lat, longitude = lng
                        )
                    )
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleLocationShare() {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                isLocationShared = !current.isLocationShared,
                shareLink = if (!current.isLocationShared && current.currentLocation != null) {
                    "https://maps.google.com/?q=${current.currentLocation.latitude},${current.currentLocation.longitude}"
                } else ""
            )
        }
    }
}

// ============ Emergency Contacts ============

data class EmergencyContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newPhone: String = "",
    val newRelation: String = ""
)

@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    private val contactRepository: EmergencyContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyContactsUiState())
    val uiState: StateFlow<EmergencyContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contacts ->
                _uiState.update { it.copy(contacts = contacts) }
            }
        }
    }

    fun showAddDialog() { _uiState.update { it.copy(showAddDialog = true) } }
    fun dismissAddDialog() { _uiState.update { it.copy(showAddDialog = false, newName = "", newPhone = "", newRelation = "") } }
    fun updateName(name: String) { _uiState.update { it.copy(newName = name) } }
    fun updatePhone(phone: String) { _uiState.update { it.copy(newPhone = phone) } }
    fun updateRelation(relation: String) { _uiState.update { it.copy(newRelation = relation) } }

    fun addContact() {
        val state = _uiState.value
        if (state.newName.isBlank() || state.newPhone.isBlank()) return
        viewModelScope.launch {
            contactRepository.addContact(
                EmergencyContact(
                    id = UUID.randomUUID().toString(),
                    name = state.newName,
                    phone = state.newPhone,
                    relation = state.newRelation
                )
            )
            dismissAddDialog()
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch { contactRepository.deleteContact(contact) }
    }
}

// ============ Route Warnings (Crowdsourced) ============

data class RouteWarningsUiState(
    val warnings: List<com.hikingtrailnavigator.app.data.local.entity.RouteWarningEntity> = emptyList(),
    val selectedType: String = "",
    val description: String = "",
    val reporterName: String = "",
    val submitted: Boolean = false
)

@HiltViewModel
class RouteWarningsViewModel @Inject constructor(
    private val routeWarningDao: com.hikingtrailnavigator.app.data.local.dao.RouteWarningDao,
    private val emergencyService: EmergencyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteWarningsUiState())
    val uiState: StateFlow<RouteWarningsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routeWarningDao.getAllActiveWarnings().collect { warnings ->
                _uiState.update { it.copy(warnings = warnings) }
            }
        }
    }

    fun updateType(type: String) { _uiState.update { it.copy(selectedType = type) } }
    fun updateDescription(desc: String) { _uiState.update { it.copy(description = desc) } }
    fun updateReporterName(name: String) { _uiState.update { it.copy(reporterName = name) } }

    fun submitWarning() {
        val state = _uiState.value
        if (state.selectedType.isBlank() || state.description.isBlank()) return

        viewModelScope.launch {
            // Get current location for the warning
            val location = emergencyService.getCurrentLocation()
            routeWarningDao.insert(
                com.hikingtrailnavigator.app.data.local.entity.RouteWarningEntity(
                    id = UUID.randomUUID().toString(),
                    trailId = "",
                    latitude = location?.latitude ?: 0.0,
                    longitude = location?.longitude ?: 0.0,
                    warningType = state.selectedType,
                    description = state.description,
                    reportedBy = state.reporterName.ifBlank { "Anonymous" },
                    reportedAt = System.currentTimeMillis(),
                    upvotes = 0,
                    isActive = true
                )
            )
            _uiState.update {
                it.copy(submitted = true, selectedType = "", description = "")
            }
        }
    }

    fun upvoteWarning(id: String) {
        viewModelScope.launch { routeWarningDao.upvote(id) }
    }
}
