package commands

import kotlinx.coroutines.runBlocking
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.FileUpload
import share.TrackingManager
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class CustomColor: ICommand {
    override val name: String
        get() = "textcolor"
    override val description: String
        get() = "change the text color in the /uptime command"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "color", "the hex code of the color")
                .setRequired(true)
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply(true).complete()
        val manager = TrackingManager(event.user.idLong)
        runBlocking {
            val setting = event.getOption("color")!!.asString
            val os = ByteArrayOutputStream()
            try {
                val color = Color.decode(setting)
                val image = BufferedImage(530, 450, BufferedImage.TYPE_INT_RGB)
                val g: Graphics2D = image.createGraphics()
                g.color = color
                g.fill(Rectangle(0,0, 530, 450))
                g.dispose()
                println("r: ${color.red}, g: ${color.green}, g: ${color.blue}")
                ImageIO.write(image, "png", os)
                val outputFile = File("color.png")
                ImageIO.write(image, "png", outputFile)

            }catch (e: NumberFormatException) {
                hook.editOriginal("")
                    .setEmbeds(
                        EmbedBuilder()
                            .setTitle("$setting is not a valid color")
                            .build()
                    )
                    .queue()
                return@runBlocking
            }

            val success = manager.setColor(setting)
            if (success){
                hook.editOriginal("")
                    .setEmbeds(
                        EmbedBuilder()
                            .setTitle("Successfully Updated color to $setting")
                            .setImage("attachment://uptime.png")
                            .build()

                    ).setFiles(FileUpload.fromData(os.toByteArray(), "uptime.png"))
                    .queue()
            }
        }

    }
}