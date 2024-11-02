package commands

import api.LocalAPI
import apiUrl
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import share.Link
import share.Member
import share.data.UptimeImage
import utils.getMinecraftUsername
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.floor

class Uptime: ICommand {
    override val name = "uptime"
    override val description = "Gets the farming uptime of someone (requires to be in a guild)"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() {
            val options = ArrayList<OptionData>()
            options.add(OptionData(OptionType.STRING, "ign", "The ign"))
            return options
        }
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
                val custom: String? = if (response.status.value >= 300){
                    println("failed to get settings")
                    null
                }else{
                    if(response.body<Link>().settings.customImage){
                        println("setting userid")
                        event.user.id
                    }else{
                        println("setting disabled")
                        null
                    }
                }
                val color: Color? = if (response.status.value >= 300){
                    println("failed to get settings")
                    null
                }else {
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
                ImageIO.write(image, "png", os)
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

            }
            client.close()
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something failed, probably $ign is not in a guild").queue()
        }

    }
}