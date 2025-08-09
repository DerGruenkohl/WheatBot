package com.dergruenkohl.services.server.models

import kotlinx.serialization.Serializable

@Serializable
data class UptimeLeaderboardResponse(
    val page: Int,
    val totalPages: Int,
    val entries: List<LeaderboardEntry>
)