package com.dergruenkohl.commands

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getGuildByName
import com.dergruenkohl.utils.database.GuildRepo.save
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption

@Command
class AddGuild: ApplicationCommand() {
    @JDASlashCommand(name = "addguild", description = "Add a guild to the database")
    suspend fun addGuild(event: GlobalSlashEvent, @SlashOption(description = "Guild name") guild: String) {
        try {
            event.reply("Adding guild...").setEphemeral(true).queue()
            val guildreply = hypixelClient.getGuildByName(guild).guild
            if (guildreply == null) {
                event.hook.editOriginal("Guild not found").queue()
                return
            }
            guildreply.save()
            event.hook.editOriginal("Guild ${guildreply.name} added with ${guildreply.members.size} members").queue()
        }catch (ex: Exception) {
            event.reply("Error: ${ex.message}").setEphemeral(true).queue()
        }
    }
}