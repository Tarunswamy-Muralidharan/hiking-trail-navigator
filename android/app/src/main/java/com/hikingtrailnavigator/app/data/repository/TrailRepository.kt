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
    private val lowActivityZoneDao: LowActivityZoneDao,
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

    fun getLowActivityZones(): Flow<List<LowActivityZone>> =
        lowActivityZoneDao.getAllZones().map { entities -> entities.map { it.toDomain() } }

    suspend fun insertLowActivityZones(zones: List<LowActivityZoneEntity>) =
        lowActivityZoneDao.insertAll(zones)
}

@Singleton
class HazardRepository @Inject constructor(
    private val hazardReportDao: HazardReportDao,
    private val api: HikerApi
) {
    fun getAllHazards(): Flow<List<HazardReport>> =
        hazardReportDao.getActiveHazardReports().map { entities -> entities.map { it.toDomain() } }

    fun getAllHazardsIncludingExpired(): Flow<List<HazardReport>> =
        hazardReportDao.getAllHazardReports().map { entities -> entities.map { it.toDomain() } }

    suspend fun verifyHazard(id: String) = hazardReportDao.verifyHazard(id)
    suspend fun rejectHazard(id: String) = hazardReportDao.rejectHazard(id)
    suspend fun deleteExpiredReports() = hazardReportDao.deleteExpiredReports()

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

// ── UML Repositories ──

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun getUserById(id: String): User? =
        userDao.getUserById(id)?.toDomainUser()

    suspend fun getUserByEmail(email: String): User? =
        userDao.getUserByEmail(email)?.toDomainUser()

    fun getForestOfficers(): Flow<List<User>> =
        userDao.getForestOfficers().map { entities -> entities.map { it.toDomainUser() } }

    suspend fun createUser(user: User, passwordHash: String = "") {
        val entity = user.toEntity().copy(passwordHash = passwordHash)
        userDao.insert(entity)
    }

    suspend fun updateCurrentTrail(userId: String, trailId: String?) =
        userDao.updateCurrentTrail(userId, trailId)
}

@Singleton
class HikeSessionRepository @Inject constructor(
    private val sessionDao: HikeSessionDao,
    private val locationDao: LocationDao
) {
    fun getSessionsByHiker(hikerId: String): Flow<List<HikeSession>> =
        sessionDao.getSessionsByHiker(hikerId).map { entities -> entities.map { it.toDomain() } }

    fun getActiveSessions(): Flow<List<HikeSession>> =
        sessionDao.getActiveSessions().map { entities -> entities.map { it.toDomain() } }

    suspend fun getSessionById(id: String): HikeSession? =
        sessionDao.getSessionById(id)?.toDomain()

    suspend fun startSession(session: HikeSession) =
        sessionDao.insert(session.toEntity())

    suspend fun endSession(sessionId: String) =
        sessionDao.endSession(sessionId, System.currentTimeMillis())

    suspend fun updateStatus(sessionId: String, status: HikeStatus) =
        sessionDao.updateStatus(sessionId, status.name)

    // Location tracking for session
    suspend fun addLocation(location: Location) =
        locationDao.insert(location.toEntity())

    fun getSessionLocations(sessionId: String): Flow<List<Location>> =
        locationDao.getLocationsBySession(sessionId).map { entities -> entities.map { it.toDomain() } }

    suspend fun getLastLocation(sessionId: String): Location? =
        locationDao.getLastLocation(sessionId)?.toDomain()
}

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {
    fun getNotificationsForUser(userId: String): Flow<List<Notification>> =
        notificationDao.getNotificationsForUser(userId).map { entities -> entities.map { it.toDomain() } }

    fun getNotificationsForAlert(alertId: String): Flow<List<Notification>> =
        notificationDao.getNotificationsForAlert(alertId).map { entities -> entities.map { it.toDomain() } }

    suspend fun sendNotification(notification: Notification) =
        notificationDao.insert(notification.toEntity())

    suspend fun markAsRead(notificationId: String) =
        notificationDao.updateStatus(notificationId, "read")
}

@Singleton
class SafetyCheckInRepository @Inject constructor(
    private val checkInDao: SafetyCheckInDao
) {
    fun getCheckInsForSession(sessionId: String): Flow<List<SafetyCheckIn>> =
        checkInDao.getCheckInsForSession(sessionId).map { entities -> entities.map { it.toDomain() } }

    suspend fun scheduleCheckIn(checkIn: SafetyCheckIn) =
        checkInDao.insert(checkIn.toEntity())

    suspend fun confirmCheckIn(checkInId: String) =
        checkInDao.updateStatus(checkInId, "confirmed")

    suspend fun markMissed(checkInId: String) =
        checkInDao.updateStatus(checkInId, "missed")

    suspend fun getPendingCheckIns(sessionId: String): List<SafetyCheckIn> =
        checkInDao.getPendingCheckIns(sessionId).map { it.toDomain() }
}
