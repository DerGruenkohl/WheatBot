package commands.gain

import api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import listeners.Option
import listeners.SubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import okhttp3.internal.toLongOrDefault
import share.GainBody
import share.GainGenerator
import share.GraphPlayer
import utils.getMeow
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


@SubCommand(
    name = "pests",
    description = "pest gain",
    options = [
        Option(
            name = "username",
            description = "first user",
            type = OptionType.STRING,
            required = true
        ),
        Option(
            name = "type",
            description = "the specific type for the gain prediction",
            type = OptionType.STRING,
            required = true,
            choices = [
                listeners.Choice("mite", "mite"),
                listeners.Choice("cricket", "cricket"),
                listeners.Choice("moth", "moth"),
                listeners.Choice("worm", "worm"),
                listeners.Choice("slug", "slug"),
                listeners.Choice("beetle", "beetle"),
                listeners.Choice("locust", "locust"),
                listeners.Choice("rat", "rat"),
                listeners.Choice("mosquito", "mosquito"),
                listeners.Choice("fly", "fly"),
            ]
        ),
        Option(
            name = "days",
            description = "the past x days for calculating the gain (1-30)",
            type = OptionType.INTEGER,
            required = true
        ),
        Option(
            name = "goal",
            description = "the goal (must be a valid number, defaults to 1B)",
            type = OptionType.STRING,
            required = true
        )
    ]

)
class PestGain {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
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
            "pest",
            type,
            days
        )
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