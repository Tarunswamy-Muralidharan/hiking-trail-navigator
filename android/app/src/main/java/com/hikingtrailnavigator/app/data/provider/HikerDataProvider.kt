package com.hikingtrailnavigator.app.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.room.Room
import com.hikingtrailnavigator.app.data.local.HikerDatabase

/**
 * ContentProvider that exposes hiker app data to the admin app.
 * Provides read-only access to SOS alerts, hike sessions, trails, and active hikers.
 * Also supports resolving SOS alerts via update.
 */
class HikerDataProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.hikingtrailnavigator.app.provider"
        private const val SOS_ALERTS = 1
        private const val HIKE_SESSIONS = 2
        private const val TRAILS = 3
        private const val ACTIVE_HIKERS = 4
        private const val NOTIFICATIONS = 5
        private const val SOS_ALERT_RESOLVE = 6
        private const val TRAIL_DELETE = 7
        private const val TRAILS_INSERT = 8

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "sos_alerts", SOS_ALERTS)
            addURI(AUTHORITY, "hike_sessions", HIKE_SESSIONS)
            addURI(AUTHORITY, "trails", TRAILS)
            addURI(AUTHORITY, "active_hikers", ACTIVE_HIKERS)
            addURI(AUTHORITY, "notifications", NOTIFICATIONS)
            addURI(AUTHORITY, "sos_alerts/resolve/*", SOS_ALERT_RESOLVE)
            addURI(AUTHORITY, "trails/delete/*", TRAIL_DELETE)
            addURI(AUTHORITY, "trails/insert", TRAILS_INSERT)
        }
    }

    private lateinit var database: HikerDatabase

    override fun onCreate(): Boolean {
        database = Room.databaseBuilder(
            context!!,
            HikerDatabase::class.java,
            "hiker_database"
        ).fallbackToDestructiveMigration().build()
        return true
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        val db = database.openHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            SOS_ALERTS -> db.query(
                "SELECT * FROM sos_alerts ORDER BY timestamp DESC"
            )
            HIKE_SESSIONS -> db.query(
                "SELECT * FROM hike_sessions ORDER BY startTime DESC"
            )
            TRAILS -> db.query(
                "SELECT id, name, description, difficulty, distance, estimatedDuration, elevationGain, rating, region, popularity, startLat, startLng FROM trails ORDER BY name ASC"
            )
            ACTIVE_HIKERS -> db.query(
                "SELECT * FROM active_hiker_sessions WHERE isActive = 1 ORDER BY startTime DESC"
            )
            NOTIFICATIONS -> db.query(
                "SELECT * FROM notifications ORDER BY timestamp DESC"
            )
            else -> null
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int {
        val db = database.openHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            SOS_ALERT_RESOLVE -> {
                val alertId = uri.lastPathSegment ?: return 0
                db.execSQL("UPDATE sos_alerts SET isResolved = 1 WHERE id = ?", arrayOf(alertId))
                context?.contentResolver?.notifyChange(uri, null)
                1
            }
            else -> 0
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null // Read-only for now
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = database.openHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            TRAIL_DELETE -> {
                val trailId = uri.lastPathSegment ?: return 0
                db.execSQL("DELETE FROM trails WHERE id = ?", arrayOf(trailId))
                context?.contentResolver?.notifyChange(uri, null)
                1
            }
            else -> 0
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            SOS_ALERTS -> "vnd.android.cursor.dir/sos_alert"
            HIKE_SESSIONS -> "vnd.android.cursor.dir/hike_session"
            TRAILS -> "vnd.android.cursor.dir/trail"
            ACTIVE_HIKERS -> "vnd.android.cursor.dir/active_hiker"
            NOTIFICATIONS -> "vnd.android.cursor.dir/notification"
            else -> null
        }
    }
}
