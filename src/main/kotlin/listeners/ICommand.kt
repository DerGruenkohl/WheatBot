package listeners

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KClass

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

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String,
    val description: String,
    val options: Array<Option> = [],
    val subCommands: Array<KClass<out Any>> = []
)

annotation class SubCommand(
    val name: String,
    val description: String,
    val options: Array<Option> = []
)

annotation class Option(
    val name: String,
    val type: OptionType,
    val description: String,
    val choices: Array<Choice> = [],
    val required: Boolean = false
)
annotation class Choice(
    val name: String,
    val value: String
)