package commands

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import listeners.Command
import listeners.Option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
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
import share.Link
import share.Member
import utils.getMinecraftUsername

@Command(
    name = "uptimegraph",
    description = "Gets the farming uptime of someone with a nice graph (requires to be in a guild)",
    options = [
        Option(
            type = OptionType.STRING,
            name = "ign",
            description = "The ign"
        )
    ]
)
class UptimeGraph {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
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
                val member = client.request("uptime/player/$ign").body<Member>()

                val plot = plot {
                    layout {
                        size = 854 to 480
                        theme = Theme.DARCULA
                        title = "Uptime of $ign"

                    }
                    y(member.expHistory.values
                    .map {
                        it.hours.toFloat()+ it.mins.toFloat()/60f
                    }.asReversed()){
                        axis.name = "Hours"
                    }


                    x(member.expHistory.keys
                        .map {
                            val date = LocalDate.fromEpochDays(it.toInt())
                            "${date.dayOfMonth}.${date.month}"
                        }
                        .asReversed()
                    ){
                        axis.name = "day"
                    }
                    line {
                        color = Color.WHITE
                        type = LineType.SOLID
                    }
                    points {
                        size = 3.5
                        symbol = Symbol.BULLET
                        color = Color.BLUE
                    }

                }.toPNG()
                hook.editOriginal("")
                    .setAttachments(FileUpload.fromData(plot, "uptime.png"))
                    .queue()
            }
            client.close()
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something failed, probably $ign is not in a guild").queue()
        }

    }
}