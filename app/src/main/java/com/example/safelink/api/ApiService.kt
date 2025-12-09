package com.example.safelink.api


import com.example.safelink.models.Alert
import com.example.safelink.models.AlertResponse
import com.example.safelink.models.AuthRequest
import com.example.safelink.models.AuthResponse
import com.example.safelink.models.SignupRequest
import com.example.safelink.models.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ApiService.kt (version corrigée)
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @GET("/api/alerts")
    suspend fun getAlerts(
//        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null
    ): Response<AlertResponse>  // ← AlertResponse, pas List<Alert>

    @GET("/alerts/{id}")
    suspend fun getAlertById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Alert>

    @DELETE("/alerts/{id}")
    suspend fun deleteAlert(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Map<String, String>>


}