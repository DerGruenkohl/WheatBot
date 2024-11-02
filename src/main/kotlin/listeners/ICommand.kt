package listeners

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

interface ICommand {
    val name: String
    val description: String
    val options: List<OptionData>
    val subCommands: List<ISubCommand>

    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean)
}
interface ISubCommand {
    val name: String
    val description: String
    val options: List<OptionData>
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean)

}