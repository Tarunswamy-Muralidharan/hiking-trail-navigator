package com.hikingtrailnavigator.app.ui.screens.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hikingtrailnavigator.app.data.repository.ActivityRepository
import com.hikingtrailnavigator.app.data.repository.EmergencyContactRepository
import com.hikingtrailnavigator.app.domain.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

// ============ Profile ============

data class ProfileUiState(
    val totalHikes: Int = 0,
    val totalDistance: Double = 0.0,
    val totalDuration: Long = 0,
    val emergencyContactCount: Int = 0,
    val userName: String = "Hiker",
    val preferences: UserPreferences = UserPreferences()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val contactRepository: EmergencyContactRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private object PrefsKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val CHECK_IN_INTERVAL = intPreferencesKey("check_in_interval")
        val FALL_DETECTION = booleanPreferencesKey("fall_detection")
        val SILENT_SOS = booleanPreferencesKey("silent_sos")
        val DEVIATION_DISTANCE = intPreferencesKey("deviation_distance")
        val GPS_ACCURACY = stringPreferencesKey("gps_accuracy")
        val LOCATION_SHARE = booleanPreferencesKey("location_share")
    }

    init {
        viewModelScope.launch {
            val (count, dist, dur) = activityRepository.getStats()
            _uiState.update { it.copy(totalHikes = count, totalDistance = dist, totalDuration = dur) }
        }
        viewModelScope.launch {
            val count = contactRepository.getContactCount()
            _uiState.update { it.copy(emergencyContactCount = count) }
        }
        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        userName = prefs[PrefsKeys.USER_NAME] ?: "Hiker",
                        preferences = UserPreferences(
                            checkInInterval = prefs[PrefsKeys.CHECK_IN_INTERVAL] ?: 60,
                            fallDetectionEnabled = prefs[PrefsKeys.FALL_DETECTION] ?: true,
                            silentSOSEnabled = prefs[PrefsKeys.SILENT_SOS] ?: true,
                            deviationAlertDistance = prefs[PrefsKeys.DEVIATION_DISTANCE] ?: 100,
                            gpsAccuracy = prefs[PrefsKeys.GPS_ACCURACY] ?: "high",
                            locationShareEnabled = prefs[PrefsKeys.LOCATION_SHARE] ?: true
                        )
                    )
                }
            }
        }
    }
}

// ============ Settings ============

data class SettingsUiState(
    val userName: String = "Hiker",
    val checkInInterval: Int = 60,
    val fallDetectionEnabled: Boolean = true,
    val silentSOSEnabled: Boolean = true,
    val deviationAlertDistance: Int = 100,
    val gpsAccuracy: String = "high",
    val locationShareEnabled: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private object PrefsKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val CHECK_IN_INTERVAL = intPreferencesKey("check_in_interval")
        val FALL_DETECTION = booleanPreferencesKey("fall_detection")
        val SILENT_SOS = booleanPreferencesKey("silent_sos")
        val DEVIATION_DISTANCE = intPreferencesKey("deviation_distance")
        val GPS_ACCURACY = stringPreferencesKey("gps_accuracy")
        val LOCATION_SHARE = booleanPreferencesKey("location_share")
    }

    init {
        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        userName = prefs[PrefsKeys.USER_NAME] ?: "Hiker",
                        checkInInterval = prefs[PrefsKeys.CHECK_IN_INTERVAL] ?: 60,
                        fallDetectionEnabled = prefs[PrefsKeys.FALL_DETECTION] ?: true,
                        silentSOSEnabled = prefs[PrefsKeys.SILENT_SOS] ?: true,
                        deviationAlertDistance = prefs[PrefsKeys.DEVIATION_DISTANCE] ?: 100,
                        gpsAccuracy = prefs[PrefsKeys.GPS_ACCURACY] ?: "high",
                        locationShareEnabled = prefs[PrefsKeys.LOCATION_SHARE] ?: true
                    )
                }
            }
        }
    }

    fun updateUserName(name: String) { _uiState.update { it.copy(userName = name, isSaved = false) } }
    fun updateCheckInInterval(interval: Int) { _uiState.update { it.copy(checkInInterval = interval, isSaved = false) } }
    fun toggleFallDetection() { _uiState.update { it.copy(fallDetectionEnabled = !it.fallDetectionEnabled, isSaved = false) } }
    fun toggleSilentSOS() { _uiState.update { it.copy(silentSOSEnabled = !it.silentSOSEnabled, isSaved = false) } }
    fun updateDeviationDistance(distance: Int) { _uiState.update { it.copy(deviationAlertDistance = distance, isSaved = false) } }
    fun updateGpsAccuracy(accuracy: String) { _uiState.update { it.copy(gpsAccuracy = accuracy, isSaved = false) } }
    fun toggleLocationShare() { _uiState.update { it.copy(locationShareEnabled = !it.locationShareEnabled, isSaved = false) } }

    fun saveSettings() {
        viewModelScope.launch {
            val state = _uiState.value
            context.dataStore.edit { prefs ->
                prefs[PrefsKeys.USER_NAME] = state.userName
                prefs[PrefsKeys.CHECK_IN_INTERVAL] = state.checkInInterval
                prefs[PrefsKeys.FALL_DETECTION] = state.fallDetectionEnabled
                prefs[PrefsKeys.SILENT_SOS] = state.silentSOSEnabled
                prefs[PrefsKeys.DEVIATION_DISTANCE] = state.deviationAlertDistance
                prefs[PrefsKeys.GPS_ACCURACY] = state.gpsAccuracy
                prefs[PrefsKeys.LOCATION_SHARE] = state.locationShareEnabled
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
