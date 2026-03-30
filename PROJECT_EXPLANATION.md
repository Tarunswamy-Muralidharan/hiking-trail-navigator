# Hiking Trail Navigator - Complete Project Explanation

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [System Architecture](#2-system-architecture)
3. [UML Diagram Mapping (Exp 2-6)](#3-uml-diagram-mapping-exp-2-6)
   - 3.1 Use Case Diagram (Exp 2)
   - 3.2 Class Diagram (Exp 3)
   - 3.3 Sequence Diagram (Exp 4)
   - 3.4 Collaboration Diagram (Exp 4)
   - 3.5 Activity Diagram (Exp 5)
   - 3.6 State Chart Diagram (Exp 5)
4. [All 16 SRS Features - How Each Works](#4-all-16-srs-features---how-each-works)
5. [Database Design](#5-database-design)
6. [Navigation Flow](#6-navigation-flow)
7. [Key Algorithms](#7-key-algorithms)
8. [Admin System & Inter-App Communication](#8-admin-system--inter-app-communication)
9. [Technology Stack Details](#9-technology-stack-details)
10. [File-by-File Reference](#10-file-by-file-reference)

---

## 1. Project Overview

**Hiking Trail Navigator** is a native Android application that provides comprehensive hiking trail navigation with real-time safety monitoring. It addresses a real-world problem: hikers in the Western Ghats (Coimbatore region) face risks from wildlife, landslides, no-coverage zones, and falls, with limited rescue infrastructure.

**The system has two apps:**
- **Main App** (`com.hikingtrailnavigator.app`) - For hikers: trail discovery, navigation, safety alerts, SOS
- **Trail Admin App** (`com.hikingtrailnavigator.admin`) - For forest officers: monitor hikers, manage trails, respond to SOS

**Key numbers:**
- 16 functional requirements fully implemented
- 15 Room database entities (DB version 12)
- 11 UML domain model classes
- 9 Android services
- 73 automated tests (43 unit + 30 instrumented)
- 10 seeded trails (9 real Western Ghats trails + 1 PSG iTech campus demo)

---

## 2. System Architecture

```
+------------------------------------------------------------------+
|                    PRESENTATION LAYER                              |
|  Jetpack Compose UI Screens + ViewModels (MVVM) + Hilt DI        |
|  LoginScreen, HomeScreen, TrailListScreen, TrailDetailScreen,     |
|  DirectionsScreen, ActiveHikeScreen, SOSScreen,                   |
|  SafetyDashboardScreen, HazardReportScreen, LiveTrackingScreen,   |
|  EmergencyContactsScreen, RouteWarningsScreen, AdminScreens...    |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                      DOMAIN LAYER                                  |
|  11 UML Domain Classes (Trail.kt)                                 |
|  User (abstract) -> Hiker, ForestOfficer, Admin                   |
|  HikeSession, SOSAlert, Notification, SafetyCheckIn, Location     |
|  Trail, DangerZone, NoCoverageZone, LowActivityZone, HazardReport |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                       DATA LAYER                                   |
|  Repositories (6): Trail, Hazard, Activity, Contact, Session,     |
|                     User, Notification, SafetyCheckIn              |
|  Room Database: 15 entities, 15 DAOs                              |
|  Remote API: Retrofit + OkHttp (offline-first)                    |
|  ContentProvider: IPC with Trail Admin app                        |
+------------------------------------------------------------------+
                              |
                              v
+------------------------------------------------------------------+
|                     SERVICE LAYER                                  |
|  GeofencingService    - Trail deviation, zone detection            |
|  LocationTrackingService - GPS tracking, route recording           |
|  EmergencyService     - SOS SMS, GPS location, vibration           |
|  FallDetectionService - Accelerometer + gyroscope 3-phase          |
|  ConnectivityService  - Network monitoring                         |
|  WeatherService       - Weather data and risk assessment           |
|  CheckInService       - Periodic safety check-ins                  |
|  SessionManager       - User session persistence                   |
|  SosNotificationService - Push notifications for SOS               |
|  RiskAssessmentService  - Combined risk score computation          |
+------------------------------------------------------------------+
```

**Design Patterns Used:**
- **MVVM** (Model-View-ViewModel) - All screens have dedicated ViewModels with StateFlow
- **Repository Pattern** - Abstracts data sources (Room DB + API) behind repositories
- **Dependency Injection** - Hilt/Dagger provides all dependencies
- **Observer Pattern** - Kotlin StateFlow/Flow for reactive data streams
- **Singleton** - Services and repositories are @Singleton scoped
- **Strategy Pattern** - Risk assessment combines multiple risk strategies (weather, terrain, coverage)

---

## 3. UML Diagram Mapping (Exp 2-6)

### 3.1 Use Case Diagram (Experiment 2)

The Use Case Diagram defines **3 actors** and **16 use cases**. Here is exactly how each maps to the code:

#### Actors

| UML Actor | Code Representation | How It Works |
|-----------|-------------------|-------------|
| **Hiker** | `Hiker` class in `Trail.kt:15-23` | User who logs in via `LoginScreen`. Stored in `users` table with `role = "Hiker"`. Has `experienceLevel`, `emergencyContact`, `currentTrailId`. |
| **Forest Officer** | `ForestOfficer` class in `Trail.kt:25-35` | Logs in via `AdminLoginScreen` (admin/admin123). Has `badgeNumber`, `assignedRegion`. Receives SOS notifications via `SosNotificationService`. |
| **Admin** | `Admin` class in `Trail.kt:37-46` | Same login as ForestOfficer. Can manage trails (CRUD), moderate hazard reports, manage danger zones. |

#### Use Cases -> Code Mapping

| Use Case | Screen/File | How User Interacts |
|----------|------------|-------------------|
| **Browse Trails** | `TrailListScreen.kt` + `TrailsViewModel.kt` | User sees all 10 trails sorted by popularity. Can search by name/region, filter by difficulty (chips), sort by popularity/distance/rating/difficulty. |
| **View Trail Details** | `TrailDetailScreen.kt` | Tapping a trail shows: description, difficulty badge, distance, duration, elevation gain, rating, hazards list, elevation profile chart, risk assessment score, and two buttons - "Directions" and "Start Hike". |
| **View Directions** | `DirectionsScreen.kt` | Full-screen satellite map centered on trail location. Shows green safe route (glow + inner polyline), danger zone circles/markers, start (green) and end (red) markers. Trail info bar at top. "Start Hike" button at bottom. |
| **Start Hike** | `ActiveHikeScreen.kt` + `ActiveHikeViewModel.kt` | Map follows user GPS. Shows real-time stats (time, distance, elevation, pace, ETA, remaining). Turn-by-turn navigation with compass bearing. Orange walked path drawn on map. |
| **Emergency SOS** | `SOSScreen.kt` + `SOSViewModel.kt` | Big red SOS button. When pressed: gets GPS location, sends SMS to all emergency contacts + Forest Dept + SDMA, persists `SosAlert` to DB, sends push notification to forest officer, vibrates SOS pattern. |
| **Silent SOS** | `SOSScreen.kt` line "Silent SOS" button | Same as SOS but `isSilentMode = true` - no vibration, no visible indicators. Also triggered by 5 rapid volume-down presses (`MainActivity.kt` `onKeyDown`). |
| **Report Hazard** | `HazardReportScreen.kt` + `HazardReportViewModel.kt` | User selects hazard type (Wildlife/Landslide/Flooding/etc.), severity (Low-Critical), enters description. Auto-captures GPS. Report saved with 7-day expiration. Other users can confirm reports (upvote). |
| **View Safety Dashboard** | `SafetyDashboardScreen.kt` + `SafetyDashboardViewModel.kt` | Shows: overall risk level, weather data (temp/humidity/wind/rain/UV), weather alerts, danger zones list, no-coverage zones list, hazard reports, emergency contact count, online/offline status. |
| **Manage Contacts** | `EmergencyContactsScreen.kt` + `EmergencyContactsViewModel.kt` | Add/delete emergency contacts (name, phone, relation). These contacts receive SOS SMS messages. |
| **Live Location Sharing** | `LiveTrackingScreen.kt` + `LiveTrackingViewModel.kt` | Toggle tracking on/off. Toggle location sharing. Configurable update interval (5s/15s/30s/60s). Generates Google Maps share link. |
| **View Activity History** | `ActivityHistoryScreen.kt` + `NavigateViewModel.kt` | Shows total hikes, total distance, total duration. Lists all past hikes with date, distance, duration, elevation, user difficulty rating. |
| **Admin: Monitor Hikers** | `AdminDashboardScreen.kt` | Shows active hikers with GPS, last check-in time, missed check-in count. Color-coded: green (safe), orange (1 missed), red (2+ missed). |
| **Admin: Manage Trails** | `AdminManageTrailsScreen.kt` + `AdminAddTrailScreen.kt` | View all trails, add new trails (name, description, difficulty, distance, elevation, coordinates, hazards), delete trails. |
| **Admin: Respond to SOS** | `AdminDashboardScreen.kt` SOS section | Lists active SOS alerts with type (SOS_BUTTON/FALL_DETECTED/CHECKIN_MISSED), hiker name, GPS, trail, time. "Resolve" button marks alert as handled. |
| **Admin: Moderate Hazards** | `AdminDashboardScreen.kt` Hazard section | Lists all hazard reports with confidence level (Low/Medium/High based on confirmations), expiration countdown, verified status. Verify/Reject buttons. |
| **Admin: Manage Warnings** | `AdminDashboardScreen.kt` Warnings section | Lists crowdsourced route warnings with type, description, reporter, upvotes. Deactivate button removes false reports. |

---

### 3.2 Class Diagram (Experiment 3)

The Class Diagram defines **11 domain classes** with inheritance, composition, and associations. All are implemented in `domain/model/Trail.kt` and persisted via Room entities in `data/local/entity/TrailEntity.kt`.

#### Class Hierarchy: User Inheritance

```
                    User (abstract)
                 /       |        \
              Hiker  ForestOfficer  Admin
```

**Code: `Trail.kt` lines 5-46**

```kotlin
// Abstract base class
open class User(
    open val userId: String,
    open val name: String,
    open val email: String,
    open val phoneNumber: String,
    open val role: UserRole     // enum: Hiker, ForestOfficer, Admin
)

// Subclass 1: Hiker
data class Hiker(
    // inherits User fields +
    val experienceLevel: String,    // "Beginner", "Intermediate", "Expert"
    val emergencyContact: String,
    val currentTrailId: String?     // currently active trail
) : User(...)

// Subclass 2: ForestOfficer
data class ForestOfficer(
    // inherits User fields +
    val badgeNumber: String,        // "FO-2026-001"
    val assignedRegion: String      // "Western Ghats - Coimbatore"
) : User(...) {
    fun respondToAlert(alertId: String): Boolean  // UML operation
    fun rescueHiker(hikerId: String): Boolean     // UML operation
}

// Subclass 3: Admin
data class Admin(
    // inherits User fields +
    val assignedRegion: String
) : User(...) {
    fun manageTrails(): Boolean       // UML operation
    fun manageDangerZones(): Boolean  // UML operation
}
```

**Persistence:** Single table `users` with `role` discriminator column. `UserEntity.toDomainUser()` converts to the correct subclass based on role.

#### Association: HikeSession

```
Hiker  1 -------- *  HikeSession  * -------- 1  Trail
                        |
                     Location *
                     SafetyCheckIn *
                     SOSAlert *
```

**Code: `Trail.kt` lines 48-63**

```kotlin
data class HikeSession(
    val sessionId: String,
    val hikerId: String,        // FK -> User
    val trailId: String,        // FK -> Trail
    val trailName: String,
    val startTime: Long,
    val endTime: Long?,
    val status: HikeStatus      // NotStarted, Active, Paused, Completed, Emergency
) {
    fun startHike(): HikeSession    // UML operation -> changes status to Active
    fun endHike(): HikeSession      // UML operation -> changes status to Completed
    fun trackLocation(): Boolean    // UML operation -> returns true if Active
}
```

**How it's used:**
- When user taps "Start Hike" on `TrailDetailScreen`, `ActiveHikeViewModel.init` creates a `HikeSession` with status `Active` and persists it via `HikeSessionRepository.startSession()`.
- Every GPS update calls `hikeSessionRepository.addLocation()` creating `Location` entries linked to the session.
- Safety check-ins are persisted as `SafetyCheckIn` entries linked to the session.
- If SOS is triggered, a `SOSAlert` is created linked to the session.

#### All 11 UML Classes

| UML Class | Kotlin File | Entity Table | Key Attributes | Key Operations |
|-----------|-------------|-------------|----------------|---------------|
| `User` | `Trail.kt:7-13` | `users` | userId, name, email, phone, role | - |
| `Hiker` | `Trail.kt:15-23` | `users` (role="Hiker") | experienceLevel, emergencyContact | selectTrail(), startHike() |
| `ForestOfficer` | `Trail.kt:25-35` | `users` (role="ForestOfficer") | badgeNumber, assignedRegion | respondToAlert(), rescueHiker() |
| `Admin` | `Trail.kt:37-46` | `users` (role="Admin") | assignedRegion | manageTrails(), manageDangerZones() |
| `Trail` | `Trail.kt:117-136` | `trails` | name, difficulty, distance, coordinates, hazards | getRiskLevel() |
| `HikeSession` | `Trail.kt:52-64` | `hike_sessions` | hikerId, trailId, status, startTime | startHike(), endHike(), trackLocation() |
| `Location` | `Trail.kt:68-74` | `locations` | latitude, longitude, timestamp, sessionId | - |
| `SOSAlert` | `Trail.kt:78-93` | `sos_alerts` | alertType, latitude, longitude, status | resolve() |
| `Notification` | `Trail.kt:97-104` | `notifications` | alertId, recipientId, message, status | - |
| `SafetyCheckIn` | `Trail.kt:108-113` | `safety_checkins` | sessionId, scheduledTime, responseStatus | - |
| `DangerZone` | `Trail.kt:146-155` | `danger_zones` | center, radius, type, severity | - |

---

### 3.3 Sequence Diagram (Experiment 4)

The Sequence Diagram shows the **SOS Emergency Flow** - a 9-step message sequence between objects.

**Code: `SafetyViewModel.kt` - `SOSViewModel.activateSos()` (lines 127-213)**

```
Step  UML Message                      Code Implementation
----  -----------                      -------------------
1.    Hiker -> HikeSession:            User taps SOS button on SOSScreen.kt
      triggerSOS()                     SOSViewModel.activateSos(location) called

2.    HikeSession -> Location:         emergencyService.getCurrentLocation()
      getCurrentLocation()             Uses FusedLocationProviderClient (GPS)

3.    Location -> HikeSession:         Returns LatLng(latitude, longitude)
      return currentCoordinates        Stored in uiState.currentLocation

4.    HikeSession -> SOSAlert:         SOSAlert object created with:
      createSOSAlert(coordinates)      alertId, hikerId, trailId, alertType,
                                       latitude, longitude, message

5.    SOSAlert -> SOSAlert:            sosAlertDao.insert(sosAlert.toSosEntity())
      logAlert()                       Persisted to sos_alerts table

6.    SOSAlert -> Notification:        notificationRepository.sendNotification(
      sendAlert(forestOfficer)         Notification(alertId, recipientId="officer_1",
                                       message="SOS ALERT: $hikerName needs help!"))

7.    Notification -> ForestOfficer:   sosNotificationService.sendSosAlert(...)
      notifySOS(alertDetails)          Creates Android notification channel,
                                       sends system notification to device

8.    ForestOfficer -> SOSAlert:       On AdminDashboardScreen, officer taps
      acknowledgeAlert()               "Resolve" -> AdminViewModel.resolveAlert(id)
                                       -> sosAlertDao.resolve(id) sets isResolved=1

9.    System -> Hiker:                 uiState updated with confirmation:
      sendConfirmation()               "SOS confirmed! Forest officer notified.
                                       X contacts alerted."
```

**Additionally, SMS is sent in parallel (Step 1b):**
```kotlin
emergencyService.sendSosToAllContacts(location)
// Sends SMS to ALL emergency contacts + Forest Dept (1800-425-1600) + SDMA (1070)
// Uses SmsManager.sendMultipartTextMessage() - works WITHOUT internet
```

---

### 3.4 Collaboration Diagram (Experiment 4)

The Collaboration Diagram shows **object interactions** during a complete hiking session. Numbers indicate message ordering.

```
                         +------------------+
                    1.   |                  |   2.
         LoginScreen --> | SessionManager   | --> HomeScreen
                         |  (saves userId)  |
                         +------------------+
                                  |
                             3. selectTrail()
                                  |
                                  v
+----------------+  4.  +-------------------+  5.  +-------------------+
|                | ---> |                   | ---> |                   |
| TrailListScreen|      | TrailDetailScreen |      | DirectionsScreen  |
| (TrailsVM)     |      | (TrailRepo)       |      | (map + route)     |
+----------------+      +-------------------+      +-------------------+
                                  |
                            6. startHike()
                                  |
                                  v
                         +-------------------+     +--------------------+
                         | ActiveHikeScreen  |<--->| GeofencingService  |
                         | (ActiveHikeVM)    |     | (deviation check)  |
                         +-------------------+     +--------------------+
                          |    |    |    |
           7. location    |    |    |    |    11. checkIn
              updates     |    |    |    |    response
                          v    |    |    v
                  +----------+ | +----------+  +------------------+
                  | Location | | | CheckIn  |  | FallDetection    |
                  | Tracking | | | Service  |  | Service          |
                  | Service  | | +----------+  | (accel+gyro)     |
                  +----------+ |               +------------------+
                               |                        |
                        8. dangerZone           9. fallDetected
                           /noCoverage             |
                           check                   v
                               |            +------------------+
                               v            | EmergencyService |
                      +---------------+     | (SMS + vibrate)  |
                      | Alert Dialog  |     +------------------+
                      | (warn user)   |             |
                      +---------------+      10. sendSOS()
                                                    |
                                                    v
                                            +------------------+
                                            | SosNotification  |
                                            | Service          |
                                            | (notify admin)   |
                                            +------------------+
```

**Implementation files for each object:**

| Object | File | Role |
|--------|------|------|
| LoginScreen | `ui/screens/login/LoginScreen.kt` | Captures hiker name, saves to SessionManager |
| SessionManager | `service/SessionManager.kt` | SharedPreferences - stores userId, hikerName, activeSessionId |
| TrailsViewModel | `ui/screens/trails/TrailsViewModel.kt` | Loads trails, handles search/filter/sort |
| TrailRepository | `data/repository/TrailRepository.kt` | Queries TrailDao, DangerZoneDao, NoCoverageZoneDao, LowActivityZoneDao |
| ActiveHikeViewModel | `ui/screens/navigate/NavigateViewModel.kt` | Core hike logic - location, zones, check-ins, fall detection |
| GeofencingService | `service/GeofencingService.kt` | Haversine distance calculation, zone boundary checks |
| LocationTrackingService | `service/LocationTrackingService.kt` | FusedLocationProviderClient, 5-second GPS updates |
| FallDetectionService | `service/FallDetectionService.kt` | 3-phase algorithm: freefall -> impact -> rotation |
| EmergencyService | `service/EmergencyService.kt` | SMS sending, GPS location fetch, vibration patterns |
| SosNotificationService | `service/SosNotificationService.kt` | Android notification channel for SOS alerts |
| CheckInService | `service/CheckInService.kt` | Periodic timer, escalation logic |

---

### 3.5 Activity Diagram (Experiment 5)

The Activity Diagram shows the **workflow** with **3 swimlanes**: Hiker, System, and Admin.

#### Swimlane 1: Hiker Actions

```
[Start] --> Login (enter name) --> Browse Trails --> Select Trail --> View Details
    |
    +--> View Directions (map with route, danger zones)
    |
    +--> Start Hike
            |
            +--> [Active Hiking Loop]
            |     |
            |     +--> Receive turn-by-turn guidance
            |     +--> Respond to safety check-in
            |     +--> Report hazard
            |     +--> Share live location
            |     |
            |     +--> [Decision] Off trail? --> Yes: See deviation warning
            |     +--> [Decision] In danger zone? --> Yes: See alert + vibration
            |     +--> [Decision] No coverage? --> Yes: See warning
            |     +--> [Decision] Low activity area? --> Yes: See caution alert
            |     |
            |     +--> [Decision] Emergency?
            |           +--> Yes: Tap SOS / Silent SOS / Volume button x5
            |           +--> Fall detected? --> 30s countdown --> Auto-SOS
            |
            +--> End Hike --> Rate difficulty (1-5 stars) --> View summary
```

**Code flow:**
1. `LoginScreen.kt` -> `SessionManager.saveHikerName()`
2. `HomeScreen.kt` -> bottom nav to Trails tab
3. `TrailListScreen.kt` -> tap trail -> `TrailDetailScreen.kt`
4. Tap "Directions" -> `DirectionsScreen.kt` (map centered on trail)
5. Tap "Start Hike" -> `ActiveHikeScreen.kt` (map follows GPS)
6. During hike: `ActiveHikeViewModel.onLocationUpdate()` runs on every GPS fix:
   - Calculates distance, elevation, pace, ETA
   - Checks trail deviation via `GeofencingService.getDistanceFromTrail()`
   - Checks danger zones via `GeofencingService.isInsideZone()`
   - Checks no-coverage zones
   - Checks low-activity zones
   - Updates turn-by-turn waypoint guidance
7. Check-in timer fires every `checkInIntervalMinutes` (from trail data)
8. Fall detection runs continuously via `FallDetectionService`
9. End hike -> difficulty rating dialog -> save `HikeActivity`

#### Swimlane 2: System Actions

```
[On Location Update] --> Calculate distances --> Check geofences
    |
    +--> Trail deviation > 100m? --> Show warning card
    +--> Inside danger zone? --> Show alert dialog + vibrate
    +--> Inside no-coverage? --> Show warning + suggest offline mode
    +--> Inside low-activity? --> Show caution alert
    |
[On Check-in Timer] --> Show check-in dialog
    +--> No response 2min --> Escalation Level 1 (urgent warning)
    +--> No response 4min --> Escalation Level 2 (auto-SOS)
    |
[On Fall Detected] --> Show countdown dialog (30s)
    +--> No response --> Auto-SOS with FALL_DETECTED type
    |
[On SOS Triggered] --> Get GPS --> Send SMS --> Persist SOSAlert
    --> Send Notification --> Update Admin Dashboard
```

#### Swimlane 3: Admin Actions

```
[Login] --> View Dashboard
    |
    +--> Monitor active hikers (GPS, check-in status)
    +--> View SOS alerts --> Respond --> Resolve
    +--> Moderate hazard reports --> Verify / Reject
    +--> Manage route warnings --> Deactivate false reports
    +--> Manage trails --> Add / Delete trails
```

**Code: Navigation graph in `Navigation.kt` + `HikerApp.kt`**

---

### 3.6 State Chart Diagram (Experiment 5)

The State Chart shows the **HikeSession lifecycle** with 5 states and transitions.

```
                    selectTrail()
    [LoggedIn] ─────────────────> [SelectingTrail]
                                        |
                                   startHike()
                                        |
                                        v
                            ┌─────> [Hiking] <──────┐
                            |          |  |          |
                       resumeHike()    |  |     pauseHike()
                            |          |  |          |
                            |          |  └────> [Paused]
                            |          |
                            |    DangerDetected /
                            |    fallDetected /
                            |    checkInMissed
                            |          |
                            |          v
                            |    [DangerDetected]
                            |          |
                            |     triggerSOS()
                            |          |
                            |          v
                            |    [AlertSent]
                            |          |
                            |    resolveAlert()
                            |          |
                            └──────────┘
                                        |
                                   endHike()
                                        |
                                        v
                                  [HikeEnded]
```

**Code implementation in `ActiveHikeViewModel`:**

| State | HikeStatus enum | When it happens | Code |
|-------|----------------|-----------------|------|
| **LoggedIn** | - | User enters name on LoginScreen | `SessionManager.saveHikerName()` |
| **SelectingTrail** | `NotStarted` | User browses trails | `TrailListScreen` / `TrailDetailScreen` |
| **Hiking** | `Active` | Start Hike tapped | `hikeSessionRepository.startSession(session)` with `status = Active` |
| **Paused** | `Paused` | Pause button tapped | `hikeSessionRepository.updateStatus(id, HikeStatus.Paused)` |
| **DangerDetected** | `Active` | Fall/deviation/zone entry | `onFallDetected()`, danger zone check in `onLocationUpdate()` |
| **AlertSent** | `Emergency` | SOS auto-triggered | `hikeSessionRepository.updateStatus(id, HikeStatus.Emergency)` |
| **HikeEnded** | `Completed` | End Hike confirmed | `hikeSessionRepository.endSession(id)` sets `status = Completed` |

**State transitions in code:**
```kotlin
// Hiking -> Paused
fun togglePause() {
    hikeSessionRepository.updateStatus(currentSessionId,
        if (newPaused) HikeStatus.Paused else HikeStatus.Active)
}

// Hiking -> Emergency (via fall detection)
private fun onFallDetected() {
    // 30-second countdown, if no response:
    hikeSessionRepository.updateStatus(currentSessionId, HikeStatus.Emergency)
    emergencyService.sendSosToContacts(loc)
}

// Hiking -> Completed
fun submitRatingAndEndHike() {
    hikeSessionRepository.endSession(currentSessionId) // sets Completed
    activityRepository.saveActivity(activity)
}
```

---

## 4. All 16 SRS Features - How Each Works

### FR-101: Trail Discovery and Search

**What it does:** Users browse, search, filter, and sort trails.

**How it works:**
- `TrailListScreen.kt` displays all trails from Room DB via `TrailsViewModel`
- **Search**: `TrailDao.searchTrails(query)` uses SQL `LIKE` on name and region columns
- **Filter**: Difficulty filter chips (Easy/Moderate/Hard/Expert) -> `TrailDao.getTrailsByDifficulty()`
- **Sort**: SortOption enum -> sorts by Popularity (default), Distance, Rating, or Difficulty
- **Data**: 10 pre-seeded trails in `SeedData.kt` with real coordinates, distances, elevation profiles
- **Offline**: All trail data cached in Room DB, no internet needed to browse

**Key files:** `TrailListScreen.kt`, `TrailsViewModel.kt`, `TrailDao.kt`, `SeedData.kt`

---

### FR-102: Real-Time Navigation and Guidance

**What it does:** GPS-based turn-by-turn navigation during active hike.

**How it works:**
- `ActiveHikeScreen.kt` shows osmdroid map with `followMyLocation = true`
- On each GPS update, `ActiveHikeViewModel.onLocationUpdate()`:
  - Calculates distance to next trail waypoint using Haversine formula
  - Computes compass bearing (N/NE/E/SE/S/SW/W/NW) using `getBearingDirection()`
  - When within 30m of a waypoint, advances to the next one
  - Displays: "Head NE - 145m to next waypoint" in the stats bar
- **Stats shown**: Time, Distance, Elevation, Remaining distance, ETA, Pace
- Distance remaining = total trail distance - distance traveled
- ETA = distance remaining / average speed

**Key files:** `ActiveHikeScreen.kt`, `NavigateViewModel.kt` (ActiveHikeViewModel), `GeofencingService.kt`

---

### FR-104: Activity Tracking and Statistics

**What it does:** Records and displays hiking performance data.

**How it works:**
- During hike, `ActiveHikeViewModel` tracks:
  - Distance: Haversine cumulative sum between GPS points
  - Elevation gain: Sum of positive altitude changes
  - Pace: elapsed time / distance (min/km)
  - Speed: segment distance / segment time (km/h)
  - Route: List of all GPS points (stored as JSON in `hike_activities.route`)
- On hike end, `HikeActivity` saved via `ActivityRepository.saveActivity()`
- `ActivityHistoryScreen` shows:
  - Total hikes count, total distance, total duration (from `HikeActivityDao` aggregate queries)
  - Individual hike cards with all metrics
- Post-hike difficulty rating (1-5 stars) stored in `userDifficultyRating` field

**Key files:** `NavigateViewModel.kt`, `ActivityHistoryScreen.kt`, `HikeActivityDao.kt`

---

### FR-105: Trail Difficulty Assessment and Hazard Reporting

**What it does:** Post-hike difficulty feedback + community hazard reports with expiration.

**How it works:**
- **Difficulty feedback**: When ending a hike, a star rating dialog appears (1=Very Easy to 5=Very Hard). Rating saved in `HikeActivity.userDifficultyRating`.
- **Hazard reporting**: `HazardReportScreen` lets users select type (Wildlife/Landslide/Flooding/Fallen Tree/Unsafe Terrain/Other), severity (Low-Critical), description. Auto-captures GPS location.
- **Expiration**: Each report has `expiresAt = reportedAt + 7 days`. `HazardReportDao.getActiveHazardReports()` filters by `expiresAt > now`. Admin dashboard shows expiration countdown.
- **Community validation**: Other users can tap "Confirm" to increment `confirmations` count. Confidence level: Low (0), Medium (1-2), High (3+).

**Key files:** `ActiveHikeScreen.kt` (rating dialog), `HazardReportScreen.kt`, `SafetyViewModel.kt`, `HazardReportEntity.kt`

---

### FR-201: Periodic Safety Check-In System

**What it does:** Prompts user periodically, escalates if no response.

**How it works:**
- Timer starts in `ActiveHikeViewModel.startCheckInTimer()` with interval from trail data (default 60 min)
- When timer fires: `SafetyCheckIn` entity created with `status = "pending"`, dialog shown
- **Tier 0** (0-2 min): "Are you doing okay?" dialog with "I'm Safe" button
- **Tier 1** (after 2 min no response): `escalationLevel = 1`, urgent warning, check-in marked as `missed`
- **Tier 2** (after 4 min total): `escalationLevel = 2`, auto-SOS triggered:
  - Session status -> `Emergency`
  - SMS sent to all contacts
  - `SOSAlert` persisted with `alertType = "CHECKIN_MISSED"`
  - Push notification to forest officer
- User can acknowledge at any time to reset escalation

**Key files:** `NavigateViewModel.kt` (lines 186-265), `SafetyCheckInEntity.kt`, `SafetyCheckInRepository.kt`

---

### FR-202: Manual SOS Emergency Trigger

**What it does:** One-tap emergency alert with continuous location.

**How it works:**
- `SOSScreen` has large red SOS button
- `SOSViewModel.activateSos(location)` follows 9-step UML sequence (see Section 3.3)
- SMS sent via `SmsManager.sendMultipartTextMessage()` - works without internet
- Message includes: GPS coordinates, Google Maps link, app name
- Recipients: All user emergency contacts + TN Forest Dept (1800-425-1600) + SDMA (1070)
- SOS vibration pattern: 3 short, 3 long, 3 short (Morse SOS)
- `SosAlert` persisted to DB for admin dashboard
- `Notification` entity sent to forest officer
- Android push notification via `SosNotificationService`

**Key files:** `SOSScreen.kt`, `SafetyViewModel.kt` (SOSViewModel), `EmergencyService.kt`

---

### FR-203: Automatic Fall Detection System

**What it does:** Detects falls using phone sensors, auto-escalates.

**How it works - 3-Phase Algorithm:**
1. **Phase 1 - Freefall**: Accelerometer reads < 4.5 m/s^2 (normal gravity is ~9.8). Records `freefallTime`.
2. **Phase 2 - Impact**: Accelerometer spike > 18 m/s^2. Records `impactTime`.
3. **Phase 3 - Rotation**: Gyroscope reads > 2.5 rad/s. Records `rotationTime`.
4. **Validation**: All 3 phases must occur within 2500ms window. 5-second cooldown between alerts.

**When fall detected:**
- `FallDetectionService` emits `fallDetected = true` via StateFlow
- `ActiveHikeViewModel.onFallDetected()` shows dialog with 30-second countdown
- If user taps "I'm Fine" -> countdown cancelled, fall detection reset
- If no response after 30s -> auto-SOS with `alertType = "FALL_DETECTED"`

**Thresholds (tuned for demo-friendliness):**
| Parameter | Demo Value | Real-World Value |
|-----------|-----------|-----------------|
| Impact threshold | 18 m/s^2 | 25 m/s^2 |
| Freefall threshold | 4.5 m/s^2 | 3.0 m/s^2 |
| Rotation threshold | 2.5 rad/s | 5.0 rad/s |
| Time window | 2500 ms | 2000 ms |

**Key files:** `FallDetectionService.kt`, `NavigateViewModel.kt` (lines 282-334)

---

### FR-204: Live Location Sharing and Monitoring

**What it does:** Share real-time location with configurable intervals.

**How it works:**
- `LiveTrackingScreen` has toggle to start/stop tracking
- Location sharing toggle sends updates to server via `HikerApi.sendLocationUpdate()`
- **Configurable intervals**: User selects 5s / 15s / 30s / 60s via filter chips
- `LiveTrackingViewModel.updateLocation()` checks `now - lastShareTime >= intervalMs` before sending
- Generates share link: `https://maps.google.com/?q=lat,lng`
- Works with `LocationTrackingService` (5-second GPS updates via FusedLocationProvider)

**Key files:** `LiveTrackingScreen.kt`, `SafetyViewModel.kt` (LiveTrackingViewModel)

---

### FR-205: Geo-Fencing and Route Deviation Alerts

**What it does:** Warns when user goes >100m off trail.

**How it works:**
- On each GPS update: `geofencingService.getDistanceFromTrail(currentPoint, trail.coordinates)`
- `getDistanceFromTrail()` finds minimum Haversine distance from current point to ANY trail waypoint
- If distance > 100m: `isDeviating = true`, yellow warning card shows: "You are Xm off trail!"
- Uses Haversine formula for accurate Earth-surface distance calculation

**Key files:** `NavigateViewModel.kt` (line 400-401), `GeofencingService.kt`

---

### FR-206: Environmental and Connectivity Risk Alerts

**What it does:** Weather warnings + network monitoring.

**How it works:**
- **Weather**: `WeatherService.fetchWeather()` returns temperature, humidity, wind, rain probability, UV index
  - Seasonal alerts: Monsoon (Jun-Sep) warns about rain/leeches, Summer (Mar-May) warns about heat
  - Risk score: rain>70%(+30), wind>40km/h(+25), UV>8(+15), each alert(+10)
  - Levels: "high" (>=50), "medium" (>=25), "low" (<25)
- **Connectivity**: `ConnectivityService` registers `NetworkCallback` to monitor internet
  - Emits `isOnline: StateFlow<Boolean>`
  - `SafetyDashboardScreen` shows offline warning card
  - `SOSScreen` shows "Offline - SOS will use SMS"

**Key files:** `WeatherService.kt`, `ConnectivityService.kt`, `SafetyDashboardScreen.kt`

---

### FR-207: Silent Emergency Alerts

**What it does:** Discreet SOS via software button or hardware gesture.

**How it works:**
- **Software**: "Silent SOS" button on `SOSScreen` -> `SOSViewModel.activateSilentSos()`
  - Same flow as regular SOS but `isSilentMode = true` -> no vibration
  - SMS still sent, notification still sent, but phone stays quiet
- **Hardware (Volume Button)**: `MainActivity.onKeyDown()` detects `KEYCODE_VOLUME_DOWN`
  - Counts rapid presses within 3-second window
  - 5 presses triggers `triggerSilentSos()` on background coroutine
  - Gets GPS location, sends SMS to all contacts silently
  - No screen indication, no vibration - completely discreet

**Key files:** `MainActivity.kt` (onKeyDown), `SafetyViewModel.kt` (activateSilentSos)

---

### FR-208: Danger Zone Identification and Alerts

**What it does:** Visual + haptic alerts when entering danger zones.

**How it works:**
- 8 danger zones seeded in `SeedData.kt` (wildlife corridors, landslide areas, flood zones, restricted areas)
- Each zone: center point (LatLng), radius (meters), type, severity, description
- **Map visualization**: Red circles with 60% opacity fill, danger zone markers with name/severity
- **Detection**: On each GPS update, `geofencingService.isInsideZone()` checks if user is within ANY zone radius
- **Alert on entry**: `AlertDialog` with zone name, type, severity, description, "I Understand" button
- **Vibration**: `emergencyService.triggerSOSVibration()` fires when entering zone (FR-208 enhancement)
- **Layer toggle**: User can show/hide danger zone layer via the layers panel

**Key files:** `NavigateViewModel.kt` (lines 404-408), `ActiveHikeScreen.kt` (danger zone dialog), `EmergencyService.kt`

---

### FR-209: No-Cell-Reception Area Detection and Warnings

**What it does:** Alerts before entering no-coverage zones.

**How it works:**
- 4 no-coverage zones seeded (Vellingiri deep forest, Topslip forest, Siruvani interior, Grass Hills)
- Detection: Same `isInsideZone()` check as danger zones
- **Pre-entry alert**: Dialog says "You are entering an area with no cellular network. Safety features will continue to work offline. SOS alerts will be sent once connectivity is restored."
- **Persistent indicator**: Gray signal-off card shown while inside zone
- Toggleable via layer panel

**Key files:** `NavigateViewModel.kt` (lines 410-413), `ActiveHikeScreen.kt`

---

### FR-210: Unexplored and Low-Activity Area Visualization

**What it does:** Shows areas with minimal hiking data on map.

**How it works:**
- 5 zones seeded: Anaikatti Ridge (unexplored), Siruvani Interior (low), Parambikulam Buffer (unexplored), Kovaipudur Hills (low), Valparai Corridor (unexplored)
- `LowActivityZoneEntity` stored in `low_activity_zones` table
- **Map visualization**: Gray circles - darker for "unexplored", lighter for "low" activity
- **Detection**: `isInsideZone()` check on each GPS update
- **Alert on entry**: Different messages for unexplored ("exercise extreme caution") vs low-activity ("limited trail data")
- Indicator card shown while inside zone
- Toggleable via layer panel

**Key files:** `LowActivityZoneEntity` in `TrailEntity.kt`, `LowActivityZoneDao` in `TrailDao.kt`, `NavigateViewModel.kt`, `ActiveHikeScreen.kt`, `SeedData.kt`

---

### FR-211: Combined Risk Awareness Overlay

**What it does:** Unified map overlay with toggleable layers and risk computation.

**How it works:**
- **Layer toggles**: Layers button on ActiveHikeScreen opens panel with 3 switches:
  - Danger Zones (red) - on/off
  - No Coverage (yellow) - on/off
  - Low Activity (gray) - on/off
- Each toggle controls: map circles, markers, alert detection, and indicator cards
- **Risk computation**: `RiskAssessmentService.assessTrailRisk()` computes composite score:
  - Difficulty: Easy(5) / Moderate(15) / Hard(30) / Expert(45)
  - Elevation: >1000m(20) / >500m(10)
  - Coverage: None(25) / Partial(10)
  - Nearby danger zones: +10 per zone (within radius + 500m buffer)
  - Weather: rain(15), wind(10), UV(5), alerts(10 each)
  - Risk levels: "critical" (>=60), "high" (>=40), "medium" (>=20), "low" (<20)
- Shown on `TrailDetailScreen` and `SafetyDashboardScreen`

**Key files:** `ActiveHikeScreen.kt` (LayerToggle component), `NavigateViewModel.kt`, `RiskAssessmentService.kt`

---

### FR-212: Community Validation and Moderation System

**What it does:** Validates hazard reports, admin moderation, expiration.

**How it works:**
- **Community validation**: Users can "Confirm" hazard reports -> increments `confirmations` count
  - Confidence levels: Low (0 confirmations), Medium (1-2), High (3+)
  - Visual indicator: Bold green if >=3 confirmations on `HazardReportScreen`
- **Route warning upvotes**: Similar system for `RouteWarningEntity.upvotes`
- **Admin moderation** (on `AdminDashboardScreen`):
  - Lists all hazard reports with confidence level, expiration countdown, verified status
  - **Verify** button: Sets `isVerified = true` (green background)
  - **Reject** button: Deletes the report from DB
- **Expiration**: Reports auto-expire after 7 days
  - `HazardReportEntity.expiresAt` set on creation
  - `HazardReportDao.getActiveHazardReports()` filters `WHERE expiresAt > now`
  - `HazardReportDao.deleteExpiredReports()` called on admin dashboard load
  - Expired reports shown as "EXPIRED" in admin view
- **Route warnings**: Admin can deactivate false reports (`isActive = 0`)

**Key files:** `AdminScreens.kt` (hazard moderation UI), `AdminViewModel.kt`, `HazardReportDao.kt`, `SafetyViewModel.kt` (HazardReportViewModel)

---

## 5. Database Design

### Entity-Relationship Summary (DB Version 12, 15 Entities)

```
users (userId PK, name, email, phoneNumber, role, experienceLevel?,
       emergencyContact?, currentTrailId?, badgeNumber?, assignedRegion?)
    |
    |-- 1:* -- hike_sessions (sessionId PK, hikerId FK, trailId FK, trailName,
    |                         startTime, endTime?, status)
    |              |
    |              |-- 1:* -- locations (id PK, lat, lng, timestamp, sessionId FK)
    |              |-- 1:* -- safety_checkins (checkInId PK, sessionId FK,
    |              |                           scheduledTime, responseStatus)
    |              |-- 1:* -- sos_alerts (id PK, hikerName, trailId, alertType,
    |                                    lat, lng, timestamp, isResolved, message)
    |
    |-- 1:* -- notifications (notificationId PK, alertId FK, recipientId FK,
                              message, timestamp, status)

trails (id PK, name, description, difficulty, distance, estimatedDuration,
        elevationGain, rating, coordinates JSON, startLat, startLng, endLat,
        endLng, hazards JSON, region, popularity, coverageStatus,
        elevationProfile JSON, schedule, checkInIntervalMinutes)

danger_zones (id PK, name, centerLat, centerLng, radius, type, severity,
              description, verified)

no_coverage_zones (id PK, name, centerLat, centerLng, radius, description)

low_activity_zones (id PK, name, centerLat, centerLng, radius,
                    activityLevel, description)

hazard_reports (id PK, type, severity, lat, lng, description, reportedAt,
               confirmations, expiresAt, isVerified)

hike_activities (id PK, trailId, trailName, startTime, endTime, distance,
                 duration, elevationGain, route JSON, checkIns, userDifficultyRating)

emergency_contacts (id PK, name, phone, relation)

active_hiker_sessions (id PK, hikerName, trailId, trailName, startTime,
                       lastCheckInTime, lastLat, lastLng, isActive, missedCheckIns)

route_warnings (id PK, trailId, lat, lng, warningType, description,
                reportedBy, reportedAt, upvotes, isActive)
```

### Type Converters
- `List<LatLng>` stored as JSON string in `coordinates` and `route` columns
- `List<ElevationPoint>` stored as JSON in `elevationProfile`
- `List<String>` stored as JSON in `hazards`
- Gson used for serialization/deserialization

---

## 6. Navigation Flow

```kotlin
// Navigation.kt - Screen routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Trails : Screen("trails")
    object TrailDetail : Screen("trail/{trailId}")
    object Directions : Screen("directions/{trailId}")
    object ActiveHike : Screen("active_hike/{trailId}")
    object Navigate : Screen("navigate")
    object ActivityHistory : Screen("activity_history")
    object Safety : Screen("safety")
    object SOS : Screen("sos")
    object HazardReport : Screen("hazard_report")
    object LiveTracking : Screen("live_tracking")
    object EmergencyContacts : Screen("emergency_contacts")
    object RouteWarnings : Screen("route_warnings")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AdminLogin : Screen("admin_login")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminManageTrails : Screen("admin_manage_trails")
    object AdminAddTrail : Screen("admin_add_trail")
}
```

**Bottom Navigation Tabs:** Home | Trails | Navigate | Safety | Profile

---

## 7. Key Algorithms

### Haversine Distance Formula
Used for all distance calculations (trail deviation, zone detection, distance traveled).

```kotlin
// GeofencingService.kt
fun haversineMeters(p1: LatLng, p2: LatLng): Double {
    val r = 6371000.0  // Earth radius in meters
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLng = Math.toRadians(p2.longitude - p1.longitude)
    val a = sin(dLat/2)^2 + cos(lat1) * cos(lat2) * sin(dLng/2)^2
    val c = 2 * atan2(sqrt(a), sqrt(1-a))
    return r * c  // distance in meters
}
```

### Fall Detection 3-Phase Algorithm
```
Time:  ─────|──freefall──|──impact──|──rotation──|─────
Accel: ─9.8─|───<4.5────|──>18.0───|────────────|─9.8─
Gyro:  ─0───|───────────|──────────|───>2.5─────|──0──
Window:      |←─────── 2500ms ──────────────────→|
```

### Compass Bearing Calculation
```kotlin
// ActiveHikeViewModel - turn-by-turn guidance
fun getBearingDirection(from: LatLng, to: LatLng): String {
    val bearing = atan2(sin(dLng)*cos(lat2),
                        cos(lat1)*sin(lat2) - sin(lat1)*cos(lat2)*cos(dLng))
    // Convert radians to degrees (0-360), then to compass direction
    // 0=N, 45=NE, 90=E, 135=SE, 180=S, 225=SW, 270=W, 315=NW
}
```

### Risk Assessment Scoring
```
Risk Score = Difficulty(5-45) + Elevation(0-20) + Coverage(0-25)
           + DangerZones(10 each) + Weather(0-50)

Levels: Low(<20) | Medium(20-39) | High(40-59) | Critical(60+)
```

---

## 8. Admin System & Inter-App Communication

### Main App Admin (Built-in)
- `AdminLoginScreen`: Username/password login (admin/admin123)
- `AdminDashboardScreen`: Live overview (active hikers, SOS alerts, hazard moderation, route warnings, trail management)
- `AdminManageTrailsScreen`: Trail CRUD operations
- `AdminAddTrailScreen`: Form to add new trails with coordinates

### Separate Trail Admin App (via ContentProvider)
The `adminapp/` module is a **separate APK** that communicates with the main app through Android's `ContentProvider` IPC mechanism.

```
Trail Admin App                    Main App
┌──────────────┐                 ┌──────────────────────┐
│ AdminActivity│                 │ HikerDataProvider    │
│ HikerDataReader ──(query)──> │ (ContentProvider)    │
│ AdminViewModel│                │   - trails           │
│ AdminApp     │                 │   - active_hikers    │
└──────────────┘                 │   - sos_alerts       │
                                 │   - danger_zones     │
                                 └──────────────────────┘
```

**Authority:** `com.hikingtrailnavigator.app.provider`
**Content URIs:** `content://com.hikingtrailnavigator.app.provider/trails`, `/active_hikers`, `/sos_alerts`, `/danger_zones`

---

## 9. Technology Stack Details

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Kotlin 1.9+ | Primary language |
| UI Framework | Jetpack Compose (Material 3) | Declarative UI |
| Architecture | MVVM + Clean Architecture | Separation of concerns |
| DI | Hilt (Dagger) | Dependency injection |
| Database | Room (SQLite) v12 | Local persistence |
| Maps | osmdroid + ESRI World Imagery | Offline-capable satellite maps |
| Location | FusedLocationProviderClient | GPS tracking |
| Sensors | Accelerometer + Gyroscope | Fall detection |
| Navigation | Jetpack Navigation Compose | Screen routing |
| Networking | Retrofit + OkHttp | REST API (offline-first) |
| Serialization | Gson | JSON <-> Kotlin conversion |
| Testing | JUnit 4 + Espresso + Compose Testing | 73 automated tests |

---

## 10. File-by-File Reference

### Screens (UI)
| File | Screen | SRS Features |
|------|--------|-------------|
| `LoginScreen.kt` | Hiker name entry / Admin login | User roles |
| `HomeScreen.kt` | Dashboard with quick actions | FR-101 |
| `TrailListScreen.kt` | Trail browse/search/filter | FR-101 |
| `TrailDetailScreen.kt` | Trail info, risk score | FR-101, FR-105 |
| `DirectionsScreen.kt` | Trail map with route | FR-102 |
| `ActiveHikeScreen.kt` | Live hiking with GPS | FR-102, FR-104, FR-205, FR-208, FR-209, FR-210, FR-211 |
| `NavigateScreen.kt` | Navigation hub | FR-102 |
| `ActivityHistoryScreen.kt` | Past hike stats | FR-104 |
| `SOSScreen.kt` | SOS + Silent SOS | FR-202, FR-207 |
| `SafetyDashboardScreen.kt` | Safety overview | FR-206, FR-211 |
| `HazardReportScreen.kt` | Report hazards | FR-105, FR-212 |
| `LiveTrackingScreen.kt` | Location sharing | FR-204 |
| `EmergencyContactsScreen.kt` | Manage contacts | FR-202 |
| `RouteWarningsScreen.kt` | Community warnings | FR-212 |
| `AdminScreens.kt` | Admin login/dashboard/trails | FR-212, Admin |
| `ProfileScreen.kt` | User profile | - |
| `SettingsScreen.kt` | App settings | - |

### ViewModels
| File | ViewModels | Purpose |
|------|-----------|---------|
| `NavigateViewModel.kt` | NavigateViewModel, ActiveHikeViewModel | Trail listing + active hike logic |
| `TrailsViewModel.kt` | TrailsViewModel, TrailDetailViewModel | Search/filter + detail/risk |
| `SafetyViewModel.kt` | SafetyDashboardVM, SOSVM, HazardVM, LiveTrackingVM, ContactsVM, WarningsVM | All safety features |
| `AdminViewModel.kt` | AdminViewModel | Admin dashboard + trail CRUD |
| `HomeViewModel.kt` | HomeViewModel | Home screen stats |
| `ProfileViewModel.kt` | ProfileViewModel | Profile settings |

### Services
| File | Service | SRS Features |
|------|---------|-------------|
| `GeofencingService.kt` | Haversine distance, zone detection | FR-205, FR-208, FR-209, FR-210 |
| `LocationTrackingService.kt` | GPS tracking (5s interval) | FR-102, FR-204 |
| `EmergencyService.kt` | SMS, GPS, vibration | FR-202, FR-207 |
| `FallDetectionService.kt` | 3-phase fall algorithm | FR-203 |
| `ConnectivityService.kt` | Network monitoring | FR-206 |
| `WeatherService.kt` | Weather data + risk | FR-206 |
| `CheckInService.kt` | Periodic check-ins | FR-201 |
| `SessionManager.kt` | SharedPreferences session | Login |
| `SosNotificationService.kt` | Push notifications | FR-202 |
| `RiskAssessmentService.kt` | Combined risk score | FR-211 |

### Data Layer
| File | Contents | Purpose |
|------|----------|---------|
| `TrailEntity.kt` | 15 entity classes + converters | Room entities + domain mapping |
| `TrailDao.kt` | 15 DAO interfaces | Database queries |
| `HikerDatabase.kt` | Room database config (v12) | DB setup |
| `TrailRepository.kt` | 7 repository classes | Data access abstraction |
| `SeedData.kt` | 10 trails, 8 danger zones, 4 no-coverage zones, 5 low-activity zones | Initial data |
| `DatabaseSeeder.kt` | Seeds DB on first launch | Data initialization |
| `HikerDataProvider.kt` | ContentProvider | Admin app IPC |
| `HikerApi.kt` | Retrofit API interface | Remote endpoints |
| `AppModule.kt` | Hilt DI providers | Dependency wiring |

### Domain Model
| File | Classes | UML Mapping |
|------|---------|-------------|
| `Trail.kt` | User, Hiker, ForestOfficer, Admin, HikeSession, Location, SOSAlert, Notification, SafetyCheckIn, Trail, DangerZone, NoCoverageZone, LowActivityZone, HazardReport, HikeActivity, EmergencyContact, WeatherData | All 11 UML classes + supporting models |
