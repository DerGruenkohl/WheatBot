package com.dergruenkohl.services

import com.dergruenkohl.jda
import com.dergruenkohl.utils.database.LinkEntity
import com.dergruenkohl.utils.database.LinkTable
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.retrieveUserByIdOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.seconds

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
            logger.info { "\t- ${guild.name} (${guild.id}) members: ${guild.loadMembers().get().size}" }
        }
    }

    // Executes after the above listener, but doesn't prevent the listener below from running
    @BEventListener(priority = 0, mode = BEventListener.RunMode.ASYNC)
    suspend fun onReadyAsync(event: ReadyEvent) {
        logger.info { "(Before) Async handling of ReadyEvent" }
        delay(10.seconds)
        transaction {
            runBlocking {
                LinkEntity.find { LinkTable.discordName.isNull() }.forEach {
                    logger.info { "No user found for ${it.discordId}" }
                    val user = jda.retrieveUserByIdOrNull(it.discordId, false)?: return@forEach logger.warn { "User not found" }
                    logger.info { "Found user ${user.name}" }
                    it.discordName = user.name
                }
            }

        }

        logger.info { "(After) Async handling of ReadyEvent" }
    }

    // Executes after the above listener
    @BEventListener(priority = -1)
    fun onReadyLast(event: ReadyEvent) {
        logger.info { "Last handling of ReadyEvent" }
    }
}