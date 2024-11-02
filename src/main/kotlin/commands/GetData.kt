package commands

import kotlinx.coroutines.runBlocking
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import share.TrackingManager
import share.data.CollectionPlot
import utils.getMinecraftUsername

class GetData: ICommand {
    val collectionChoices = listOf(
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
    )

    override val name: String
        get() = "getdata"
    override val description: String
        get() = "gets the tracked data for a specific collection/pest"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "type", "the type of data").setRequired(true)
                .addChoices(
                    Choice("collection", "collection"),
                    Choice("pests", "pests"),
                    Choice("cropweight", "weight"),
                    Choice("totalweight", "totalweight"),
                ),
            OptionData(OptionType.STRING, "name", "the collection/pest you want to get").addChoices(
                collectionChoices
            ),
            OptionData(OptionType.USER, "user", "the user you want the data off").setRequired(false),
            OptionData(OptionType.INTEGER, "days", "The last x tracked days (keep empty for all time)").setRequired(false)
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        runBlocking {
            var userID = event.getOption("user")?.asUser?.idLong
            if (userID == null){ userID = event.user.idLong}
            val manager = TrackingManager(userID)
            val hook = event.deferReply(ephemeral).complete()

            val type = event.getOption("type")!!.asString
            val link = manager.getSettings()
            if (link == null){
                hook.
                editOriginal("")
                    .setEmbeds(
                        EmbedBuilder()
                            .setDescription("Failed getting the link for <@$userID> , are you linked?")
                            .build()
                    )
                    .queue()
                return@runBlocking
            }
            val settings = link.settings
            if(!settings.track){hook.editOriginal("user has tracking disabled").queue(); return@runBlocking}

            hook.editOriginal("getting data for ${getMinecraftUsername(link.uuid)}.").queue()
            val tracking = manager.getTracking()
            println("track1")
            if (tracking.isEmpty()){
                hook.editOriginal("There is no tracked data for ${getMinecraftUsername(link.uuid)} yet").queue()
                return@runBlocking
            }

            try {
                val time = CollectionPlot(tracking)
                val plot: ByteArray?
                when(type){
                    "collection" -> {
                        if (!settings.collectionGain){hook.editOriginal("collection view disabled").queue(); return@runBlocking}
                        val name = event.getOption("name")!!.asString
                       plot = time.createCollectionPlot(name)
                            .toPNG()
                    }
                    "pests" -> {
                        println("meow meow")
                        if (!settings.pestGain){hook.editOriginal("pest view disabled").queue(); return@runBlocking}
                        val name = event.getOption("name")!!.asString
                        plot = time.createPestPlot(name)
                            .toPNG()
                    }
                    "weight" -> {
                        val name = event.getOption("name")!!.asString
                        plot = time.createWeightPlot(name)
                            .toPNG()
                    }
                    "totalweight" -> {
                        plot = time.createWeightPlot()
                            .toPNG()
                    }
                    else -> plot = null
                }

                if (plot == null){
                    hook.editOriginal("something failed").queue()
                    return@runBlocking
                }

                val upload = FileUpload.fromData(plot, "$name.png")

                hook.editOriginal("Here is your data:")
                    .setAttachments(upload)
                    .queue()

            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }
}