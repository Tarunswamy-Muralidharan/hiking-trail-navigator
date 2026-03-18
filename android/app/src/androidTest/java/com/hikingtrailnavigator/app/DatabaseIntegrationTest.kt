package com.hikingtrailnavigator.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hikingtrailnavigator.app.data.local.HikerDatabase
import com.hikingtrailnavigator.app.data.local.dao.*
import com.hikingtrailnavigator.app.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * INTEGRATION TESTS for Room Database + DAOs
 *
 * What: Tests that multiple components (Database + DAO + Entity) work together correctly.
 *       Verifies SQL queries, inserts, updates, and deletes against a real SQLite database.
 * Why: Unit tests can't catch SQL errors. Integration tests verify actual database behavior.
 *      Catches issues like wrong column names, invalid queries, type converter failures.
 * How: Runs on device/emulator. Creates an in-memory Room database (destroyed after test).
 *      Each test inserts data, performs operations, and verifies results.
 * SRS: Validates data persistence for trails (FR-101), danger zones (FR-208),
 *       hazard reports (FR-212), emergency contacts, hike activities, and route warnings.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {

    private lateinit var database: HikerDatabase
    private lateinit var trailDao: TrailDao
    private lateinit var dangerZoneDao: DangerZoneDao
    private lateinit var hazardReportDao: HazardReportDao
    private lateinit var hikeActivityDao: HikeActivityDao
    private lateinit var emergencyContactDao: EmergencyContactDao
    private lateinit var activeHikerDao: ActiveHikerDao
    private lateinit var routeWarningDao: RouteWarningDao
    private lateinit var sosAlertDao: SosAlertDao

    @Before
    fun setUp() {
        // Create an in-memory database that is destroyed when the test ends
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HikerDatabase::class.java)
            .allowMainThreadQueries() // OK for testing only
            .build()

        trailDao = database.trailDao()
        dangerZoneDao = database.dangerZoneDao()
        hazardReportDao = database.hazardReportDao()
        hikeActivityDao = database.hikeActivityDao()
        emergencyContactDao = database.emergencyContactDao()
        activeHikerDao = database.activeHikerDao()
        routeWarningDao = database.routeWarningDao()
        sosAlertDao = database.sosAlertDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== Trail DAO Integration Tests ====================

    @Test
    fun insertAndRetrieveTrail() = runTest {
        val trail = TrailEntity(
            id = "trail-1",
            name = "Vellingiri Hills Trek",
            description = "Sacred trek to the Vellingiri peak",
            difficulty = "Hard",
            distance = 22.0,
            estimatedDuration = "8-10 hours",
            elevationGain = 1500,
            rating = 4.5,
            coordinates = "[{\"latitude\":10.95,\"longitude\":76.82}]",
            startLat = 10.95, startLng = 76.82,
            endLat = 11.01, endLng = 76.78,
            hazards = "[\"Steep terrain\",\"Leeches\"]",
            region = "Coimbatore",
            popularity = 95,
            coverageStatus = "None",
            elevationProfile = "[{\"distance\":0.0,\"elevation\":500}]"
        )

        trailDao.insertTrail(trail)
        val retrieved = trailDao.getTrailById("trail-1")

        assertNotNull(retrieved)
        assertEquals("Vellingiri Hills Trek", retrieved!!.name)
        assertEquals("Hard", retrieved.difficulty)
        assertEquals(22.0, retrieved.distance, 0.01)
        assertEquals(1500, retrieved.elevationGain)
    }

    @Test
    fun getAllTrailsOrderedByPopularity() = runTest {
        val trails = listOf(
            createTrailEntity("t1", "Low Pop Trail", popularity = 10),
            createTrailEntity("t2", "High Pop Trail", popularity = 95),
            createTrailEntity("t3", "Medium Pop Trail", popularity = 50)
        )
        trailDao.insertTrails(trails)

        val result = trailDao.getAllTrails().first()

        assertEquals(3, result.size)
        assertEquals("High Pop Trail", result[0].name)  // Highest popularity first
        assertEquals("Medium Pop Trail", result[1].name)
        assertEquals("Low Pop Trail", result[2].name)
    }

    @Test
    fun searchTrailsByName() = runTest {
        trailDao.insertTrails(listOf(
            createTrailEntity("t1", "Vellingiri Hills Trek", region = "Coimbatore"),
            createTrailEntity("t2", "Siruvani Dam Trek", region = "Coimbatore"),
            createTrailEntity("t3", "Doddabetta Peak", region = "Nilgiris")
        ))

        val results = trailDao.searchTrails("Vellingiri").first()

        assertEquals(1, results.size)
        assertEquals("Vellingiri Hills Trek", results[0].name)
    }

    @Test
    fun searchTrailsByRegion() = runTest {
        trailDao.insertTrails(listOf(
            createTrailEntity("t1", "Trail A", region = "Coimbatore"),
            createTrailEntity("t2", "Trail B", region = "Coimbatore"),
            createTrailEntity("t3", "Trail C", region = "Nilgiris")
        ))

        val results = trailDao.searchTrails("Coimbatore").first()

        assertEquals(2, results.size)
    }

    @Test
    fun filterTrailsByDifficulty() = runTest {
        trailDao.insertTrails(listOf(
            createTrailEntity("t1", "Easy Trail", difficulty = "Easy"),
            createTrailEntity("t2", "Hard Trail", difficulty = "Hard"),
            createTrailEntity("t3", "Another Easy", difficulty = "Easy")
        ))

        val easyTrails = trailDao.getTrailsByDifficulty("Easy").first()

        assertEquals(2, easyTrails.size)
        assertTrue(easyTrails.all { it.difficulty == "Easy" })
    }

    // ==================== Hazard Report Integration Tests (FR-212) ====================

    @Test
    fun insertAndConfirmHazardReport() = runTest {
        val report = HazardReportEntity(
            id = "hr-1", type = "Landslide", severity = "High",
            latitude = 11.05, longitude = 76.85,
            description = "Trail blocked", reportedAt = System.currentTimeMillis(),
            confirmations = 0
        )

        hazardReportDao.insert(report)
        hazardReportDao.confirmHazard("hr-1") // +1
        hazardReportDao.confirmHazard("hr-1") // +2
        hazardReportDao.confirmHazard("hr-1") // +3 → community confirmed!

        val reports = hazardReportDao.getAllHazardReports().first()
        assertEquals(1, reports.size)
        assertEquals(3, reports[0].confirmations) // FR-212: 3+ = confirmed
    }

    @Test
    fun deleteHazardReport() = runTest {
        val report = HazardReportEntity(
            id = "hr-1", type = "Wildlife", severity = "Medium",
            latitude = 11.0, longitude = 76.9,
            description = "Bear spotted", reportedAt = System.currentTimeMillis(),
            confirmations = 1
        )

        hazardReportDao.insert(report)
        hazardReportDao.delete(report)

        val reports = hazardReportDao.getAllHazardReports().first()
        assertTrue(reports.isEmpty())
    }

    // ==================== Emergency Contact Integration Tests ====================

    @Test
    fun addAndDeleteEmergencyContact() = runTest {
        val contact = EmergencyContactEntity(
            id = "ec-1", name = "TN Forest Dept",
            phone = "18004251600", relation = "Emergency Service"
        )

        emergencyContactDao.insert(contact)
        assertEquals(1, emergencyContactDao.getContactCount())

        emergencyContactDao.delete(contact)
        assertEquals(0, emergencyContactDao.getContactCount())
    }

    // ==================== Hike Activity Integration Tests ====================

    @Test
    fun saveActivityAndGetStatistics() = runTest {
        hikeActivityDao.insert(HikeActivityEntity(
            id = "ha-1", trailId = "t1", trailName = "Vellingiri",
            startTime = 1000L, endTime = 37000L, distance = 22.0,
            duration = 36000000L, elevationGain = 1500, route = "[]", checkIns = 12
        ))
        hikeActivityDao.insert(HikeActivityEntity(
            id = "ha-2", trailId = "t2", trailName = "Siruvani",
            startTime = 2000L, endTime = 20000L, distance = 10.0,
            duration = 18000000L, elevationGain = 450, route = "[]", checkIns = 6
        ))

        assertEquals(2, hikeActivityDao.getActivityCount())
        assertEquals(32.0, hikeActivityDao.getTotalDistance(), 0.01) // 22 + 10
        assertEquals(54000000L, hikeActivityDao.getTotalDuration()) // 36M + 18M
    }

    // ==================== Active Hiker Session Tests (Admin Dashboard) ====================

    @Test
    fun activeHikerCheckInAndEndSession() = runTest {
        val session = ActiveHikerSessionEntity(
            id = "session-1", hikerName = "Poornesh",
            trailId = "trail-1", trailName = "Vellingiri",
            startTime = System.currentTimeMillis(),
            lastCheckInTime = System.currentTimeMillis(),
            lastLat = 10.95, lastLng = 76.82,
            isActive = true, missedCheckIns = 0
        )

        activeHikerDao.insert(session)
        assertEquals(1, activeHikerDao.getActiveCount())

        // Simulate check-in with new location
        activeHikerDao.checkIn("session-1", System.currentTimeMillis(), 11.0, 76.85)
        val updated = activeHikerDao.getById("session-1")
        assertEquals(11.0, updated!!.lastLat, 0.01)
        assertEquals(0, updated.missedCheckIns) // Reset after check-in

        // Simulate missed check-in
        activeHikerDao.incrementMissedCheckIn("session-1")
        val missed = activeHikerDao.getById("session-1")
        assertEquals(1, missed!!.missedCheckIns)

        // End session
        activeHikerDao.endSession("session-1")
        assertEquals(0, activeHikerDao.getActiveCount()) // No longer active
    }

    // ==================== Route Warning Tests (Crowdsource FR-212) ====================

    @Test
    fun routeWarningUpvoteAndDeactivate() = runTest {
        val warning = RouteWarningEntity(
            id = "rw-1", trailId = "trail-1",
            latitude = 10.98, longitude = 76.79,
            warningType = "Landslide", description = "Road blocked",
            reportedBy = "Hiker123", reportedAt = System.currentTimeMillis(),
            upvotes = 0, isActive = true
        )

        routeWarningDao.insert(warning)

        // Upvote 3 times (community confirmation)
        routeWarningDao.upvote("rw-1")
        routeWarningDao.upvote("rw-1")
        routeWarningDao.upvote("rw-1")

        val warnings = routeWarningDao.getAllActiveWarnings().first()
        assertEquals(3, warnings[0].upvotes) // Community confirmed

        // Admin deactivates
        routeWarningDao.deactivate("rw-1")
        val active = routeWarningDao.getAllActiveWarnings().first()
        assertTrue(active.isEmpty()) // Deactivated, no longer visible
    }

    @Test
    fun getWarningsForSpecificTrail() = runTest {
        routeWarningDao.insert(RouteWarningEntity(
            id = "rw-1", trailId = "trail-1", latitude = 10.98, longitude = 76.79,
            warningType = "Landslide", description = "Blocked", reportedBy = "User1",
            reportedAt = System.currentTimeMillis(), upvotes = 0, isActive = true
        ))
        routeWarningDao.insert(RouteWarningEntity(
            id = "rw-2", trailId = "trail-2", latitude = 11.0, longitude = 76.9,
            warningType = "Wildlife", description = "Elephant", reportedBy = "User2",
            reportedAt = System.currentTimeMillis(), upvotes = 0, isActive = true
        ))

        val trail1Warnings = routeWarningDao.getWarningsForTrail("trail-1").first()
        assertEquals(1, trail1Warnings.size)
        assertEquals("Landslide", trail1Warnings[0].warningType)
    }

    // ==================== SOS Alert Tests (Session 10 - Admin Dashboard) ====================

    @Test
    fun insertAndResolveSosAlert() = runTest {
        val alert = SosAlertEntity(
            id = "sos-1",
            hikerName = "Poornesh",
            trailId = "trail-1",
            trailName = "Vellingiri Hills",
            alertType = "SOS_BUTTON",
            latitude = 10.95,
            longitude = 76.82,
            timestamp = System.currentTimeMillis(),
            isResolved = false,
            message = "Hiker pressed SOS button"
        )

        sosAlertDao.insert(alert)
        val activeAlerts = sosAlertDao.getActiveAlerts().first()
        assertEquals(1, activeAlerts.size)
        assertEquals("SOS_BUTTON", activeAlerts[0].alertType)
        assertEquals("Poornesh", activeAlerts[0].hikerName)

        // Resolve the alert
        sosAlertDao.resolve("sos-1")
        val afterResolve = sosAlertDao.getActiveAlerts().first()
        assertTrue(afterResolve.isEmpty()) // Resolved alerts not shown
    }

    @Test
    fun multipleSosAlertTypes() = runTest {
        val alerts = listOf(
            SosAlertEntity(
                id = "sos-1", hikerName = "Hiker1", trailId = "t1", trailName = "Trail1",
                alertType = "SOS_BUTTON", latitude = 11.0, longitude = 76.9,
                timestamp = System.currentTimeMillis(), message = "SOS button"
            ),
            SosAlertEntity(
                id = "sos-2", hikerName = "Hiker2", trailId = "t2", trailName = "Trail2",
                alertType = "FALL_DETECTED", latitude = 11.1, longitude = 77.0,
                timestamp = System.currentTimeMillis(), message = "Fall detected"
            ),
            SosAlertEntity(
                id = "sos-3", hikerName = "Hiker3", trailId = "t1", trailName = "Trail1",
                alertType = "CHECKIN_MISSED", latitude = 10.9, longitude = 76.8,
                timestamp = System.currentTimeMillis(), message = "Missed check-in"
            )
        )

        alerts.forEach { sosAlertDao.insert(it) }
        val active = sosAlertDao.getActiveAlerts().first()
        assertEquals(3, active.size)

        // Resolve one
        sosAlertDao.resolve("sos-2")
        val remaining = sosAlertDao.getActiveAlerts().first()
        assertEquals(2, remaining.size)
    }

    // ==================== Trail Delete Test (Session 10 - Admin CRUD) ====================

    @Test
    fun deleteTrailById() = runTest {
        trailDao.insertTrails(listOf(
            createTrailEntity("t1", "Trail to Keep"),
            createTrailEntity("t2", "Trail to Delete")
        ))

        val before = trailDao.getAllTrails().first()
        assertEquals(2, before.size)

        trailDao.deleteTrail("t2")

        val after = trailDao.getAllTrails().first()
        assertEquals(1, after.size)
        assertEquals("Trail to Keep", after[0].name)
    }

    // ==================== Helper ====================

    private fun createTrailEntity(
        id: String, name: String, difficulty: String = "Moderate",
        region: String = "Coimbatore", popularity: Int = 50
    ) = TrailEntity(
        id = id, name = name, description = "Test trail",
        difficulty = difficulty, distance = 10.0, estimatedDuration = "4 hours",
        elevationGain = 500, rating = 4.0,
        coordinates = "[{\"latitude\":11.0,\"longitude\":76.9}]",
        startLat = 11.0, startLng = 76.9, endLat = 11.1, endLng = 77.0,
        hazards = "[\"Rocks\"]", region = region, popularity = popularity,
        coverageStatus = "Full",
        elevationProfile = "[{\"distance\":0.0,\"elevation\":100}]"
    )
}
