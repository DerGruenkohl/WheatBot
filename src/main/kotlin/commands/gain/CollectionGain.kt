package commands.gain

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import okhttp3.internal.toLongOrDefault
import share.*
import utils.getMeow
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class CollectionGain: ISubCommand {
    override val name: String
        get() = "collection"
    override val description: String
        get() = "collection gain"
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "username", "first user").setRequired(true),
            OptionData(OptionType.STRING, "type", "the specific type for the gain prediction", true)
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
                    Choice("cocoa", "cocoa"),
                ),
            OptionData(OptionType.INTEGER, "days", "the past x days for calculating the gain", true)
                .setMinValue(1)
                .setMaxValue(30),
            OptionData(OptionType.STRING, "goal", "the goal (must be a valid number)", true)

        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply(ephemeral).complete()
        var meow = getMeow()
        if (meow == "-1"){meow = "https://cdn2.thecatapi.com/images/QUdOiX2hP.jpg"}
        hook.editOriginal("").setEmbeds(
            EmbedBuilder()
                .setTitle("Please wait a moment")
                .setImage(meow)
                .build()
        ).complete()

        val name = event.getOption("username")!!.asString
        val days = event.getOption("days")!!.asInt
        val type = event.getOption("type")!!.asString
        val goal = event.getOption("goal")!!.asString.toLongOrDefault(1_000_000_000)

        val data = GainBody(
            name,
            "collection",
            type,
            days
        )
        val client = LocalAPI().client
        runBlocking {
            val resp =client.post("gain"){
                contentType(ContentType.Application.Json)
                setBody(data)
            }
            if (resp.status.value >= 300){
                hook.editOriginal("Something went wrong while fetching the data. Most Likely someone had their API off for longer than the specified duration").queue()
                return@runBlocking
            }
            val g = resp.body<GraphPlayer>()
            client.close()

            val overtake = GainGenerator(g, goal)
            val gen = overtake.generateGain()
            val os = ByteArrayOutputStream()
            ImageIO.write(gen.second, "png", os)
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "overtake.png"))
                .setEmbeds(gen.first)
                .queue()

        }
    }
}