package commands.getdata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import listeners.Choice
import listeners.Option
import listeners.SubCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import share.ErrorHandler
import share.TrackingManager
import share.data.CollectionPlot
import utils.dsl.runAsync
import utils.getMinecraftUsername

@SubCommand(
    name = "cropweight",
    description = "get the tracked crop weight of a user",
    options = [
        Option(
            name = "user",
            description = "the user to get the weight data of",
            type = OptionType.USER,
            required = true
        ),
        Option(
            name = "type",
            description = "the collection type to get",
            type = OptionType.STRING,
            choices = [
                Choice("carrot", "carrot"),
                Choice("cactus", "cactus"),
                Choice("cane", "sugarCane"),
                Choice("pumpkin", "pumpkin"),
                Choice("wheat", "wheat"),
                Choice("seeds", "seeds"),
                Choice("mushroom", "mushroom"),
                Choice("wart", "wart"),
                Choice("melon", "melon"),
                Choice("potato", "potato"),
                Choice("cocoa", "cocoaBeans"),
            ],
            required = true
        ),
        Option(
            name = "days",
            description = "the amount of days to get the data of (keep empty for total)",
            type = OptionType.INTEGER,
        )
    ]
)
class CropWeight {
     suspend fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = withContext(Dispatchers.IO) {
            event.deferReply(ephemeral).complete()
        }
        val userID = event.getOption("user")?.asUser?.idLong ?: event.user.idLong
        val type = event.getOption("type")?.asString ?: return ErrorHandler.handle("no type found", hook)
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

        if (!settings.collectionGain){hook.editOriginal("collection view disabled").queue(); return}
        val plot = CollectionPlot(tracking, days)

        val data = plot.createWeightPlot(type).toPNG()

        val upload = FileUpload.fromData(data, "$type.png")
        hook.editOriginal("Here is your data:")
            .setAttachments(upload)
            .queue()


    }


}