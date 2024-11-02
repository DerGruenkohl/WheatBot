package buttons.lbbuttons

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import listeners.IButton
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import share.data.LeaderboardPlot
import share.time

class Averages(override val id: String = "avg") : IButton {
    override fun execute(event: ButtonInteractionEvent) {
        val client = LocalAPI().client
        val hook = event.deferReply().setEphemeral(event.message.isEphemeral).complete()
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            val request = client.request("leaderboard/average")
            if (request.status.value >= 300){hook.editOriginal("Failed to get the average cuz: ${request.status.description}"); return@launch}

            val data = request.body<Map<Long, time>>()
            val plot = LeaderboardPlot(data).createPlot().toPNG()

            hook.editOriginal("")
                .setAttachments(FileUpload.fromData(plot, "uptime.png"))
                .queue()

        }
    }
}