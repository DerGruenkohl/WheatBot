package commands.gain

import listeners.Command
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData

@Command(
    name = "gain",
    description = "get the gain of a specific user",
    subCommands = [
        CollectionGain::class,
        SkillGain::class,
        PestGain::class,
        WeightGain::class
    ]
)
class Gain