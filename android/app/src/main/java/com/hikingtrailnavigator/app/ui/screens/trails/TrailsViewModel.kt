package com.hikingtrailnavigator.app.ui.screens.trails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.repository.TrailRepository
import com.hikingtrailnavigator.app.domain.model.*
import com.hikingtrailnavigator.app.service.RiskAssessmentService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrailListUiState(
    val trails: List<Trail> = emptyList(),
    val searchQuery: String = "",
    val selectedDifficulty: Difficulty? = null,
    val sortBy: SortOption = SortOption.Popularity
)

enum class SortOption { Popularity, Distance, Rating, Difficulty }

@HiltViewModel
class TrailListViewModel @Inject constructor(
    private val trailRepository: TrailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrailListUiState())
    val uiState: StateFlow<TrailListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            trailRepository.getAllTrails().collect { trails ->
                _uiState.update { state ->
                    state.copy(trails = filterAndSort(trails, state))
                }
            }
        }
    }

    private var allTrails: List<Trail> = emptyList()

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, trails = filterAndSort(allTrails, it.copy(searchQuery = query))) }
    }

    fun onDifficultyFilter(difficulty: Difficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty, trails = filterAndSort(allTrails, it.copy(selectedDifficulty = difficulty))) }
    }

    fun onSortChange(sort: SortOption) {
        _uiState.update { it.copy(sortBy = sort, trails = filterAndSort(allTrails, it.copy(sortBy = sort))) }
    }

    private fun filterAndSort(trails: List<Trail>, state: TrailListUiState): List<Trail> {
        allTrails = trails
        var filtered = trails

        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                it.region.contains(state.searchQuery, ignoreCase = true)
            }
        }

        state.selectedDifficulty?.let { diff ->
            filtered = filtered.filter { it.difficulty == diff }
        }

        return when (state.sortBy) {
            SortOption.Popularity -> filtered.sortedByDescending { it.popularity }
            SortOption.Distance -> filtered.sortedBy { it.distance }
            SortOption.Rating -> filtered.sortedByDescending { it.rating }
            SortOption.Difficulty -> filtered.sortedBy { it.difficulty.ordinal }
        }
    }
}

data class TrailDetailUiState(
    val trail: Trail? = null,
    val riskScore: Int = 0,
    val riskLevel: String = "low",
    val riskFactors: List<String> = emptyList(),
    val dangerZones: List<DangerZone> = emptyList()
)

@HiltViewModel
class TrailDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trailRepository: TrailRepository,
    private val riskAssessmentService: RiskAssessmentService
) : ViewModel() {

    private val trailId: String = savedStateHandle["trailId"] ?: ""

    private val _uiState = MutableStateFlow(TrailDetailUiState())
    val uiState: StateFlow<TrailDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val trail = trailRepository.getTrailById(trailId)
            trail?.let { t ->
                val assessment = riskAssessmentService.assessTrailRisk(t, emptyList())
                _uiState.update {
                    it.copy(
                        trail = t,
                        riskScore = assessment.score,
                        riskLevel = assessment.level,
                        riskFactors = assessment.factors
                    )
                }
            }
        }
        viewModelScope.launch {
            trailRepository.getDangerZones().collect { zones ->
                _uiState.update { it.copy(dangerZones = zones) }
            }
        }
    }
}
