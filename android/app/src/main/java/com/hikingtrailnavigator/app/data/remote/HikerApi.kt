package com.hikingtrailnavigator.app.data.remote

import retrofit2.Response
import retrofit2.http.*

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

data class SosRequest(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val trailId: String? = null,
    val trailName: String? = null
)

data class LocationUpdateRequest(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val speed: Float? = null
)

data class HazardReportRequest(
    val type: String,
    val severity: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)

interface HikerApi {
    @GET("trails")
    suspend fun getTrails(): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("trails/{id}")
    suspend fun getTrailById(@Path("id") id: String): Response<ApiResponse<Map<String, Any>>>

    @POST("safety/sos")
    suspend fun triggerSos(@Body request: SosRequest): Response<ApiResponse<Any>>

    @PUT("safety/sos/cancel")
    suspend fun cancelSos(@Body body: Map<String, String>): Response<ApiResponse<Any>>

    @POST("safety/silent-sos")
    suspend fun triggerSilentSos(@Body request: SosRequest): Response<ApiResponse<Any>>

    @POST("safety/location-update")
    suspend fun sendLocationUpdate(@Body request: LocationUpdateRequest): Response<ApiResponse<Any>>

    @POST("hazards")
    suspend fun reportHazard(@Body request: HazardReportRequest): Response<ApiResponse<Any>>

    @GET("hazards")
    suspend fun getHazards(): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("danger-zones")
    suspend fun getDangerZones(): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<Any>>
}
