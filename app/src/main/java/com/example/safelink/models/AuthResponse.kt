package com.example.safelink.models

data class AuthResponse(
    val token: String,
    val user: User,
    val message: String? = null
)