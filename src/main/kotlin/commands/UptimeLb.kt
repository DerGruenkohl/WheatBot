package commands

import listeners.ICommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import share.Leaderboard


class UptimeLb: ICommand {
    override val name: String
        get() = "uptimelb"
    override val description: String
        get() = "gets the uptime leaderboard"
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.INTEGER, "startpos", "the start position of the leaderboard"),
            OptionData(OptionType.STRING, "username", "gets the leaderboard position for a specific user")
            )

    override fun execute(event: SlashCommandInteractionEvent) {
        val hook = event.reply("Preparing the leaderboard <:wheat_pray:1224363201026850917>").complete()
        try {
            val lb = Leaderboard()
            var startIndex = if (event.getOption("startpos") != null){
                event.getOption("startpos")!!.asInt-1
            }
            else 0

            event.getOption("username")?.let { mapping ->
                startIndex = lb.getStartIndex(mapping.asString)
            }

            val msg = lb.createUptimeLB(startIndex)
            hook.editOriginal(MessageEditData.fromCreateData(msg)).queue()
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something went wrong").queue()
        }
    }

}