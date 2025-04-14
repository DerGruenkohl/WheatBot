package com.dergruenkohl

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import com.dergruenkohl.config.Config
import com.dergruenkohl.config.Data
import com.dergruenkohl.config.Environment
import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.components.Components.Companion.defaultPersistentTimeout
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess


object WheatBot: CoroutineScope {
    private val logger = KotlinLogging.logger {  }
    private const val mainPackageName = "com.dergruenkohl"
    const val botName = "CarryService"
    lateinit var jda: JDA

    /**
     * Default scope for the bot, used for all tasks that are not IO or auto
     */
    override val coroutineContext = namedDefaultScope(
        name = "WheatBot",
        corePoolSize = 8
    ).coroutineContext
    /**
     * Scope for IO tasks, like database access and network requests
     */
    val IO = object : CoroutineScope {
        override val coroutineContext = namedDefaultScope("WheatBot IO", 16).coroutineContext
    }
    /**
     * Scope for auto tasks, like the ones that run every 5 minutes
    */
    val AUTO = object : CoroutineScope {
        override val coroutineContext = namedDefaultScope("WheatBot AUTO", 4).coroutineContext
    }
    val IOContext = IO.coroutineContext

    @JvmStatic
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
                GlobalApplicationCommandManager.Defaults.contexts = setOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)
                GlobalApplicationCommandManager.Defaults.integrationTypes = setOf(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL)
                disableExceptionsInDMs = Environment.isDev
                addPredefinedOwners(*config.ownerIds.toLongArray())
                addSearchPath(mainPackageName)
                applicationCommands {
                    @OptIn(DevConfig::class)
                    disableAutocompleteCache = Environment.isDev
                    fileCache(Data.folder) {
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
}
