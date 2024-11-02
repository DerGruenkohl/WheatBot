package commands.gain

import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class Gain: ICommand {
    override val name: String
        get() = "gain"
    override val description: String
        get() = "gain prediction"
    override val subCommands: List<ISubCommand>
        get() = listOf(
            CollectionGain(),
            PestGain(),
            SkillGain(),
            WeightGain()
        )
    override val options: List<OptionData>
        get() = listOf()

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {

    }
}