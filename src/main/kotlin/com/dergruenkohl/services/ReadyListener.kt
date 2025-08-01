package com.dergruenkohl.services

import com.dergruenkohl.WheatBot
import com.dergruenkohl.utils.database.LinkEntity
import com.dergruenkohl.utils.database.LinkTable
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.retrieveUserByIdOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.session.ReadyEvent

// You can optionally have a name to differentiate between multiple instance of your services
@BService(name = "ReadyListner")
class ReadyListener {
    private val logger = KotlinLogging.logger { }

    @BEventListener(priority = 1)
    fun onReadyFirst(event: ReadyEvent) {
        logger.info { "First handling of ReadyEvent" }
        WheatBot.jda = event.jda

        //Print some information about the bot
        logger.info { "Bot connected as ${WheatBot.jda.selfUser.name}" }
        logger.info { "The bot is present on these guilds :" }
        for (guild in WheatBot.jda.guildCache) {
            logger.info { "\t- ${guild.name} (${guild.id})" }
        }
    }

    // Executes after the above listener
    @BEventListener(priority = -1)
    fun onReadyLast(event: ReadyEvent) {
        logger.info { "Last handling of ReadyEvent" }
    }
}