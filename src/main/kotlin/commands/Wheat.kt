package commands

import listeners.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class Wheat: ICommand {
    override val name = "wheat"
    override val options: List<OptionData> = ArrayList()
    override val description: String
        get() = "get a wheat"

    override fun execute(event: SlashCommandInteractionEvent) {

        val wheat = "https://hmbldtw.spdns.org/~pkruenelke/web/wheat_basic.webp"
        val builder = EmbedBuilder()
        builder.setTitle("Wheat")
        builder.setImage(wheat)
        event.replyEmbeds(builder.build()).queue()
    }
}