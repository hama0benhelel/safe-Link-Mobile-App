package com.example.safelink.api

import com.example.safelink.models.AuthRequest
import com.example.safelink.models.AuthResponse
import com.example.safelink.models.SignupRequest
import com.example.safelink.models.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>
}