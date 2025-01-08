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
    name = "collection",
    description = "collection overtake",
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
            ]
        ),
        Option(
            name = "days",
            description = "the past x days for calculating the gain (1-30)",
            type = OptionType.INTEGER,
            required = true,
        )
    ]
)
class Collection {
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
            "collection",
            type,
            days
        )

        runBlocking {
            val resp =client.post("overtake"){
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