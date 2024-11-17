package commands

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.LineType
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import share.HistoricalUptime
import share.Link
import share.Member
import share.Types
import utils.getMinecraftUsername

class UptimeHistory: ICommand {
    override val name = "uptimehistoy"
    override val description = "Gets the historical farming uptime of someone (data from before 3.11.24 may be inaccurate)"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() = listOf(

            OptionData(OptionType.STRING, "type", "the type", true).addChoices(
                Command.Choice("total","total"),
                Command.Choice("30d","thirty"),
                Command.Choice("7d","seven"),
                Command.Choice("weeks","weeks"),
            ),
            OptionData(OptionType.STRING, "ign", "The ign"),
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val option = event.getOption("ign")
        var ign: String? = null
        val hook = event.deferReply()
            .setEphemeral(ephemeral)
            .complete()

        try {
            val api = LocalAPI()
            val client = api.client
            runBlocking {
                if(option != null) {
                    ign = option.asString
                }
                if(ign == null) {
                    val response = client.request("link/get/${event.user.id}")
                    if (response.status.value >= 300){
                        hook.editOriginal("No Ign applied and no account linked!").queue()
                        return@runBlocking
                    }
                    ign = getMinecraftUsername(response.body<Link>().uuid)
                }
                val type = event.getOption("type")!!.asString

                val finaltype = when(type){
                    "thirty" -> Types.THIRTY
                    "weeks" -> Types.WEEKS
                    "total" -> Types.TOTAL
                    "seven" -> Types.SEVEN
                    else -> Types.WEEKS
                }


                val uptime = HistoricalUptime(ign!!, finaltype)
                val plot = uptime.createPlot().toPNG()
                hook.editOriginal("")
                    .setAttachments(FileUpload.fromData(plot, "uptime.png"))
                    .queue()
            }
            client.close()
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something failed, probably $ign doesnt have his uptime tracked").queue()
        }
    }
}