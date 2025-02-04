package commands

import api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import listeners.Command
import listeners.Option
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.y
import share.ErrorHandler
import share.Member
import utils.getMeow
import utils.getMinecraftUsername

@Command(
    name = "compare_uptime",
    description = "compare your uptime with other players!",
    options = [
        Option(
            type = OptionType.STRING,
            name = "names",
            description = "the igns of all players like this: ign1,ign2,ign3,...",
            required = true
        )
    ]
)
class CompareUptime {
    suspend fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply()
            .setEphemeral(ephemeral)
            .complete()

        var meow = getMeow()
        if (meow == "-1") {
            meow = "https://cdn2.thecatapi.com/images/QUdOiX2hP.jpg"
        }
        withContext(Dispatchers.IO) {
            hook.editOriginal("").setEmbeds(
                EmbedBuilder()
                    .setTitle("Please wait a moment")
                    .setImage(meow)
                    .build()
            ).complete()
        }
        try {
            val option = event.getOption("names")!!.asString
            val members = option.split(",")
                .map { ign ->
                    client.request("uptime/player/$ign").body<Member>()
                }

            val frame = membersToDataFrame(members)
            val plot = frame.plot {
                line {
                    x("date")
                    y("uptime")
                    color("Players")
                }
                points {
                    x("date")
                    y("uptime")
                    color("Players")
                }
                layout {
                    y.axis.name = "Uptime in mins"
                    title = "Uptime Comparison"
                    theme = Theme.HIGH_CONTRAST_DARK
                }
            }.toPNG()
            hook.editOriginal("")
                .setAttachments(FileUpload.fromData(plot, "uptime.png"))
                .setEmbeds()
                .queue()
        } catch (e: Exception) {
            ErrorHandler.handle(e, hook)
        }

    }

    fun membersToDataFrame(members: List<Member>): AnyFrame {
        val months = mutableListOf<String>()
        val sales = mutableListOf<Int>()
        val categories = mutableListOf<String>()

        for (member in members) {
            for ((month, timeEntry) in member.expHistory) {
                months.add(LocalDate.fromEpochDays(month.toInt()).toString())
                sales.add(timeEntry.toMinutes())
                categories.add(getMinecraftUsername(member.uuid))
            }
        }
        sales.reverse()
        months.reverse()

        return dataFrameOf(
            "date" to months,
            "uptime" to sales,
            "Players" to categories
        )
    }
}