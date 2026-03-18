package com.hikingtrailnavigator.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hikingtrailnavigator.app.data.local.dao.*
import com.hikingtrailnavigator.app.data.local.entity.*

@Database(
    entities = [
        TrailEntity::class,
        DangerZoneEntity::class,
        NoCoverageZoneEntity::class,
        HazardReportEntity::class,
        HikeActivityEntity::class,
        EmergencyContactEntity::class,
        ActiveHikerSessionEntity::class,
        RouteWarningEntity::class,
        SosAlertEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HikerDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
    abstract fun dangerZoneDao(): DangerZoneDao
    abstract fun noCoverageZoneDao(): NoCoverageZoneDao
    abstract fun hazardReportDao(): HazardReportDao
    abstract fun hikeActivityDao(): HikeActivityDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun activeHikerDao(): ActiveHikerDao
    abstract fun routeWarningDao(): RouteWarningDao
    abstract fun sosAlertDao(): SosAlertDao
}
