package com.dergruenkohl.commands

import com.dergruenkohl.api.ApiInstance
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dergruenkohl.listeners.Command
import com.dergruenkohl.listeners.Option
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import com.dergruenkohl.share.ContestHandler
import com.dergruenkohl.share.ErrorHandler
import com.dergruenkohl.share.Link
import com.dergruenkohl.share.getContest
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import java.awt.Color

@Command(
    name = "contests",
    description = "gets a contest graph",
    options = [
        Option(
            type = OptionType.STRING,
            name = "name",
            description = "ign of the user"
        )
    ]
)
class Contests {
    private val LOGGER = KtorSimpleLogger("Contests")
    suspend fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = withContext(Dispatchers.IO) {
            event.deferReply(ephemeral).complete()
        }
        var ign = event.getOption("name")?.asString

        try {
            if (ign == null) {
                val response = ApiInstance.client.request("link/get/${event.user.id}")
                ign = getMinecraftUsername(response.body<Link>().uuid)

            }
            val contests = getContest(getMinecraftUUID(ign))
            val manager = ContestHandler(contests)

            val embed = EmbedBuilder()
            embed.setTitle("Contest Activity")
            if (contests.isEmpty()) {
                embed.setColor(Color.RED)
                embed.setDescription("No contests found for $ign")
                hook.editOriginal("").setEmbeds(embed.build()).queue()
                return
            }
            val builder = StringBuilder()
            val contestMap = manager.getContest().toMap().toSortedMap()
            builder.append("Showing hourly contests for **${ign}**\nGraph is on a 24 hour period based on time of contest.\nShowing over time period: **All Time**\n\n")

            contestMap.forEach { (hour, count) ->
                if (contestMap.values.max() < 10) {
                    builder.append("[${"█".repeat(count)}] (**$count**) \n")
                } else {
                    val scale = (contestMap.values.maxOrNull() ?: 10) / 10
                    builder.append("[${"█".repeat(count / scale)}] (**$count**) \n")
                }
            }
            embed.setDescription(builder.toString())
            hook.editOriginal("").setEmbeds(embed.build()).queue()
        } catch (e: Exception) {
            ErrorHandler.handle(e, hook)
        }
    }
}