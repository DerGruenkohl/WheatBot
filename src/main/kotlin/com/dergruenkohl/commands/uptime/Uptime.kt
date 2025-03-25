package com.dergruenkohl.commands.uptime

import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUsername
import com.dergruenkohl.utils.hypixelutils.getFarmingUptime
import com.sksamuel.scrimage.nio.PngWriter
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@Command
object Uptime: ApplicationCommand() {
    private val logger = KotlinLogging.logger {}
    private val writer = PngWriter()

    @JDASlashCommand(name = "uptime", description = "Get the uptime of a player")
    suspend fun onUptime(event: GlobalSlashEvent, @SlashOption("ign", "The Person you want to view the Uptime off") name: String?) {
        event.replyEmbeds(getLoading()).queue()
        try {
            //Get the minecraft username, if not provided, get the linked account and retrieve the ign for that
            val link = LinkRepo.getLink(event.user.idLong)
            val ign = name?: getMinecraftUsername(link?.uuid?: return event.hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "You need to provide a minecraft name or link your account"
            }).queue())

            val uptime = getCachedUptime(ign, link) ?: return event.hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "Could not get uptime for $name"
            }).queue()
            val imgBytes = uptime.bytes(writer)
            event.hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(imgBytes, "uptime.png"))
                .queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, event.hook)
        }
    }
    @JDASlashCommand(name = "uptimegraph", description = "Get the uptime of a player")
    suspend fun onUptimeGraph(event: GlobalSlashEvent, @SlashOption("ign", "The Person you want to view the Uptime off") name: String?){
        event.replyEmbeds(getLoading()).queue()
        try {
            //Get the minecraft username, if not provided, get the linked account and retrieve the ign for that
            val link = LinkRepo.getLink(event.user.idLong)
            val ign = name?: getMinecraftUsername(link?.uuid?: return event.hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "You need to provide a minecraft name or link your account"
            }).queue())


            val uptime = (getUptime(ign)?: return event.hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "Could not get uptime for $name"
            }).queue()).getFarmingUptime()

            val yMins = uptime.values.map { it.toMinutes() }.reversed()
            val yStrings = uptime.values.map { it.toString() }.reversed()


            val plot = plot {
                y(yMins)
                x(uptime.keys.reversed())
                points {
                    size = 3.5
                    symbol = Symbol.BULLET
                    this.color = Color.BLUE
                }
                line {
                    this.color = Color.BLUE
                }

                layout{
                    title = "Uptime for $ign"
                    theme = Theme.HIGH_CONTRAST_DARK
                    x.axis.name = "Date"
                    y.axis.name = "Uptime"
                    //x.axis.breaksLabeled(data.keys.toList(), timestamps)
                    y.axis.breaksLabeled(yMins, yStrings)

                }
            }.toPNG()

            event.hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "uptime.png"))
                .queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, event.hook)
        }
    }
}