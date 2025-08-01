package com.dergruenkohl.commands.uptime

import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.database.UptimeRepo
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import com.dergruenkohl.utils.hypixelutils.Time
import dev.freya02.botcommands.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import java.time.LocalDate
import kotlin.time.measureTime

@Command
object UptimeHistory: ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }
    @JDASlashCommand(name = "uptimehistory", description = "Get the uptime history of a player")
    suspend fun onUptimeHistory(
        event: GlobalSlashEvent,
        @SlashOption("ign", "The Person you want to view the Uptime history of") name: String?,
        @SlashOption("days", "The number of days to get the history for") days: Int = 0
        ) {
        try {
            event.replyEmbeds(getLoading()).queue()
            val link = LinkRepo.getLink(event.user.idLong)
            val ign = name?: getMinecraftUsername(link?.uuid ?: return event.hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "You need to provide a minecraft name or link your account"
            }).queue())
            val uuid = getMinecraftUUID(ign)
            var data: Map<Int, Time>
            val dur = measureTime {
                data = UptimeRepo.getUptimeEntries(uuid)
            }
            logger.info { "Fetched data in $dur" }
            val filteredData = if(days > 0) {
                data.filter { it.key > LocalDate.now().toEpochDay() - days }
            }else {
                data
            }
            if (filteredData.isEmpty()) {
                event.hook.editOriginalEmbeds(Embed {
                    title = "Error"
                    description = "No uptime data found for $ign"
                }).queue()
                return
            }
            val plot = generatePlot(filteredData, ign)
            event.hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png"))
                .queue()


        } catch (e: Exception) {
            ErrorHandler.handle(e, event.hook)
        }
    }
    private fun generatePlot(data: Map<Int, Time>, ign: String) = plot {
        val timestamps = data.keys.map {
            kotlinx.datetime.LocalDate.fromEpochDays(it).toString()
        }
        val yValues = data.values.map { it.toMinutes() }
        x(timestamps)
        y(yValues)
        points {
            size = 3.5
            symbol = Symbol.BULLET
            this.color = Color.BLUE
        }
        line {
            this.color = Color.BLUE
        }

        layout{
            title = "Uptime history for $ign"
            theme = Theme.HIGH_CONTRAST_DARK
            x.axis.name = "Date"
            y.axis.name = "Uptime"
            //x.axis.breaksLabeled(data.keys.toList(), timestamps)
            y.axis.breaksLabeled(yValues, data.values.map { it.toString() })

        }
    }.toPNG()
}