package commands

import listeners.Command
import listeners.Option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.messages.MessageEditData
import share.Leaderboard

@Command(
    name = "uptimelb",
    description = "gets the uptime leaderboard",
    options = [
        Option(
            name = "startpos",
            description = "the start position of the leaderboard",
            type = OptionType.INTEGER
        ),
        Option(
            name = "username",
            description = "gets the leaderboard position for a specific user",
            type = OptionType.STRING
        )
    ]
)
class UptimeLb {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.reply("Preparing the leaderboard <:wheat_pray:1224363201026850917>")
            .setEphemeral(ephemeral)
            .complete()
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