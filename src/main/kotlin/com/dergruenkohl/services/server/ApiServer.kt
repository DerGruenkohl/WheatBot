package com.dergruenkohl.services.server

import com.dergruenkohl.config.Config
import com.dergruenkohl.services.server.lb.lbRoutes
import com.dergruenkohl.services.server.user.userRoutes
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

@BService
class ApiServer(val config: Config) {
    init {
        embeddedServer(Netty, config.serverPort){
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            routing {
                route("/v1"){
                    userRoutes()
                    route("/lb"){
                        lbRoutes()
                    }

                }
                install(CORS) {
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Delete)
                    allowMethod(HttpMethod.Patch)
                    allowHeader(HttpHeaders.Authorization)
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader("*")

                    // Allow your frontend origin
                    allowHost("dergruenkohl.com", schemes = listOf("http", "https"))
                    allowHost("localhost:5173", schemes = listOf("http", "https"))
                    allowHost("127.0.0.1:5173", schemes = listOf("http", "https"))
                }
            }
        }.start(wait = false)
    }
}