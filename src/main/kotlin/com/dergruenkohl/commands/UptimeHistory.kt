package com.dergruenkohl.commands

import com.dergruenkohl.api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import com.dergruenkohl.listeners.Choice
import com.dergruenkohl.listeners.Command
import com.dergruenkohl.listeners.Option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import com.dergruenkohl.share.HistoricalUptime
import com.dergruenkohl.share.Link
import com.dergruenkohl.share.Types
import com.dergruenkohl.utils.getMinecraftUsername

@Command(
    name = "uptimehistory",
    description = "Gets the historical farming uptime of someone (data from before 3.11.24 may be inaccurate)",
    options = [
        Option(
            name = "type",
            description = "the type",
            type = OptionType.STRING,
            required = true,
            choices = [
                Choice("total","total"),
                Choice("30d","thirty"),
                Choice("7d","seven"),
                Choice("weeks","weeks"),
            ]
        )
    ]
)
class UptimeHistory {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val option = event.getOption("ign")
        var ign: String? = null
        val hook = event.deferReply()
            .setEphemeral(ephemeral)
            .complete()

        try {
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
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something failed, probably $ign doesnt have his uptime tracked").queue()
        }
    }
}