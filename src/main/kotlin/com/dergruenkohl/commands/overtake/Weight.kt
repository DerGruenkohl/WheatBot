package com.dergruenkohl.commands.overtake

import com.dergruenkohl.api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import com.dergruenkohl.listeners.Option
import com.dergruenkohl.listeners.SubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.FileUpload
import com.dergruenkohl.share.ErrorHandler
import com.dergruenkohl.share.OutgoingGraph
import com.dergruenkohl.share.Overtake
import com.dergruenkohl.share.OvertakeBody
import com.dergruenkohl.utils.getMeow
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@SubCommand(
    name = "weight",
    description = "weight overtake",
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
            name = "days",
            description = "the past x days for calculating the gain (1-30)",
            type = OptionType.INTEGER,
            required = true
        )
    ]
)
class Weight {
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
        val data = OvertakeBody(
            name,
            name2,
            "weight",
            "",
            days
        )
        runBlocking {
            try {
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
            } catch (e: Exception) {
                ErrorHandler.handle(e, hook)
            }

        }
    }
}