package com.hikingtrailnavigator.app.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.hikingtrailnavigator.app.data.repository.ActivityRepository
import com.hikingtrailnavigator.app.data.repository.HazardRepository
import com.hikingtrailnavigator.app.data.repository.TrailRepository
import com.hikingtrailnavigator.app.domain.model.*
import com.hikingtrailnavigator.app.service.ConnectivityService
import com.hikingtrailnavigator.app.service.GeofencingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UnexploredArea(
    val center: LatLng,
    val radius: Double,
    val label: String
)

data class SearchSuggestion(
    val name: String,
    val subtitle: String,
    val location: LatLng,
    val zoomLevel: Double,
    val trailId: String? = null // non-null if it's a trail
)

data class HomeUiState(
    val trails: List<Trail> = emptyList(),
    val dangerZones: List<DangerZone> = emptyList(),
    val noCoverageZones: List<NoCoverageZone> = emptyList(),
    val hazardReports: List<HazardReport> = emptyList(),
    val unexploredAreas: List<UnexploredArea> = emptyList(),
    val searchQuery: String = "",
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    val showSuggestions: Boolean = false,
    val selectedPlace: SearchSuggestion? = null,
    val mapCenterLat: Double = 11.0168,
    val mapCenterLng: Double = 76.9558,
    val mapZoom: Double = 16.0,
    val showDangerZones: Boolean = true,
    val showNoCoverageZones: Boolean = true,
    val showHazards: Boolean = true,
    val showUnexplored: Boolean = false,
    val riskLevel: String = "low",
    val isOffline: Boolean = false,
    val noCoverageWarning: String? = null,
    val currentLocation: LatLng? = null,
    val cameraMoveKey: Int = 0,
    val useSatellite: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val trailRepository: TrailRepository,
    private val hazardRepository: HazardRepository,
    private val activityRepository: ActivityRepository,
    private val connectivityService: ConnectivityService,
    private val geofencingService: GeofencingService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Known places around Coimbatore for search
    private val knownPlaces = listOf(
        SearchSuggestion("PSG iTech", "Neelambur, Coimbatore", LatLng(11.0650, 77.0932), 18.0),
        SearchSuggestion("PSG iTech Neelambur", "Campus, Coimbatore", LatLng(11.0650, 77.0932), 18.0),
        SearchSuggestion("PSG College of Technology", "Peelamedu, Coimbatore", LatLng(11.0243, 77.0028), 15.0),
        SearchSuggestion("Coimbatore City", "Tamil Nadu", LatLng(11.0168, 76.9558), 12.0),
        SearchSuggestion("Neelambur", "Coimbatore", LatLng(11.0650, 77.0932), 16.0),
        SearchSuggestion("Marudhamalai Temple", "Coimbatore", LatLng(11.0010, 76.9080), 14.0),
        SearchSuggestion("Mettupalayam", "Coimbatore District", LatLng(11.2990, 76.9370), 13.0),
        SearchSuggestion("Ooty / Nilgiris", "Tamil Nadu", LatLng(11.4100, 76.6950), 12.0),
        SearchSuggestion("Siruvani Falls", "Coimbatore", LatLng(10.9380, 76.6250), 13.0),
        SearchSuggestion("Vellingiri Hills", "Western Ghats, Coimbatore", LatLng(11.0100, 76.7950), 13.0),
        SearchSuggestion("Topslip", "Anamalai Hills", LatLng(10.4840, 76.8380), 13.0),
        SearchSuggestion("Valparai", "Anamalai Hills", LatLng(10.3267, 76.9505), 13.0),
        SearchSuggestion("Pollachi", "Coimbatore District", LatLng(10.6580, 77.0080), 13.0),
        SearchSuggestion("Kolli Hills", "Namakkal", LatLng(11.2540, 78.3580), 12.0),
        SearchSuggestion("Doddabetta Peak", "Nilgiris", LatLng(11.4010, 76.7350), 14.0),
        SearchSuggestion("Anamalai Hills", "Western Ghats", LatLng(10.3500, 76.8800), 12.0),
        SearchSuggestion("Ukkadam", "Coimbatore", LatLng(10.9925, 76.9614), 14.0),
        SearchSuggestion("RS Puram", "Coimbatore", LatLng(11.0060, 76.9500), 14.0),
        SearchSuggestion("Gandhipuram", "Coimbatore", LatLng(11.0183, 76.9725), 14.0),
        SearchSuggestion("Saravanampatti", "Coimbatore", LatLng(11.0550, 76.9920), 14.0)
    )

    init {
        viewModelScope.launch {
            trailRepository.getAllTrails().collect { trails ->
                _uiState.update { it.copy(trails = trails) }
                computeUnexploredAreas(trails)
            }
        }
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
                _uiState.update { it.copy(hazardReports = hazards) }
            }
        }
        viewModelScope.launch {
            connectivityService.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
        startLocationMonitoring()
    }

    private var hasInitialLocation = false

    @SuppressLint("MissingPermission")
    private fun startLocationMonitoring() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
        viewModelScope.launch {
            while (true) {
                try {
                    val location = fusedClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        // First location fix: center map on user
                        if (!hasInitialLocation) {
                            hasInitialLocation = true
                            _uiState.update {
                                it.copy(
                                    currentLocation = currentLatLng,
                                    mapCenterLat = currentLatLng.latitude,
                                    mapCenterLng = currentLatLng.longitude,
                                    mapZoom = 17.0
                                )
                            }
                        } else {
                            _uiState.update { it.copy(currentLocation = currentLatLng) }
                        }
                        checkNoCoverageProximity(currentLatLng)
                    }
                } catch (_: Exception) {}
                delay(10_000)
            }
        }
    }

    private fun checkNoCoverageProximity(location: LatLng) {
        val zones = _uiState.value.noCoverageZones
        for (zone in zones) {
            val distance = geofencingService.haversineMeters(location, zone.center)
            if (distance <= zone.radius) {
                _uiState.update {
                    it.copy(noCoverageWarning = "You are in: ${zone.name}. ${zone.description}")
                }
                return
            } else if (distance <= zone.radius + 1000) {
                _uiState.update {
                    it.copy(noCoverageWarning = "Approaching: ${zone.name} (${((distance - zone.radius) / 1000).toInt()}km away)")
                }
                return
            }
        }
        _uiState.update { it.copy(noCoverageWarning = null) }
    }

    private fun computeUnexploredAreas(trails: List<Trail>) {
        viewModelScope.launch {
            val activities = activityRepository.getAllActivities().first()
            val visitedTrailIds = activities.map { it.trailId }.toSet()
            val unexplored = trails
                .filter { it.popularity < 75 && it.id !in visitedTrailIds }
                .map { trail ->
                    val midIdx = trail.coordinates.size / 2
                    val center = if (trail.coordinates.isNotEmpty()) trail.coordinates[midIdx] else trail.startPoint
                    UnexploredArea(center = center, radius = 2000.0, label = "${trail.name} area - Low activity")
                }
            _uiState.update { it.copy(unexploredAreas = unexplored) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, selectedPlace = null) }

        if (query.length < 2) {
            _uiState.update { it.copy(searchSuggestions = emptyList(), showSuggestions = false) }
            return
        }

        val q = query.lowercase()

        // Match trails
        val trailMatches = _uiState.value.trails
            .filter { it.name.lowercase().contains(q) || it.region.lowercase().contains(q) }
            .map { trail ->
                SearchSuggestion(
                    name = trail.name,
                    subtitle = "${trail.region} - ${trail.difficulty.name} - ${trail.distance}km",
                    location = trail.startPoint,
                    zoomLevel = 14.0,
                    trailId = trail.id
                )
            }

        // Match known places
        val placeMatches = knownPlaces
            .filter { it.name.lowercase().contains(q) || it.subtitle.lowercase().contains(q) }

        val all = (trailMatches + placeMatches).take(8)
        _uiState.update { it.copy(searchSuggestions = all, showSuggestions = all.isNotEmpty()) }
    }

    fun onSuggestionSelected(suggestion: SearchSuggestion) {
        _uiState.update {
            it.copy(
                searchQuery = suggestion.name,
                selectedPlace = suggestion,
                showSuggestions = false,
                mapCenterLat = suggestion.location.latitude,
                mapCenterLng = suggestion.location.longitude,
                mapZoom = suggestion.zoomLevel,
                cameraMoveKey = it.cameraMoveKey + 1
            )
        }
    }

    fun dismissSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }

    fun clearSelection() {
        val loc = _uiState.value.currentLocation
        _uiState.update {
            it.copy(
                selectedPlace = null,
                searchQuery = "",
                mapCenterLat = loc?.latitude ?: 11.0168,
                mapCenterLng = loc?.longitude ?: 76.9558,
                mapZoom = 17.0,
                cameraMoveKey = it.cameraMoveKey + 1
            )
        }
    }

    // Find nearest trail to a given location
    fun getNearestTrail(location: LatLng): Trail? {
        return _uiState.value.trails.minByOrNull {
            geofencingService.haversineMeters(location, it.startPoint)
        }
    }

    fun centerOnMyLocation() {
        val loc = _uiState.value.currentLocation ?: return
        _uiState.update {
            it.copy(
                mapCenterLat = loc.latitude,
                mapCenterLng = loc.longitude,
                mapZoom = 17.0,
                selectedPlace = null,
                searchQuery = "",
                cameraMoveKey = it.cameraMoveKey + 1
            )
        }
    }

    fun toggleDangerZones() { _uiState.update { it.copy(showDangerZones = !it.showDangerZones) } }
    fun toggleNoCoverageZones() { _uiState.update { it.copy(showNoCoverageZones = !it.showNoCoverageZones) } }
    fun toggleHazards() { _uiState.update { it.copy(showHazards = !it.showHazards) } }
    fun toggleUnexplored() { _uiState.update { it.copy(showUnexplored = !it.showUnexplored) } }
    fun toggleSatellite() { _uiState.update { it.copy(useSatellite = !it.useSatellite) } }
}
