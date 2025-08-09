package com.dergruenkohl.services.server.models

import kotlinx.serialization.Serializable

@Serializable
data class WeightData(
    val current: Double,
    val history: Map<String, Double>
)