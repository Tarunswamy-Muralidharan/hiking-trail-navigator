package com.hikingtrailnavigator.admin.data

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Reads data from the Hiker app's ContentProvider.
 */
class HikerDataReader(private val resolver: ContentResolver) {

    companion object {
        private const val AUTHORITY = "com.hikingtrailnavigator.app.provider"
        val SOS_ALERTS_URI: Uri = Uri.parse("content://$AUTHORITY/sos_alerts")
        val HIKE_SESSIONS_URI: Uri = Uri.parse("content://$AUTHORITY/hike_sessions")
        val TRAILS_URI: Uri = Uri.parse("content://$AUTHORITY/trails")
        val ACTIVE_HIKERS_URI: Uri = Uri.parse("content://$AUTHORITY/active_hikers")
        val NOTIFICATIONS_URI: Uri = Uri.parse("content://$AUTHORITY/notifications")
        val HAZARD_REPORTS_URI: Uri = Uri.parse("content://$AUTHORITY/hazard_reports")
        val ROUTE_WARNINGS_URI: Uri = Uri.parse("content://$AUTHORITY/route_warnings")
    }

    fun getSosAlerts(): List<SosAlert> {
        val alerts = mutableListOf<SosAlert>()
        val cursor = resolver.query(SOS_ALERTS_URI, null, null, null, null) ?: return alerts
        cursor.use {
            while (it.moveToNext()) {
                alerts.add(SosAlert(
                    id = it.getString("id"),
                    hikerName = it.getString("hikerName"),
                    trailId = it.getString("trailId"),
                    trailName = it.getString("trailName"),
                    alertType = it.getString("alertType"),
                    latitude = it.getDouble("latitude"),
                    longitude = it.getDouble("longitude"),
                    timestamp = it.getLong("timestamp"),
                    isResolved = it.getInt("isResolved") == 1,
                    message = it.getString("message")
                ))
            }
        }
        return alerts
    }

    fun getHikeSessions(): List<HikeSession> {
        val sessions = mutableListOf<HikeSession>()
        val cursor = resolver.query(HIKE_SESSIONS_URI, null, null, null, null) ?: return sessions
        cursor.use {
            while (it.moveToNext()) {
                sessions.add(HikeSession(
                    sessionId = it.getString("sessionId"),
                    hikerId = it.getString("hikerId"),
                    trailId = it.getString("trailId"),
                    trailName = it.getString("trailName"),
                    startTime = it.getLong("startTime"),
                    endTime = it.getLongOrNull("endTime"),
                    status = it.getString("status")
                ))
            }
        }
        return sessions
    }

    fun getTrails(): List<Trail> {
        val trails = mutableListOf<Trail>()
        val cursor = resolver.query(TRAILS_URI, null, null, null, null) ?: return trails
        cursor.use {
            while (it.moveToNext()) {
                trails.add(Trail(
                    id = it.getString("id"),
                    name = it.getString("name"),
                    description = it.getString("description"),
                    difficulty = it.getString("difficulty"),
                    distance = it.getDouble("distance"),
                    estimatedDuration = it.getString("estimatedDuration"),
                    elevationGain = it.getInt("elevationGain"),
                    rating = it.getDouble("rating"),
                    region = it.getString("region"),
                    popularity = it.getInt("popularity")
                ))
            }
        }
        return trails
    }

    fun getActiveHikers(): List<ActiveHiker> {
        val hikers = mutableListOf<ActiveHiker>()
        val cursor = resolver.query(ACTIVE_HIKERS_URI, null, null, null, null) ?: return hikers
        cursor.use {
            while (it.moveToNext()) {
                hikers.add(ActiveHiker(
                    id = it.getString("id"),
                    hikerName = it.getString("hikerName"),
                    trailName = it.getString("trailName"),
                    startTime = it.getLong("startTime"),
                    lastCheckInTime = it.getLong("lastCheckInTime"),
                    lastLat = it.getDouble("lastLat"),
                    lastLng = it.getDouble("lastLng"),
                    missedCheckIns = it.getInt("missedCheckIns")
                ))
            }
        }
        return hikers
    }

