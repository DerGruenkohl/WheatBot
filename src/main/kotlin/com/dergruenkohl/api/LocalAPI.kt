package com.dergruenkohl.api

import com.dergruenkohl.config.Config
import com.dergruenkohl.hypixel.client.HypixelClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


val client by lazy {
    HttpClient(CIO) {
        expectSuccess = true
        install(Logging)
        install(HttpRequestRetry) {
            retryOnServerErrors(3)
            retryOnException(3, true)
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
}
val hypixelClient by lazy {
    HypixelClient(Config.instance.hypixelAPI)
}

