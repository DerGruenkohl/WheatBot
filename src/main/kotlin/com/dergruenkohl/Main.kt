package com.dergruenkohl

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import com.dergruenkohl.config.Config
import com.dergruenkohl.config.Environment
import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import io.github.freya022.botcommands.api.components.Components.Companion.defaultPersistentTimeout
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {  }
private const val mainPackageName = "com.dergruenkohl"
const val botName = "CarryService"
lateinit var jda: JDA
fun main(args: Array<out String>) {
    try {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.reset()
        ContextInitializer(context).autoConfig()

        // stacktrace-decoroutinator has issues when reloading with hotswap agent
        if ("-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments) {
            logger.info { "Skipping stacktrace-decoroutinator as enhanced hotswap is active" }
        } else if ("--no-decoroutinator" in args) {
            logger.info { "Skipping stacktrace-decoroutinator as --no-decoroutinator is specified" }
        } else {
            DecoroutinatorJvmApi.install()
        }

        val config = Config.instance

        BotCommands.create {
            disableExceptionsInDMs = Environment.isDev
            addPredefinedOwners(*config.ownerIds.toLongArray())
            addSearchPath(mainPackageName)
            applicationCommands {
                @OptIn(DevConfig::class)
                disableAutocompleteCache = Environment.isDev
                fileCache {
                    @OptIn(DevConfig::class)
                    checkOnline = Environment.isDev
                }
                testGuildIds += config.testGuildIds
            }
            components {
                enable = true
                defaultPersistentTimeout = null
            }
        }
        logger.info { "Loaded bot" }
    } catch (e: Exception) {
        logger.error(e) { "Unable to start the bot" }
        exitProcess(1)
    }
}
