package com.dergruenkohl.commands

import com.dergruenkohl.listeners.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import com.dergruenkohl.utils.getMeow

@Command(
    name = "cat",
    description = "get a cat"
)
class Cat {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        var wheat = getMeow()
        if(wheat == "-1"){wheat = "https://cdn2.thecatapi.com/images/QUdOiX2hP.jpg"}
        val builder = EmbedBuilder()
        builder.setTitle("Meow!")
        builder.setImage(wheat)
        event.replyEmbeds(builder.build())
            .setEphemeral(ephemeral)
            .queue()
    }
}