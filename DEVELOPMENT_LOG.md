# Development Log - Hiking Trail Navigator

## Session 1-7: Initial Development (Jan-Mar 2026)
- Started as React Native / Expo app
- Built initial screens, navigation, and trail data
- Realized native Android would be better for sensor access and performance

## Session 8 (2026-03-16): Native Android Rebuild + Bug Fixes

### What was built
Rebuilt the entire app as a native Android/Kotlin application with Jetpack Compose.

### Bugs encountered and fixed

**Bug 1: App crash on launch**
- **Error**: `SecurityException: Missing permission ACCESS_NETWORK_STATE`
- **Root cause**: `ConnectivityService` required network state permission that wasn't declared in the manifest
- **Fix**: Added `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>` to AndroidManifest.xml

**Bug 2: osmdroid map crash**
- **Error**: `NullPointerException` in osmdroid tile loading on startup
- **Root cause**: osmdroid `Configuration.getInstance().load()` was not called before any MapView was created
- **Fix**: Moved configuration loading to `HikerApplication.onCreate()` so it runs before any UI renders

**Bug 3: Map tiles not loading (HTTP)**
- **Error**: Map showed blank/grey tiles, logcat showed `CleartextTrafficPermitted=false`
- **Root cause**: Android 9+ blocks cleartext HTTP by default; some tile servers use HTTP
- **Fix**: Added `android:usesCleartextTraffic="true"` to the `<application>` tag in AndroidManifest.xml

**Bug 4: End Hike navigation crash**
- **Error**: After ending a hike, `popBackStack(Screen.Navigate.route)` returned false and the app showed a blank screen
- **Root cause**: When user navigated Home -> TrailDetail -> ActiveHike, the Navigate screen was never in the back stack
- **Fix**: Added fallback logic: try `popBackStack(Navigate)`, if false try `popBackStack(Home)`

### Features completed
- SOS system (works offline via SMS)
- 8 Coimbatore hiking trails with danger zones
- Search with location suggestions and map zoom
- No-coverage zone warnings
- Admin control panel
- Crowdsourced route warnings
- Check-in notification system
- Custom app logo

---

## Session 9 (2026-03-17): Testing Suite

### What was built
Created comprehensive testing suite: 61 test cases across 8 files.

### Errors encountered and resolved

**Error: Hilt injection in tests**
- **Problem**: Instrumented tests failed with `UninitializedPropertyAccessException` - Hilt components weren't being created for test classes
- **Fix**: Created custom `HiltTestRunner.kt` extending `AndroidJUnitRunner` and set `testInstrumentationRunner` in build.gradle

**Error: Room in-memory DB for tests**
- **Problem**: Tests were modifying the real database
- **Fix**: Used `Room.inMemoryDatabaseBuilder()` in test setup to create isolated test databases

**Error: Coroutine testing**
- **Problem**: `Flow` collectors in tests would hang indefinitely
- **Fix**: Used `Turbine` library for Flow testing - `flow.test { awaitItem(); cancelAndConsumeRemainingEvents() }`

### Deliverables
- 40 unit tests + 21 instrumented tests = 61 total
- Testing_Documentation.pdf (29 pages)

---

## Session 10 (2026-03-18): Login, Admin CRUD, Campus Trail

### What was built
- Login/role selection screen
- Admin trail CRUD (add/delete)
- PSG iTech Campus trail (fun demo trail)
- SessionManager for hiker identity
- SOS alert persistence to DB

### Errors encountered and resolved

**Error: Admin exit -> blank screen**
- **Problem**: After logging out of admin, `popUpTo(Login)` removed the login screen from the stack, leaving nothing
- **Fix**: Changed to `popUpTo(startDestinationId) { inclusive = true }` + `launchSingleTop = true`

**Error: Tests failing after schema change**
- **Problem**: Added new Room entities but test DB schema was outdated
- **Fix**: Updated `DatabaseIntegrationTest` to include all new entities, bumped test count to 73

---

## Session 11 (2026-03-20): Realistic Trail Paths + SOS Notifications

### What was built
- All 9 trails now have realistic coordinate paths (10-20 waypoints each following actual terrain)
- SOS alerts now send Android push notifications to admin app
- Database version bumped to 8

### Errors encountered and resolved

