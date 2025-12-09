package com.example.safelink.models

// Responses
data class AlertResponse(
    val page: Int,
    val limit: Int,
    val totalAlerts: Int,
    val totalPages: Int,
    val alerts: List<Alert>
)