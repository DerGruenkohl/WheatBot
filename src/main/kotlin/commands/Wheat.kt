package commands

import listeners.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Command(
    name = "wheat",
    description = "get a wheat"
)
class Wheat {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val wheat = "https://static.wikia.nocookie.net/minecraft_gamepedia/images/7/75/Wheat_JE2_BE2.png"
        val builder = EmbedBuilder()
        builder.setTitle("Wheat")
        builder.setImage(wheat)
        event.replyEmbeds(builder.build())
            .setEphemeral(ephemeral)
            .queue()
    }
}