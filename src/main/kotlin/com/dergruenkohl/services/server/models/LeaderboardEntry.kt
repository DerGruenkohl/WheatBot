package com.dergruenkohl.services.server.models

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val uuid: String,
    val username: String,
    val uptime: String
)