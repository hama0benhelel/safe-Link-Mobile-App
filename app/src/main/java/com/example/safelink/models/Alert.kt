package com.example.safelink.models

data class Alert(
    val _id: String,
    val src_ip: String,
    val attack_type: String,
    val severity: String,
    val status: String,
    val confidence: Double? = null,
    val timeAgo: String? = null,
    val timestamp: String
)
