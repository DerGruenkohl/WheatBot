package commands.overtake

import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class Overtake: ICommand {
    override val name: String
        get() = "overtake"
    override val description: String
        get() = "overtake prediction"
    override val subCommands: List<ISubCommand>
        get() = listOf(
            Collection(),
            Pests(),
            Skills(),
            Weight()
        )
    override val options: List<OptionData>
        get() = listOf()

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {

    }
}