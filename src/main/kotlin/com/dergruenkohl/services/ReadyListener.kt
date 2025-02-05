package com.dergruenkohl.services

import com.dergruenkohl.jda
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.session.ReadyEvent

// You can optionally have a name to differentiate between multiple instance of your services
@BService(name = "ReadyListner")
class ReadyListener {
    private val logger = KotlinLogging.logger { }

    @BEventListener(priority = 1)
    fun onReadyFirst(event: ReadyEvent) {
        logger.info { "First handling of ReadyEvent" }
        jda = event.jda

        //Print some information about the bot
        logger.info { "Bot connected as ${jda.selfUser.name}" }
        logger.info { "The bot is present on these guilds :" }
        for (guild in jda.guildCache) {
            logger.info { "\t- ${guild.name} (${guild.id})" }
        }
    }

    // Executes after the above listener, but doesn't prevent the listener below from running
    @BEventListener(priority = 0, mode = BEventListener.RunMode.ASYNC)
    suspend fun onReadyAsync(event: ReadyEvent) {
        logger.info { "(Before) Async handling of ReadyEvent" }
        logger.info { "(After) Async handling of ReadyEvent" }
    }

    // Executes after the above listener
    @BEventListener(priority = -1)
    fun onReadyLast(event: ReadyEvent) {
        logger.info { "Last handling of ReadyEvent" }
    }
}