**Error: Trail paths showing straight lines**
- **Problem**: Trails only had start/end points, so the polyline was a straight line
- **Fix**: Added 10-20 waypoints per trail following actual roads/paths from satellite imagery. E.g., Vellingiri Hills trek follows the real 7-hill path from Poondi (11.0100, 76.7950) to summit (10.9690, 76.7510)

**Error: SOS notification channel not created**
- **Problem**: Notifications weren't showing on Android 8+
- **Fix**: Created `NotificationChannel` in `SosNotificationService` with `IMPORTANCE_HIGH` and enabled vibration/sound

---

## Session 12 (2026-03-25): UML Alignment + Expo Cleanup

### What was built
- Removed all Expo/React Native code (App.js, src/, package.json, etc.)
- Added all 11 UML classes from OOSE experiments 2-6
- Fixed PSG iTech campus trail route

### Errors encountered and resolved

**Error: Expo files conflicting with Android project**
- **Problem**: Root-level package.json and node_modules confused some tools
- **Fix**: Deleted all Expo artifacts: App.js, app.json, src/, assets/, babel.config.js, package.json, package-lock.json

**Error: Campus trail going through buildings and highway**
- **Problem**: Original campus trail coordinates were too spread out, crossing Convention Center and onto the highway
- **Fix**: Tightened route to compact rectangle within building cluster corridors: Gate -> east corridor -> north -> west corridor -> south -> gate. Danger zones repositioned within buildings with smaller radii (10-15m)

**Error: Room migration failures after adding UML entities**
- **Problem**: Adding 5 new entities (UserEntity, HikeSessionEntity, LocationEntity, NotificationEntity, SafetyCheckInEntity) caused migration errors
- **Fix**: Used `fallbackToDestructiveMigration()` since this is a development build, bumped DB version to 11

---

## Session 13 (2026-03-25): Fall Detection Enhancement + Admin App

### What was built
- Enhanced fall detection with 3-phase gyroscope+accelerometer algorithm
- Created separate Trail Admin app via ContentProvider
- Both APKs installable on same phone

### Errors encountered and resolved

**Error: Gradle "Unsupported class file major version 69"**
- **Problem**: System JDK was Java 25 (class file version 69), but Gradle 8.x only supports up to JDK 21
- **Fix**: Set `JAVA_HOME` to Android Studio's bundled JBR (JDK 21): `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`

**Error: ContentProvider not accessible from admin app**
- **Problem**: Admin app couldn't query the ContentProvider - getting `SecurityException`
- **Fix**: Added `android:exported="true"` to the `<provider>` tag in main app's AndroidManifest.xml and proper authority matching

**Error: Fall detection too many false positives**
- **Problem**: Original accelerometer-only detection triggered from normal activities (sitting down, putting phone in pocket)
- **Fix**: Implemented 3-phase algorithm requiring: (1) Freefall < 3 m/s^2, (2) Impact > 25 m/s^2, (3) Gyro rotation > 5 rad/s - all within 2s window. Freefall must precede impact. 5s cooldown between alerts.

---

## Session 14 (2026-03-27): Map Fix + Directions + Fall Tuning

### What was built
- Fixed map centering (was showing PSG iTech for all trails)
- Added **Directions screen** - full-screen trail map with safe route, danger zones
- Split trail detail into two actions: **Directions** (view route) vs **Start Hike** (GPS tracking)
- ActiveHikeScreen now follows user's GPS location (`followMyLocation = true`)
- Adjusted fall detection sensitivity (slightly less sensitive to reduce false triggers)
- Generated 18-page UML Implementation Guide PDF

### Errors encountered and resolved

**Error: All trails showing PSG iTech Neelambur on map**
- **Problem**: When clicking "Start Hike" on any trail, the map always centered on PSG iTech campus (11.065, 77.093) instead of the actual trail location
- **Root cause**: `OsmMapView` had `enableFollowLocation()` and `runOnFirstFix` that snapped the map to the user's GPS position (which was at campus), overriding the trail coordinates passed to `centerLat`/`centerLng`
- **First fix (Session 14 early)**: Added `followMyLocation: Boolean = false` parameter to OsmMapView. Only enables follow + runOnFirstFix when explicitly set to true
- **Remaining issue**: Even with `followMyLocation = false`, the map was rendering with default coordinates (11.065, 77.093) while trail data was still loading from the Room database. The `key(trail?.id ?: "default")` should have forced recreation, but timing issues with AndroidView caused the initial Neelambur-centered map to persist.
- **Final fix**: Added early return with loading spinner when `trail == null`. Map only renders after trail data is loaded, guaranteeing correct coordinates.

