package com.dergruenkohl.commands

import com.dergruenkohl.utils.getMeow
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand

@Command
object Silly: ApplicationCommand() {
    @JDASlashCommand("cat", description = "Get a random cat picture")
    fun onCat(event: GlobalSlashEvent) {
        event.reply(getMeow()).queue()
    }
    @JDASlashCommand("wheat", description = "graan")
    fun onWheat(event: GlobalSlashEvent) {
        event.replyEmbeds(
            Embed {
                title = "Wheat"
                image = "https://static.wikia.nocookie.net/minecraft_gamepedia/images/7/75/Wheat_JE2_BE2.png"
            }
        ).queue()
    }
}