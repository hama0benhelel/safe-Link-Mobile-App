package com.example.safelink.models

data class SignupResponse(
    val token: String,
    val user: User,
    val message: String? = null
)