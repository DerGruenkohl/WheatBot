package commands

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import share.ContestHandler
import share.Link
import share.getContest
import utils.getMinecraftUUID
import utils.getMinecraftUsername
import java.awt.Color
import java.io.IOException
import kotlin.math.max

class Contests: ICommand {
    override val name: String
        get() = "contests"
    override val description: String
        get() = "gets a contest graph"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "name", "ign of the user")
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        runBlocking {
            val hook = event.deferReply(ephemeral).complete()
            val option = event.getOption("name")
            var ign: String? = null
            val client = LocalAPI().client
            val response = client.request("link/get/${event.user.id}")
            if(option != null) {
                ign = option.asString
            }
            if(ign == null) {

                if (response.status.value >= 300){
                    hook.editOriginal("No Ign applied and no account linked!").queue()
                    return@runBlocking
                }
                try {
                    ign = getMinecraftUsername(response.body<Link>().uuid)
                }catch (e: IOException){
                    hook.editOriginal("No Ign applied and no account linked!").queue()
                    return@runBlocking
                }

            }
            val contests = getContest(getMinecraftUUID(ign))
            val manager = ContestHandler(contests)

            val embed = EmbedBuilder()
            embed.setTitle("Contest Activity")
            if (contests.isEmpty()){
                embed.setColor(Color.RED)
                embed.setDescription("No contests found for $ign")
                hook.editOriginal("").setEmbeds(embed.build()).queue()
                return@runBlocking
            }
            val builder = StringBuilder()
            val contestMap = manager.getContest().toMap().toSortedMap()
            builder.append("Showing hourly contests for **${ign}**\nGraph is on a 24 hour period based on time of contest.\nShowing over time period: **All Time**\n\n")

            contestMap.forEach {(hour, count)->
                if (contestMap.values.max() < 10){
                    builder.append("[${"█".repeat(count)}] (**$count**) \n")
                }
                else{
                    val scale = (contestMap.values.maxOrNull() ?: 10) / 10
                    builder.append("[${"█".repeat(count/scale)}] (**$count**) \n")
                }
            }


            embed.setDescription(builder.toString())

            hook.editOriginal("").setEmbeds(embed.build()).queue()

        }
    }
}