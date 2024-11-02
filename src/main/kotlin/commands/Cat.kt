package commands

import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import utils.getMeow

class Cat: ICommand {
    override val name = "cat"
    override val options: List<OptionData> = ArrayList()
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val description: String
        get() = "get a cat"

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {

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