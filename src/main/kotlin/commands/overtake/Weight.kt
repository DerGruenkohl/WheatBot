package commands.overtake

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import share.OutgoingGraph
import share.Overtake
import share.OvertakeBody
import utils.getMeow
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class Weight: ISubCommand {
    override val name: String
        get() = "weight"
    override val description: String
        get() = "weight overtake"
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "username1", "first user").setRequired(true),
            OptionData(OptionType.STRING, "username2", "second user").setRequired(true),
            OptionData(OptionType.INTEGER, "days", "the past x days for calculating the gain", true)
                .setMinValue(1)
                .setMaxValue(30)
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

        val name = event.getOption("username1")!!.asString
        val name2 = event.getOption("username2")!!.asString
        val days = event.getOption("days")!!.asInt
        val data = OvertakeBody(
            name,
            name2,
            "weight",
            "",
            days
        )
        val client = LocalAPI().client
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
            client.close()

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