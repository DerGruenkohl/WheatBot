package share

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import utils.getMeow
import java.awt.Color

object ErrorHandler {
    private val errorEmbed = EmbedBuilder()
        .setTitle("Error")
        .setColor(Color.RED)
        .setFooter("Have this cat :)")

    fun handle(msg: String, hook: InteractionHook) {
        hook.editOriginal("")
            .setEmbeds(
                errorEmbed
                    .setDescription(msg)
                    .setImage(getMeow())
                    .build()
            ).queue()
    }
}