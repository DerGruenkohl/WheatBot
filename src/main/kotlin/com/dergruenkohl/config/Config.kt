package com.dergruenkohl.config


import com.dergruenkohl.hypixel.client.HypixelClient
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

@Serializable
data class DatabaseConfig(
    val serverName: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String
) {
    val mysqlUrl: String
        get() = "jdbc:mariadb://$serverName:$port/$name"
    val h2Url: String
        get() = "jdbc:h2:file:${Data.folder.absolutePathString()}/$name;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
}

@Serializable
data class Config(
    val token: String,
    val botId: Long,
    val ownerIds: List<Long>,
    val testGuildIds: List<Long>,
    val databaseConfig: DatabaseConfig,
    val mainDatabase: DatabaseConfig,
    val hypixelAPI: String,
    val kohlAPI: String,
) {
    // Wiki configs
    @get:JvmName("areTagsEnabled")
    val enableTags: Boolean get() = false

    @get:JvmName("isDevModeEnabled")
    val enableDevMode: Boolean get() = false

    companion object {
        private val logger = KotlinLogging.logger { }


        private val configFilePath: Path = Environment.configFolder.resolve("config.json")
        private val json = Json{
            ignoreUnknownKeys = true
        }
        @get:BService
        val instance: Config by lazy {
            logger.info { "Loading configuration at ${configFilePath.absolutePathString()}" }

            return@lazy json.decodeFromString<Config>(configFilePath.readText())
        }
    }
}