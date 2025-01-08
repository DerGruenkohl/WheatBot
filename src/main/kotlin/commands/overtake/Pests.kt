package commands.overtake

import api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import listeners.Choice
import listeners.Option
import listeners.SubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import share.OutgoingGraph
import share.Overtake
import share.OvertakeBody
import utils.getMeow
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@SubCommand(
    name = "pests",
    description = "pest overtake",
    options = [
        Option(
            name = "username1",
            description = "first user",
            type = OptionType.STRING,
            required = true
        ),
        Option(
            name = "username2",
            description = "second user",
            type = OptionType.STRING,
            required = true
        ),
        Option(
            name = "type",
            description = "the specific type for the overtake prediction",
            type = OptionType.STRING,
            required = true,
            choices = [
                Choice("mite", "mite"),
                Choice("cricket", "cricket"),
                Choice("moth", "moth"),
                Choice("worm", "worm"),
                Choice("slug", "slug"),
                Choice("beetle", "beetle"),
                Choice("locust", "locust"),
                Choice("rat", "rat"),
                Choice("mosquito", "mosquito"),
                Choice("fly", "fly"),
            ]
        ),
        Option(
            name = "days",
            description = "the past x days for calculating the gain (1-30)",
            type = OptionType.INTEGER,
            required = true
        )
    ]

)
class Pests {
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

        val name = event.getOption("username1")!!.asString
        val name2 = event.getOption("username2")!!.asString
        val days = event.getOption("days")!!.asInt
        val type = event.getOption("type")!!.asString

        val data = OvertakeBody(
            name,
            name2,
            "pest",
            type,
            days
        )
        runBlocking {
            val resp = client.post("overtake"){
                contentType(ContentType.Application.Json)
                setBody(data)
            }
            if (resp.status.value >= 300){
                hook.editOriginal("Something went wrong while fetching the data. Most Likely someone had their API off for longer than the specified duration").queue()
                return@runBlocking
            }
            val g = resp.body<OutgoingGraph>()

            val overtake = Overtake(g)
            val gen = overtake.generateOvertake()
            val os = ByteArrayOutputStream()
            ImageIO.write(gen.second, "png", os)
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "overtake.png"))
                .setEmbeds(gen.first)
                .queue()

        }
    }
}