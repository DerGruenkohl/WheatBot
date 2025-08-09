package com.dergruenkohl.services.server.models

import kotlinx.serialization.Serializable

// UptimeHistoryResponse.kt
@Serializable
data class UptimeHistoryResponse(
    val uuid: String,
    val username: String,
    val uptimeHistory: Map<String, String>
)
// UptimeCompareEntry.kt
@Serializable
data class UptimeCompareEntry(
    val uuid: String,
    val username: String,
    val uptimeData: Map<String, Int>
)