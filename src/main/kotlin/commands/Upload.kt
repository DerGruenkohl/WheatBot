package commands

import io.ktor.http.*
import listeners.ICommand
import listeners.ISubCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.FileUpload
import org.apache.commons.io.FileUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO

class Upload: ICommand {
    override val name: String
        get() = "upload"
    override val description: String
        get() = "upload a custom background image"
    override val subCommands: List<ISubCommand>
        get() = listOf()
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.ATTACHMENT, "image", "the background image")
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply().complete()
        val file = event.getOption("image")!!.asAttachment

        if(!file.isImage) {
            hook.editOriginal("File is not supported").queue()
            return
        }
        //hook.editOriginal("url: ${file.url}").queue()

        val builder = EmbedBuilder()
        builder.setTitle("Successfully uploaded Image, awaiting approval")
        hook.editOriginal("")
            .setEmbeds(builder.build())
            .queue()

        val channel = event.jda.getTextChannelById("1252738320107180215")!!


        val os = ByteArrayOutputStream()
        ImageIO.write(ImageIO.read(URL(file.url)), "png", os)

        channel.sendMessage(event.user.id)
            .addActionRow(
                Button.success("accept","accept"),
                Button.danger("deny", "deny")
            )
            .addFiles(FileUpload.fromData(os.toByteArray(), "upload.${file.fileExtension}"))
            .queue()

    }
}