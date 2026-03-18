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

    fun saveHikerName(name: String) { prefs.edit().putString("hiker_name", name).apply() }
    fun getHikerName(): String = prefs.getString("hiker_name", "Hiker") ?: "Hiker"
    fun saveRole(role: String) { prefs.edit().putString("role", role).apply() }
    fun getRole(): String? = prefs.getString("role", null)
    fun isLoggedIn(): Boolean = getRole() != null
    fun logout() { prefs.edit().clear().apply() }
}
