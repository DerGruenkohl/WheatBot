package com.dergruenkohl.commands.links

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getPlayer
import com.dergruenkohl.hypixel.client.getSelectedProfileID
import com.dergruenkohl.utils.database.Link
import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.database.Settings
import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.oshai.kotlinlogging.KotlinLogging

@Command
object LinkCommand: ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }

    @TopLevelSlashCommandData(description = "Manage Links")
    @JDASlashCommand(name = "link", description = "link your account", subcommand = "create")
    suspend fun onCreateLink(event: GlobalSlashEvent, @SlashOption(name = "ign", description = "Your Minecraft IGN") ign: String) {
        val hook = event.deferReply(true).await()
        try {
            val uuid = getMinecraftUUID(ign)
            val profileID = hypixelClient.getSelectedProfileID(uuid) ?: return hook.editOriginal("Invalid Profile").queue()
            LinkRepo.getLink(event.user.idLong)?.let {
                return hook.editOriginal("You are already linked to ${getMinecraftUsername(it.uuid)}, use /link update").queue()
            }
            val discord = hypixelClient.getPlayer(uuid).player.socialMedia?.links?.discord?: return hook.editOriginal("")
                .setEmbeds(
                    Embed {
                        title = "Invalid Link"
                        description = "You need to link your discord account on hypixel"
                        image = "https://cdn.discordapp.com/attachments/922202066653417512/1066476136953036800/tutorial.gif"
                    }
                )
                .queue()
            if (discord != event.user.name){
                return hook.editOriginal("")
                    .setEmbeds(
                        Embed {
                            title = "Your discord name does not match your linked discord name"
                            description = "Your linked discord name is $discord and it must be ${event.user.name}, wait a few minutes after changing for it to update"
                            image = "https://cdn.discordapp.com/attachments/922202066653417512/1066476136953036800/tutorial.gif"
                        }
                    )
                    .queue()
            }
            logger.info { "Creating link for ${event.user.name}" }

            val link = Link(
                uuid = uuid,
                discordId = event.user.idLong,
                discordName = event.user.name,
                settings = Settings(
                    uuid = uuid,
                    profileID = profileID
                )
            )
            LinkRepo.createOrUpdateLink(link)
            hook.editOriginal("Successfully created link")
                .setEmbeds(link.toEmbed())
                .queue()

        }catch (e: Exception){
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }

    @JDASlashCommand(name = "link", description = "Get your link", subcommand = "get")
    suspend fun onGetLink(event: GlobalSlashEvent){
        val hook = event.deferReply(true).await()
        try {
            val link = LinkRepo.getLink(event.user.idLong)?: return hook.editOriginal("You are not linked").queue()
            hook.editOriginal("")
                .setEmbeds(link.toEmbed())
                .queue()
        }catch (e: Exception){
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }
    @JDASlashCommand(name = "link", description = "Delete your link", subcommand = "delete")
    suspend fun onDeleteLink(event: GlobalSlashEvent){
        val hook = event.deferReply(true).await()
        try {
            val link = LinkRepo.getLink(event.user.idLong)?: return hook.editOriginal("You are not linked").queue()
            LinkRepo.deleteLink(event.user.idLong)
            hook.editOriginal("Successfully deleted link").queue()
        }catch (e: Exception){
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }

    @JDASlashCommand(name = "link", description = "Update your link", subcommand = "update")
    suspend fun onUpdateLink(event: GlobalSlashEvent, @SlashOption("ign", "new ingame name") ign: String){
        val hook = event.deferReply(true).await()
        try {
            val uuid = getMinecraftUUID(ign)
            val profileID = hypixelClient.getSelectedProfileID(uuid) ?: return hook.editOriginal("Invalid Profile").queue()
            val link = LinkRepo.getLink(event.user.idLong)?: return hook.editOriginal("You are not linked").queue()

            val discord = hypixelClient.getPlayer(uuid).player.socialMedia?.links?.discord?: return hook.editOriginal("")
                .setEmbeds(
                    Embed {
                        title = "Invalid Link"
                        description = "You need to link your discord account on hypixel"
                        image = "https://cdn.discordapp.com/attachments/922202066653417512/1066476136953036800/tutorial.gif"
                    }
                )
                .queue()
            if (discord != event.user.name){
                return hook.editOriginal("")
                    .setEmbeds(
                        Embed {
                            title = "Your discord name does not match your linked discord name"
                            description = "Your linked discord name is $discord and it must be ${event.user.name}, wait a few minutes after changing for it to update"
                            image = "https://cdn.discordapp.com/attachments/922202066653417512/1066476136953036800/tutorial.gif"
                        }
                    )
                    .queue()
            }

            val newSettings = link.settings.copy(
                uuid = uuid,
                profileID = profileID
            )
            val newLink = link.copy(
                uuid = uuid,
                settings = newSettings,
                discordName = event.user.name
            )
            LinkRepo.createOrUpdateLink(newLink)
            hook.editOriginal("Successfully updated link")
                .setEmbeds(newLink.toEmbed())
                .queue()

        }catch (e: Exception){
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }
}