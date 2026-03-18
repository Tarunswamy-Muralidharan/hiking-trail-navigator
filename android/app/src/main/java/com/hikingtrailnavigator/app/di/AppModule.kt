package com.hikingtrailnavigator.app.di

import android.content.Context
import androidx.room.Room
import com.hikingtrailnavigator.app.data.local.HikerDatabase
import com.hikingtrailnavigator.app.data.local.dao.*
import com.hikingtrailnavigator.app.data.remote.HikerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HikerDatabase {
        return Room.databaseBuilder(
            context,
            HikerDatabase::class.java,
            "hiker_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideTrailDao(db: HikerDatabase): TrailDao = db.trailDao()
    @Provides fun provideDangerZoneDao(db: HikerDatabase): DangerZoneDao = db.dangerZoneDao()
    @Provides fun provideNoCoverageZoneDao(db: HikerDatabase): NoCoverageZoneDao = db.noCoverageZoneDao()
    @Provides fun provideHazardReportDao(db: HikerDatabase): HazardReportDao = db.hazardReportDao()
    @Provides fun provideHikeActivityDao(db: HikerDatabase): HikeActivityDao = db.hikeActivityDao()
    @Provides fun provideEmergencyContactDao(db: HikerDatabase): EmergencyContactDao = db.emergencyContactDao()
    @Provides fun provideActiveHikerDao(db: HikerDatabase): ActiveHikerDao = db.activeHikerDao()
    @Provides fun provideRouteWarningDao(db: HikerDatabase): RouteWarningDao = db.routeWarningDao()
    @Provides fun provideSosAlertDao(db: HikerDatabase): SosAlertDao = db.sosAlertDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.127:3000/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideHikerApi(retrofit: Retrofit): HikerApi {
        return retrofit.create(HikerApi::class.java)
    }
}
