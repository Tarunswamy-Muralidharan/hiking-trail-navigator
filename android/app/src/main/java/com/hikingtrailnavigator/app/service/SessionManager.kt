package com.hikingtrailnavigator.app.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("hiker_session", Context.MODE_PRIVATE)

    // User identity
    fun saveUserId(userId: String) { prefs.edit().putString("user_id", userId).apply() }
    fun getUserId(): String = prefs.getString("user_id", "") ?: ""
    fun saveHikerName(name: String) { prefs.edit().putString("hiker_name", name).apply() }
    fun getHikerName(): String = prefs.getString("hiker_name", "Hiker") ?: "Hiker"
    fun saveEmail(email: String) { prefs.edit().putString("email", email).apply() }
    fun getEmail(): String = prefs.getString("email", "") ?: ""
    fun savePhoneNumber(phone: String) { prefs.edit().putString("phone", phone).apply() }
    fun getPhoneNumber(): String = prefs.getString("phone", "") ?: ""

    // Role
    fun saveRole(role: String) { prefs.edit().putString("role", role).apply() }
    fun getRole(): String? = prefs.getString("role", null)
    fun isLoggedIn(): Boolean = getRole() != null

    // Active session
    fun saveActiveSessionId(sessionId: String) { prefs.edit().putString("active_session_id", sessionId).apply() }
    fun getActiveSessionId(): String? = prefs.getString("active_session_id", null)
    fun clearActiveSession() { prefs.edit().remove("active_session_id").apply() }

    fun logout() { prefs.edit().clear().apply() }
}
