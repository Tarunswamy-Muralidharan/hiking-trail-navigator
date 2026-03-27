# Hiking Trail Navigator

**Integrated Trail Discovery and Safety Platform**

A native Android application built with Kotlin and Jetpack Compose that provides comprehensive hiking trail navigation, real-time safety monitoring, SOS emergency alerts, fall detection, and an admin system for forest officers.

## Authors

- **Tarunswamy M** - Development & Architecture
- **Poornesh P** - Initial Code & Requirements

**PSG Institute of Technology, Coimbatore** | OOSE Lab - January 2026

## Features

### Trail Discovery & Navigation
- 9 real hiking trails around Coimbatore / Western Ghats + 1 PSG iTech campus demo trail
- Satellite map view with ESRI World Imagery tiles (osmdroid)
- **Directions mode**: View trail route with green safe path, danger zones, start/end markers - centered on trail location
- **Start Hike mode**: GPS-following live tracking with real-time stats, walked path in orange
- Trail search with location suggestions, difficulty filtering
- Elevation profiles, risk assessment scores, schedule info

### Safety System
- **SOS Emergency**: One-tap SOS sends SMS to contacts + Forest Dept (1800-425-1600) + SDMA (1070) - works offline
- **Silent SOS**: Discreet emergency alert without vibration
- **Fall Detection**: 3-phase algorithm (accelerometer + gyroscope): Freefall -> Impact -> Rotation within time window
- **Safety Check-in**: Periodic check-ins with 3-tier escalation (prompt -> warning -> auto-SOS)
- **Trail Deviation Alert**: Warning when >100m off trail (Haversine distance calculation)
- **Danger Zone Alerts**: Proactive alerts when entering wildlife corridors, landslide areas, etc.
- **No-Coverage Zone Warnings**: Alerts when entering areas without cellular network

### Admin System
- Separate **Trail Admin** app communicates with main app via ContentProvider
- Admin dashboard: Active hikers, SOS alerts (with badge count), missed check-ins
- Trail CRUD: Add new trails, delete existing trails
- SOS alert resolution workflow
- Push notifications for SOS events

### Activity Tracking
- Distance, duration, elevation gain, average pace
- Activity history with summary cards
- Route recording during hikes

## Architecture

```
Clean Architecture + MVVM

Presentation Layer     Jetpack Compose UI + ViewModels (Hilt DI)
Domain Layer           11 UML domain model classes
Data Layer             Room DB (v11, 14 entities, 14 DAOs) + Repository pattern
Service Layer          9 Android services (GPS, Fall Detection, SOS, Weather, etc.)
```

## UML Diagrams Implemented (OOSE Exp 2-6)

| Experiment | Diagram | Implementation |
|------------|---------|---------------|
| Exp 2 | Use Case Diagram | App screens, navigation, 3 actor roles |
| Exp 3 | Class Diagram | 11 domain classes with inheritance (User -> Hiker/ForestOfficer/Admin) |
| Exp 4 | Sequence Diagram | 9-step SOS flow in SOSViewModel |
| Exp 4 | Collaboration Diagram | Object interactions across ViewModels |
| Exp 5 | Activity Diagram | Navigation graph swimlanes (Hiker/Admin/System) |
| Exp 5 | State Chart Diagram | HikeSession lifecycle (NotStarted -> Active -> Paused -> Completed -> Emergency) |

See `UML_Implementation_Guide.pdf` for the full 18-page mapping document.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9+ |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Database | Room (SQLite) - Version 11 |
| Maps | osmdroid + ESRI Satellite tiles |
| Location | FusedLocationProviderClient |
| Sensors | Accelerometer + Gyroscope |
| Navigation | Jetpack Navigation Compose |
| Networking | Retrofit + OkHttp (offline-first) |
| Testing | 73 tests (43 unit + 30 instrumented) |

## Build

```bash
# Set JDK (Android Studio JBR / JDK 21 required)
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

# Build main app
cd android
./gradlew :app:assembleDebug

# Build admin app
./gradlew :adminapp:assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (device required)
./gradlew connectedDebugAndroidTest

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r adminapp/build/outputs/apk/debug/adminapp-debug.apk
```

## Project Structure

```
hiker-app/
  android/
    app/src/main/java/com/hikingtrailnavigator/app/
      data/
        local/          Room DB, entities, DAOs, SeedData
        provider/       ContentProvider (admin app IPC)
        remote/         Retrofit API
        repository/     6 repositories
      di/               Hilt AppModule
      domain/model/     11 UML domain classes
      service/          9 Android services
      ui/
        components/     OsmMapView, CommonComponents
        navigation/     Screen routes, BottomNavItem
        screens/        home, trails, navigate, safety, admin, profile, login
        theme/          Material 3 theme
    adminapp/           Separate Trail Admin app module
  UML_Implementation_Guide.pdf
  Testing_Documentation.pdf
  Software Requirements Specification(updated).docx
```

## Screenshots

Login | Home | Trail List | Trail Detail
:---:|:---:|:---:|:---:
![Login](screen_login.png) | ![Home](screen1.png) | ![Trails](screen3.png) | ![Detail](screen5.png)

Directions | Active Hike | SOS | Admin
:---:|:---:|:---:|:---:
![Directions](screen7.png) | ![Hike](screen8a.png) | ![SOS](screen9.png) | ![Admin](screen12.png)

## License

Academic project - PSG iTech, Coimbatore
