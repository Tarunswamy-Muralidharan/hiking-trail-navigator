package com.hikingtrailnavigator.app.data.repository

import com.hikingtrailnavigator.app.data.local.dao.*
import com.hikingtrailnavigator.app.data.local.entity.*
import com.hikingtrailnavigator.app.data.remote.HikerApi
import com.hikingtrailnavigator.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrailRepository @Inject constructor(
    private val trailDao: TrailDao,
    private val dangerZoneDao: DangerZoneDao,
    private val noCoverageZoneDao: NoCoverageZoneDao,
    private val api: HikerApi
) {
    fun getAllTrails(): Flow<List<Trail>> =
        trailDao.getAllTrails().map { entities -> entities.map { it.toDomain() } }

    suspend fun getTrailById(id: String): Trail? =
        trailDao.getTrailById(id)?.toDomain()

    fun searchTrails(query: String): Flow<List<Trail>> =
        trailDao.searchTrails(query).map { entities -> entities.map { it.toDomain() } }

    fun getTrailsByDifficulty(difficulty: Difficulty): Flow<List<Trail>> =
        trailDao.getTrailsByDifficulty(difficulty.name).map { entities -> entities.map { it.toDomain() } }

    fun getDangerZones(): Flow<List<DangerZone>> =
        dangerZoneDao.getAllDangerZones().map { entities -> entities.map { it.toDomain() } }

    fun getNoCoverageZones(): Flow<List<NoCoverageZone>> =
        noCoverageZoneDao.getAllNoCoverageZones().map { entities -> entities.map { it.toDomain() } }

    suspend fun insertTrails(trails: List<Trail>) =
        trailDao.insertTrails(trails.map { it.toEntity() })

    suspend fun insertDangerZones(zones: List<DangerZoneEntity>) =
        dangerZoneDao.insertAll(zones)

    suspend fun insertNoCoverageZones(zones: List<NoCoverageZoneEntity>) =
        noCoverageZoneDao.insertAll(zones)
}

@Singleton
class HazardRepository @Inject constructor(
    private val hazardReportDao: HazardReportDao,
    private val api: HikerApi
) {
    fun getAllHazards(): Flow<List<HazardReport>> =
        hazardReportDao.getAllHazardReports().map { entities -> entities.map { it.toDomain() } }

    suspend fun confirmHazard(id: String) {
        hazardReportDao.confirmHazard(id)
    }

    suspend fun reportHazard(report: HazardReport) {
        hazardReportDao.insert(report.toEntity())
        try {
            api.reportHazard(
                com.hikingtrailnavigator.app.data.remote.HazardReportRequest(
                    type = report.type, severity = report.severity,
                    latitude = report.latitude, longitude = report.longitude,
                    description = report.description
                )
            )
        } catch (_: Exception) {
            // Saved locally, will sync later
        }
    }
}

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: HikeActivityDao
) {
    fun getAllActivities(): Flow<List<HikeActivity>> =
        activityDao.getAllActivities().map { entities -> entities.map { it.toDomain() } }

    suspend fun saveActivity(activity: HikeActivity) =
        activityDao.insert(activity.toEntity())

    suspend fun getStats(): Triple<Int, Double, Long> {
        val count = activityDao.getActivityCount()
        val distance = activityDao.getTotalDistance()
        val duration = activityDao.getTotalDuration()
        return Triple(count, distance, duration)
    }
}

@Singleton
class EmergencyContactRepository @Inject constructor(
    private val contactDao: EmergencyContactDao
) {
    fun getAllContacts(): Flow<List<EmergencyContact>> =
        contactDao.getAllContacts().map { entities -> entities.map { it.toDomain() } }

    suspend fun addContact(contact: EmergencyContact) =
        contactDao.insert(contact.toEntity())

    suspend fun deleteContact(contact: EmergencyContact) =
        contactDao.delete(contact.toEntity())

    suspend fun getContactCount(): Int = contactDao.getContactCount()
}
