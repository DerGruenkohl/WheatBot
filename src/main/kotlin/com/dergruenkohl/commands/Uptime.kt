package com.dergruenkohl.commands

import com.dergruenkohl.api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dergruenkohl.listeners.Command
import com.dergruenkohl.listeners.Option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import com.dergruenkohl.share.ErrorHandler
import com.dergruenkohl.share.Link
import com.dergruenkohl.share.Member
import com.dergruenkohl.share.data.UptimeImage
import com.dergruenkohl.utils.getMinecraftUsername
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

@Command(
    name = "uptime",
    description = "Gets the farming uptime of someone (requires to be in a guild)",
    options = [
        Option(
            type = OptionType.STRING,
            name = "ign",
            description = "The ign"
        )
    ]
)
class Uptime {
    private val LOGGER = KtorSimpleLogger("Uptime")
    suspend fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val option = event.getOption("ign")
        var ign: String? = null
        val hook = withContext(Dispatchers.IO) {
            event.deferReply()
                .setEphemeral(ephemeral)
                .complete()
        }
        try {
            val response = client.request("link/get/${event.user.id}")
            if (option != null) {
                ign = option.asString
            }
            if (ign == null) {

                if (response.status.value >= 300) {
                    hook.editOriginal("No Ign applied and no account linked!").queue()
                    return
                }
                try {
                    ign = getMinecraftUsername(response.body<Link>().uuid)
                } catch (e: IOException) {
                    hook.editOriginal("No Ign applied and no account linked!").queue()
                    return
                }

            }
            val custom: String? = if (response.status.value >= 300) {
                println("failed to get settings")
                null
            } else {
                if (response.body<Link>().settings.customImage) {
                    println("setting userid")
                    event.user.id
                } else {
                    println("setting disabled")
                    null
                }
            }
            val color: Color? = if (response.status.value >= 300) {
                println("failed to get settings")
                null
            } else {
                if (response.body<Link>().settings.textColor == null) {
                    null
                } else {
                    Color.decode(response.body<Link>().settings.textColor)
                }
            }

            println(custom)
            val member = client.request("uptime/player/$ign").body<Member>()
            val imageGen = UptimeImage(member, custom, color)

            val image = imageGen.createImage()
            val os = ByteArrayOutputStream()
            withContext(Dispatchers.IO) {
                ImageIO.write(image, "png", os)
            }
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "uptime.png"))
                .queue()

            /*var totalhours = 0
            var totalmins = 0
            member.expHistory.forEach {
                totalmins += it.value.mins
                totalhours += it.value.hours
                val date = LocalDate.fromEpochDays(it.key.toInt())
                builder.appendDescription(" `${date.dayOfMonth}.${date.month.value}.${date.year}` - ${it.value.hours}h ${it.value.mins}m \n")
            }
            totalhours += floor(totalmins/60f).toInt()
            builder.appendDescription("\n`$ign` has farmed a total of $totalhours hours and ${totalmins.mod(60)} mins this week\n")
            val avghrs = totalhours /7f
            val hoursInt = floor(avghrs).toInt()
            val minutes = ((avghrs - hoursInt) * 60).toInt()
            builder.appendDescription("`$ign` has farmed $hoursInt hours and $minutes mins on average per day")
            */

        } catch (e: Exception) {
            LOGGER.error(e)
            ErrorHandler.handle(e, hook)
        }

    }
}