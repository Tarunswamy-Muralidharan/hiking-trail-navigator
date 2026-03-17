package com.hikingtrailnavigator.app.data.local.dao

import androidx.room.*
import com.hikingtrailnavigator.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails ORDER BY popularity DESC")
    fun getAllTrails(): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trails WHERE id = :id")
    suspend fun getTrailById(id: String): TrailEntity?

    @Query("SELECT * FROM trails WHERE difficulty = :difficulty")
    fun getTrailsByDifficulty(difficulty: String): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trails WHERE name LIKE '%' || :query || '%' OR region LIKE '%' || :query || '%'")
    fun searchTrails(query: String): Flow<List<TrailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrails(trails: List<TrailEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrail(trail: TrailEntity)
}

@Dao
interface DangerZoneDao {
    @Query("SELECT * FROM danger_zones")
    fun getAllDangerZones(): Flow<List<DangerZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<DangerZoneEntity>)
}

@Dao
interface NoCoverageZoneDao {
    @Query("SELECT * FROM no_coverage_zones")
    fun getAllNoCoverageZones(): Flow<List<NoCoverageZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<NoCoverageZoneEntity>)
}

@Dao
interface HazardReportDao {
    @Query("SELECT * FROM hazard_reports ORDER BY reportedAt DESC")
    fun getAllHazardReports(): Flow<List<HazardReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: HazardReportEntity)

    @Delete
    suspend fun delete(report: HazardReportEntity)

    @Query("UPDATE hazard_reports SET confirmations = confirmations + 1 WHERE id = :id")
    suspend fun confirmHazard(id: String)
}

@Dao
interface HikeActivityDao {
    @Query("SELECT * FROM hike_activities ORDER BY startTime DESC")
    fun getAllActivities(): Flow<List<HikeActivityEntity>>

    @Query("SELECT COUNT(*) FROM hike_activities")
    suspend fun getActivityCount(): Int

    @Query("SELECT COALESCE(SUM(distance), 0) FROM hike_activities")
    suspend fun getTotalDistance(): Double

    @Query("SELECT COALESCE(SUM(duration), 0) FROM hike_activities")
    suspend fun getTotalDuration(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: HikeActivityEntity)
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: EmergencyContactEntity)

    @Delete
    suspend fun delete(contact: EmergencyContactEntity)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactCount(): Int
}

@Dao
interface ActiveHikerDao {
    @Query("SELECT * FROM active_hiker_sessions WHERE isActive = 1 ORDER BY startTime DESC")
    fun getActiveHikers(): Flow<List<ActiveHikerSessionEntity>>

    @Query("SELECT * FROM active_hiker_sessions WHERE id = :id")
    suspend fun getById(id: String): ActiveHikerSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ActiveHikerSessionEntity)

    @Query("UPDATE active_hiker_sessions SET lastCheckInTime = :time, lastLat = :lat, lastLng = :lng, missedCheckIns = 0 WHERE id = :id")
    suspend fun checkIn(id: String, time: Long, lat: Double, lng: Double)

    @Query("UPDATE active_hiker_sessions SET missedCheckIns = missedCheckIns + 1 WHERE id = :id")
    suspend fun incrementMissedCheckIn(id: String)

    @Query("UPDATE active_hiker_sessions SET isActive = 0 WHERE id = :id")
    suspend fun endSession(id: String)

    @Query("SELECT COUNT(*) FROM active_hiker_sessions WHERE isActive = 1")
    suspend fun getActiveCount(): Int
}

@Dao
interface RouteWarningDao {
    @Query("SELECT * FROM route_warnings WHERE isActive = 1 ORDER BY reportedAt DESC")
    fun getAllActiveWarnings(): Flow<List<RouteWarningEntity>>

    @Query("SELECT * FROM route_warnings WHERE trailId = :trailId AND isActive = 1 ORDER BY reportedAt DESC")
    fun getWarningsForTrail(trailId: String): Flow<List<RouteWarningEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warning: RouteWarningEntity)

    @Query("UPDATE route_warnings SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun upvote(id: String)

    @Query("UPDATE route_warnings SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)
}
