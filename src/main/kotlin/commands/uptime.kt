package commands

import getFarmingUptime
import getMinecraftUUID
import hypixelAPI
import listeners.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.hypixel.api.reply.GuildReply
import java.util.ArrayList
import kotlin.math.floor

class Uptime: ICommand {
    override val name = "uptime"
    override val description = "Gets the farming uptime of someone (requires to be in a guild)"
    override val options: List<OptionData>
        get() {
            val options = ArrayList<OptionData>()
            options.add(OptionData(OptionType.STRING, "ign", "The ign").setRequired(true))
            return options
        }
    override fun execute(event: SlashCommandInteractionEvent) {
        val ign = event.getOption("ign")!!.asString
        val uuid = getMinecraftUUID(ign)
        val hook = event.deferReply().complete()
        hypixelAPI.getGuildByPlayer(uuid).get().run {
            var player: GuildReply.Guild.Member? = null
            guild.members.forEach {
                if(it.uuid.toString().replace("-","") == uuid) player = it
            }


            val builder = EmbedBuilder()
            builder.setTitle("Uptime of $ign")
            var totalhours = 0
            var totalmins = 0
            player?.getFarmingUptime()?.forEach {
                val hours = if (it.value.first >= 10){
                    it.value.first
                }else{
                    "0${it.value.first}"
                }
                val mins = if (it.value.second >= 10){
                    it.value.second
                }else{
                    "0${it.value.second}"
                }
                totalhours += it.value.first
                totalmins += it.value.second
                builder.appendDescription(" `${it.key}` - ${hours}h ${mins}m \n")
            }
            totalhours += floor(totalmins/60f).toInt()
            builder.appendDescription("\n$ign has farmed a total of $totalhours hours and ${totalmins.mod(60)} mins this week\n")
            val avghrs = (totalhours + totalmins/60f)/7
            val hoursInt = floor(avghrs).toInt()
            val minutes = ((avghrs - hoursInt) * 60).toInt()
            builder.appendDescription("$ign has farmed $hoursInt hours and $minutes mins on average per day")
            val embed = builder.build()
            hook.editOriginal("").setEmbeds(embed).queue()
        }
    }
}