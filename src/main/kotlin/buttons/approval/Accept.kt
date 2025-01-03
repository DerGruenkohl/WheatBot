package buttons.approval

import listeners.IButton
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

class Accept(override val id: String = "accept") : IButton {
    override fun execute(event: ButtonInteractionEvent) {
        val file = event.message.attachments[0]!!
        val userID = event.message.contentDisplay

        FileUtils.deleteDirectory(File("images/$userID").absoluteFile)

        FileUtils.copyURLToFile(
            URL(file.url),
            File("images/$userID/image.${file.fileExtension}"),
        )

        val builder = EmbedBuilder()
        builder.setDescription("Successfully approved Image from <@$userID>")
        builder.setImage("attachment://${file.fileName}")
        event.editMessage("")
            .setEmbeds(builder.build())
            .setComponents()
            .queue()

            event.jda.retrieveUserById(userID).complete().openPrivateChannel().complete().sendMessage("Your custom background image has been approved.").queue()

    }
}