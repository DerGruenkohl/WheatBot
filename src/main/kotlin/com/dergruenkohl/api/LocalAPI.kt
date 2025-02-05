package com.dergruenkohl.api

import com.dergruenkohl.ConfigObj.apiUrl
import com.dergruenkohl.ConfigObj.key
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

private class LocalAPI {
    val client by lazy {
        HttpClient(CIO) {
            defaultRequest {
                url(apiUrl)
            }
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
            install(Auth) {
                bearer {
                    loadTokens {
                        // Load tokens from a local storage and return them as the 'BearerTokens' instance
                        BearerTokens(key, "")
                    }
                }
            }
        }
    }
}

object ApiInstance {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var _client: HttpClient? = null
    private val LOGGER = KtorSimpleLogger("LocalAPIInstance")

    val client: HttpClient
        get() {
            if (_client == null) {
                LOGGER.info("Creating new client")
                _client = LocalAPI().client
            }
            return _client!!
        }

    fun close() {
        LOGGER.info("Closing client")
        _client?.close()
        job.cancel()
    }
}