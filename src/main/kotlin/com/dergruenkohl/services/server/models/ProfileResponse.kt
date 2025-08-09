package com.dergruenkohl.services.server.models

import com.dergruenkohl.utils.hypixelutils.Time
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val uuid: String,
    val username: String,
    val weight: WeightData?,
    val skills: Map<String, Map<String, Double>>?,
    val farmingCollections: Map<String, Map<String, Long>>?,
    val miningCollections: Map<String, Map<String, Long>>?,
    val pestDrops: Map<String, Map<String, Double>>?,
    val uptimeData: Map<String, String>?,
    val uptimeHistory: Map<String, String>?
)