    fun getHazardReports(): List<HazardReport> {
        val reports = mutableListOf<HazardReport>()
        val cursor = resolver.query(HAZARD_REPORTS_URI, null, null, null, null) ?: return reports
        cursor.use {
            while (it.moveToNext()) {
                reports.add(HazardReport(
                    id = it.getString("id"),
                    type = it.getString("type"),
                    severity = it.getString("severity"),
                    latitude = it.getDouble("latitude"),
                    longitude = it.getDouble("longitude"),
                    description = it.getString("description"),
                    reportedAt = it.getLong("reportedAt"),
                    confirmations = it.getInt("confirmations"),
                    expiresAt = it.getLong("expiresAt"),
                    isVerified = it.getInt("isVerified") == 1
                ))
            }
        }
        return reports
    }

    fun getRouteWarnings(): List<RouteWarning> {
        val warnings = mutableListOf<RouteWarning>()
        val cursor = resolver.query(ROUTE_WARNINGS_URI, null, null, null, null) ?: return warnings
        cursor.use {
            while (it.moveToNext()) {
                warnings.add(RouteWarning(
                    id = it.getString("id"),
                    trailId = it.getString("trailId"),
                    latitude = it.getDouble("latitude"),
                    longitude = it.getDouble("longitude"),
                    warningType = it.getString("warningType"),
                    description = it.getString("description"),
                    reportedBy = it.getString("reportedBy"),
                    reportedAt = it.getLong("reportedAt"),
                    upvotes = it.getInt("upvotes")
                ))
            }
        }
        return warnings
    }

    fun deactivateRouteWarning(warningId: String) {
        val uri = Uri.parse("content://$AUTHORITY/route_warnings/deactivate/$warningId")
        resolver.update(uri, ContentValues(), null, null)
    }

    fun verifyHazard(hazardId: String) {
        val uri = Uri.parse("content://$AUTHORITY/hazard_reports/verify/$hazardId")
        resolver.update(uri, ContentValues(), null, null)
    }

    fun rejectHazard(hazardId: String) {
        val uri = Uri.parse("content://$AUTHORITY/hazard_reports/reject/$hazardId")
        resolver.update(uri, ContentValues(), null, null)
    }

    fun resolveSosAlert(alertId: String) {
        val uri = Uri.parse("content://$AUTHORITY/sos_alerts/resolve/$alertId")
        resolver.update(uri, ContentValues(), null, null)
    }

    fun deleteTrail(trailId: String) {
        val uri = Uri.parse("content://$AUTHORITY/trails/delete/$trailId")
        resolver.delete(uri, null, null)
    }

    // Cursor extension helpers
    private fun Cursor.getString(col: String): String =
        getString(getColumnIndexOrThrow(col)) ?: ""

    private fun Cursor.getDouble(col: String): Double =
        getDouble(getColumnIndexOrThrow(col))

    private fun Cursor.getLong(col: String): Long =
        getLong(getColumnIndexOrThrow(col))

    private fun Cursor.getInt(col: String): Int =
        getInt(getColumnIndexOrThrow(col))

    private fun Cursor.getLongOrNull(col: String): Long? {
        val idx = getColumnIndexOrThrow(col)
        return if (isNull(idx)) null else getLong(idx)
    }
}

// Data classes for the admin app
data class SosAlert(
    val id: String,
    val hikerName: String,
    val trailId: String,
    val trailName: String,
    val alertType: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isResolved: Boolean,
    val message: String
)

data class HikeSession(
    val sessionId: String,
    val hikerId: String,
    val trailId: String,
    val trailName: String,
    val startTime: Long,
    val endTime: Long?,
    val status: String
)

data class Trail(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val distance: Double,
    val estimatedDuration: String,
    val elevationGain: Int,
    val rating: Double,
    val region: String,
    val popularity: Int
)

data class ActiveHiker(
    val id: String,
    val hikerName: String,
    val trailName: String,
    val startTime: Long,
    val lastCheckInTime: Long,
    val lastLat: Double,
    val lastLng: Double,
    val missedCheckIns: Int
)

data class HazardReport(
    val id: String,
    val type: String,
    val severity: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val reportedAt: Long,
    val confirmations: Int,
    val expiresAt: Long,
    val isVerified: Boolean
)

data class RouteWarning(
    val id: String,
    val trailId: String,
    val latitude: Double,
    val longitude: Double,
    val warningType: String,
    val description: String,
    val reportedBy: String,
    val reportedAt: Long,
    val upvotes: Int
)