**Error: Green safe route not showing on some trails**
- **Problem**: The safe route polyline wasn't rendering for trails during active hike
- **Root cause**: Same timing issue - trail coordinates were null during initial render
- **Fix**: Part of the same early-return fix above. Also simplified null-safe calls since trail is guaranteed non-null after the guard.

**Design change: Directions vs Start Hike**
- **User feedback**: "When I click start hike, show my current location. Keep directions separate"
- **Solution**: Created new `DirectionsScreen.kt` with its own `DirectionsViewModel` that loads trail data and danger zones. TrailDetailScreen now has two buttons side by side:
  - **Directions** (outlined button): Opens full-screen map centered on TRAIL with green safe route, danger zones, start/end markers, trail info bar, and a "Start Hike" button at bottom
  - **Start Hike** (filled button): Goes directly to ActiveHikeScreen with `followMyLocation = true`, map follows user's GPS, shows walked path in orange

**Fall detection sensitivity adjustment**
- **Problem**: Fall detection was too sensitive for demo - triggering from normal phone handling
- **Previous thresholds**: impact=15, freefall=5, rotation=2, window=3000ms (very easy to trigger)
- **New thresholds**: impact=18, freefall=4.5, rotation=2.5, window=2500ms (still demo-friendly but fewer false positives)
- **Original real-world thresholds** (for reference): impact=25, freefall=3, rotation=5, window=2000ms

### Files changed this session
- **New**: `DirectionsScreen.kt` (with DirectionsViewModel)
- **Modified**: `Navigation.kt` (added Directions route), `HikerApp.kt` (added Directions composable + onDirections callback), `TrailDetailScreen.kt` (two buttons: Directions + Start Hike), `ActiveHikeScreen.kt` (follows GPS, loading guard, removed trail centering), `OsmMapView.kt` (followMyLocation parameter), `FallDetectionService.kt` (adjusted thresholds)
- **Generated**: `UML_Implementation_Guide.pdf` (18 pages), `README.md`, `DEVELOPMENT_LOG.md`

---

## Summary of All Major Bugs & How They Were Resolved

| # | Bug | Root Cause | Fix |
|---|-----|-----------|-----|
| 1 | Crash on launch | Missing ACCESS_NETWORK_STATE permission | Added permission to manifest |
| 2 | osmdroid crash | Config not loaded before MapView | Moved to Application.onCreate() |
| 3 | Blank map tiles | Cleartext HTTP blocked on Android 9+ | usesCleartextTraffic=true |
| 4 | End Hike blank screen | Navigate screen not in back stack | Fallback popBackStack logic |
| 5 | Hilt test failures | No test runner for DI | Custom HiltTestRunner |
| 6 | Flow tests hanging | No timeout on Flow collection | Turbine library |
| 7 | Admin exit crash | popUpTo removed Login from stack | popUpTo(startDestination) |
| 8 | Schema migration fail | New entities without migration | fallbackToDestructiveMigration |
| 9 | Gradle JDK 25 error | Unsupported class file v69 | Use Android Studio JBR (JDK 21) |
| 10 | ContentProvider denied | Provider not exported | android:exported=true |
| 11 | Fall false positives | Accelerometer-only detection | 3-phase accel+gyro algorithm |
| 12 | Map shows wrong location | GPS follow overriding trail coords | followMyLocation parameter |
| 13 | Trail not loading on map | Null trail during initial render | Early return with loading guard |
| 14 | Straight line trail paths | Only start/end coordinates | 10-20 waypoints per trail |
| 15 | SOS notifications missing | No notification channel | Created channel with IMPORTANCE_HIGH |
| 16 | Campus trail off-route | Coordinates too spread out | Compact corridor route |

## Test Results

```
Unit Tests:     43/43 PASSED
Instrumented:   30/30 PASSED
Total:          73/73 PASSED
```
