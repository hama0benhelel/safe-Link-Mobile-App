package com.example.safelink.models

data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val details: List<String>? = null
)