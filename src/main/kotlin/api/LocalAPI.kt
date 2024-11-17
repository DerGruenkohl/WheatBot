package api

import apiUrl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import key
import kotlinx.serialization.json.Json
import token

class LocalAPI {
    val client by lazy {
        HttpClient(CIO) {
            defaultRequest {
                url(apiUrl)
            }
            expectSuccess = false
            install(Logging)
            install(HttpRequestRetry){
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

