package com.dergruenkohl.commands

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getGuildById
import com.dergruenkohl.hypixel.client.getGuildByName
import com.dergruenkohl.utils.database.GuildRepo.save
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Command
class AddGuild: ApplicationCommand() {
    @JDASlashCommand(name = "addguild", description = "Add a guild to the database")
    suspend fun addGuild(event: GlobalSlashEvent, @SlashOption(description = "Guild name") guild: String?, @SlashOption(description = "Guild ID") guildId: String?) {
        try {
            if (guild == null && guildId == null) {
                event.reply("Please provide a guild name or ID").setEphemeral(true).queue()
                return
            }
            event.reply("Adding guild...").setEphemeral(true).queue()
            guild?.let {
                if (it.length < 3) {
                    event.reply("Guild name must be at least 3 characters long").setEphemeral(true).queue()
                    return
                }
                val guildreply = hypixelClient.getGuildByName(guild).guild
                if (guildreply == null) {
                    event.hook.editOriginal("Guild not found").queue()
                    return
                }
                guildreply.save()
                event.hook.editOriginal("Guild ${guildreply.name} added with ${guildreply.members.size} members").queue()
            }
            guildId?.let {
                if (it.length < 3) {
                    event.reply("Guild ID must be at least 3 characters long").setEphemeral(true).queue()
                    return
                }
                val guildreply = hypixelClient.getGuildById(guildId).guild
                if (guildreply == null) {
                    event.hook.editOriginal("Guild not found").queue()
                    return
                }
                guildreply.save()
                event.hook.editOriginal("Guild ${guildreply.name} added with ${guildreply.members.size} members").queue()
            }
        }catch (ex: Exception) {
            event.reply("Error: ${ex.message}").setEphemeral(true).queue()
        }
    }
}