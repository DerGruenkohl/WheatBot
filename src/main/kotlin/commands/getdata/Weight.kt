package commands.getdata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import listeners.Option
import listeners.SubCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import share.ErrorHandler
import share.TrackingManager
import share.data.CollectionPlot
import utils.getMinecraftUsername

@SubCommand(
    name = "weight",
    description = "get the tracked weight of a user",
    options = [
        Option(
            name = "user",
            description = "the user to get the weight data of",
            type = OptionType.USER,
            required = true
        ),
        Option(
            name = "days",
            description = "the amount of days to get the data of (keep empty for total)",
            type = OptionType.INTEGER,
        )
    ]
)
class Weight {
     suspend fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = withContext(Dispatchers.IO) {
            event.deferReply(ephemeral).complete()
        }
        val userID = event.getOption("user")?.asUser?.idLong ?: event.user.idLong
        val days = event.getOption("days")?.asInt ?: 0

        val manager = TrackingManager(userID)
        val link = manager.getSettings() ?: return ErrorHandler.handle("no settings found", hook)

        val settings = link.settings
        if(!settings.track){hook.editOriginal("user has tracking disabled").queue(); return}

        hook.editOriginal("getting data for ${getMinecraftUsername(link.uuid)}.").queue()
        val tracking = manager.getTracking()
        println("track1")
        if (tracking.isEmpty()){
            hook.editOriginal("There is no tracked data for ${getMinecraftUsername(link.uuid)} yet").queue()
            return
        }
        val plot = CollectionPlot(tracking, days)

        val data = plot.createWeightPlot().toPNG()

        val upload = FileUpload.fromData(data, "weight.png")
        hook.editOriginal("Here is your data:")
            .setAttachments(upload)
            .queue()


    }


}