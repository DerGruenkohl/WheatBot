package commands

import listeners.Command
import listeners.Option
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

@Command(
    name = "upload",
    description = "upload a custom background image",
    options = [
        Option(
            name = "image",
            description = "the background image",
            type = OptionType.ATTACHMENT
        )
    ]
)
class Upload {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
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