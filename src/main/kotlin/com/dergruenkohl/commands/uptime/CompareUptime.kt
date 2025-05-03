package com.dergruenkohl.commands.uptime

import com.dergruenkohl.hypixel.data.guild.Member
import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getMinecraftUsername
import com.dergruenkohl.utils.hypixelutils.Time
import com.dergruenkohl.utils.hypixelutils.getFarmingUptime
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.y

@Command
object CompareUptime: ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }

    @JDASlashCommand(name = "compare_uptime", description = "Compare the uptime of multiple players")
    suspend fun onCompareUptime(
        event: GlobalSlashEvent,
        @SlashOption("ign", "The People you want to compare the Uptime of") @VarArgs(value = 5,) names: List<String>
    ) {
        try {
            event.reply("getting uptime for ${names.joinToString(", ")}").queue()
            val toGet = names
                .map { name -> getUptime(name)?: return event.hook.setEphemeral(true).sendMessage("Couldnt get uptime for $name").queue() }
            val hours = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23)
            val mins = hours.map { it * 60 }
            val hoursString = hours.map { "${it}h" }

            val data = membersToDataFrame(toGet)
            val plot = data.plot {
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
                    y.axis.name = "Uptime"
                    title = "Uptime Comparison"
                    theme = Theme.HIGH_CONTRAST_DARK
                    y.axis.breaksLabeled(mins, hoursString)
                }
            }.toPNG()
            event.hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png"))
                .queue()

        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, event.hook)
        }


    }
    private suspend fun membersToDataFrame(members: List<Member>): AnyFrame {
        val xAxis = mutableListOf<String>()
        val yAxis = mutableListOf<Int>()
        val categories = mutableListOf<String>()
        for (member in members) {
            for ((timestamp, timeEntry) in member.getFarmingUptime()) {
                xAxis.add(timestamp)
                yAxis.add(timeEntry.toMinutes())
                categories.add(getMinecraftUsername(member.uuid))
            }
        }
        yAxis.reverse()
        xAxis.reverse()
        categories.reverse()

        return dataFrameOf(
            "date" to xAxis,
            "uptime" to yAxis,
            "Players" to categories,
        )
    }
}