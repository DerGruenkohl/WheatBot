package commands

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.date.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.area
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.LineType
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Legend
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.statistics.plotting.smooth.statSmooth
import org.joda.time.DateTime
import share.Link
import share.Member
import utils.getMeow
import utils.getMinecraftUsername
import java.util.Calendar
import java.util.Date
import kotlin.math.floor
import kotlin.random.Random

class CompareUptime: ICommand {
    override val name = "compare_uptime"
    override val description = "compare your uptime with other players!"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() = listOf(
                OptionData(OptionType.STRING, "names", "the igns of all players like this: ign1,ign2,ign3,...", true),
            )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply()
            .setEphemeral(ephemeral)
            .complete()

        var meow = getMeow()
        if (meow == "-1"){meow = "https://cdn2.thecatapi.com/images/QUdOiX2hP.jpg"}
        hook.editOriginal("").setEmbeds(
            EmbedBuilder()
                .setTitle("Please wait a moment")
                .setImage(meow)
                .build()
        ).complete()
        try {
            val api = LocalAPI()
            val client = api.client
            runBlocking {
                val option = event.getOption("names")!!.asString
                val members = option.split(",")
                    .map { ign ->
                        client.request("uptime/player/$ign").body<Member>()
                    }
                client.close()

                val frame = membersToDataFrame(members)
                val plot =frame.plot {
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
            }
            client.close()
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something failed").queue()
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

        return dataFrameOf(
            "date" to months,
            "uptime" to sales,
            "Players" to categories
        )
    }
}