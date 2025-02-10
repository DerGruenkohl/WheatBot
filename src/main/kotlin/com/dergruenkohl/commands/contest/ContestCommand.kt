package com.dergruenkohl.commands.contest

import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getMinecraftUUID
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

@Command
object ContestCommand: ApplicationCommand() {
    @JDASlashCommand(name = "contests", description = "Get a contest graph for a user")
    suspend fun onContest(event: GlobalSlashEvent, @SlashOption("ign") ign: String) {
        try {
            event.deferReply().queue()
            val contests = getContest(getMinecraftUUID(ign))
            val manager = ContestHandler(contests)
            val embed = EmbedBuilder()
            embed.setTitle("Contest Activity")
            if (contests.isEmpty()) {
                embed.setColor(Color.RED)
                embed.setDescription("No contests found for $ign")
                event.hook.editOriginal("").setEmbeds(embed.build()).queue()
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
            event.hook.editOriginal("").setEmbeds(embed.build()).queue()
        } catch (e: Exception) {
            ErrorHandler.handle(e, event.hook)
        }
    }
}