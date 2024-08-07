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
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import share.Link
import share.Member
import utils.getMinecraftUsername
import java.io.IOException
import kotlin.math.floor

class Uptime: ICommand {
    override val name = "uptime"
    override val description = "Gets the farming uptime of someone (requires to be in a guild)"
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
                if(option != null) {
                    ign = option.asString
                }
                if(ign == null) {
                    val response = client.request("link/get/${event.user.id}")
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
                val member = client.request("uptime/player/$ign").body<Member>()
                val builder = EmbedBuilder()
                builder.setTitle("Uptime of `$ign`")
                var totalhours = 0
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
                val embed = builder.build()
                hook.editOriginal("").setEmbeds(embed).queue()
            }
            client.close()
        }catch (e: Exception){
            e.printStackTrace()
            hook.editOriginal("Something failed, probably $ign is not in a guild").queue()
        }

    }
}