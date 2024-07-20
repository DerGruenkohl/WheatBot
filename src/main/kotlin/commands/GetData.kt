package commands

import kotlinx.coroutines.runBlocking
import listeners.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.translator.toLetsPlot
import share.PestGain
import share.TrackingManager
import share.data.CollectionPlot

class GetData: ICommand {


    override val name: String
        get() = "getdata"
    override val description: String
        get() = "gets the tracked data for a specific collection/pest"
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "type", "the type of data")
                .addChoices(
                    Choice("pests", "pest"),
                    Choice("collection", "coll")
                )
                .setRequired(true),
            OptionData(OptionType.STRING, "name", "the collection/pest you want to get")
                .addChoices(
                    Choice("carrot", "carrot"),
                    Choice("cactus", "cactus"),
                    Choice("cane", "cane"),
                    Choice("pumpkin", "pumpkin"),
                    Choice("wheat", "wheat"),
                    Choice("seeds", "seeds"),
                    Choice("mushroom", "mushroom"),
                    Choice("wart", "wart"),
                    Choice("melon", "melon"),
                    Choice("potato", "potato"),
                    Choice("cricket", "cricket_1"),
                    Choice("mite", "mite_1"),
                    Choice("slug", "slug_1"),
                    Choice("moth", "moth_1"),
                    Choice("worm", "worm_1"),
                    Choice("mosquito", "mosquito_1"),
                    Choice("fly", "fly_1"),
                    Choice("locust", "locust_1"),
                    Choice("beetle", "beetle_1"),
                    Choice("rat", "rat_1"),
                )
                .setRequired(true),
            OptionData(OptionType.USER, "user", "the user you want the data off").setRequired(false)
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        runBlocking {
            var userID = event.getOption("user")?.asUser?.idLong
            if (userID == null){ userID = event.user.idLong}
            val manager = TrackingManager(userID)
            var ephemeral = ephemeral
            val option = event.getOption("type")!!.asString
            val settings = manager.getSettings().settings
            var ownData = false
            if (!ephemeral){
                if (!settings.collectionGain && option == "coll"){ephemeral = true; ownData = true}
                if (!settings.pestGain && option == "pest"){ephemeral = true; ownData = true}
            }
            val hook = event.deferReply(ephemeral).complete()
            if (ownData && userID != event.user.idLong){hook.editOriginal("Not Allowed to view this data from this user").queue(); return@runBlocking}
            val name = event.getOption("name")!!.asString
            if (name.contains("_1") && option == "coll"){hook.editOriginal("please select pest for type").queue(); return@runBlocking }
            if (!name.contains("_1") && option != "coll"){hook.editOriginal("please select collection for type").queue(); return@runBlocking }
            val tracking = manager.getTracking()
            val time = CollectionPlot(tracking)
            val plot = time.createPlot(name).toPNG()
            val upload = FileUpload.fromData(plot, "$name.png")

            hook.editOriginal("Here is your data:")
                .setAttachments(upload)
                .queue()
        }
    }
}