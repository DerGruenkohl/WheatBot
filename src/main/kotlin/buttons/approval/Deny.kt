package buttons.approval

import listeners.IButton
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

class Deny(override val id: String= "deny") : IButton {
    override fun execute(event: ButtonInteractionEvent) {

        try {
            val userID = event.message.contentDisplay
            val file = event.message.attachments[0]

            val builder = EmbedBuilder()
            builder.setDescription("Successfully denied Image from <@$userID>")
            builder.setImage("attachment://${file.fileName}")
            event.editMessage("")
                .setEmbeds(builder.build())
                .setComponents()
                .queue()

            event.jda.retrieveUserById(userID).complete().openPrivateChannel().complete().sendMessage("Your custom background image has been denied.").queue()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

    }
}