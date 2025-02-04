package share

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import org.slf4j.Logger
import utils.getMeow
import java.awt.Color
import java.net.ConnectException
import javax.imageio.IIOException

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
                    .setFooter("discord.gg/ebxgyjhA5u")
                    .build()
            ).queue()
    }
    suspend fun handle(e: Exception, hook: InteractionHook, msg: String = "") {
        when(e) {
            is HttpRequestTimeoutException, is ConnectException -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("Failed to connect to the API, please try again later")
                            .setImage(getMeow())
                            .build()
                    ).queue()
            }
            is IIOException -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("Failed to generate an Image")
                            .setImage(getMeow())
                            .build()
                    ).queue()
            }
            is ClientRequestException -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("Api request failed: ${e.response.bodyAsText()}")
                            .setImage(getMeow())
                            .build()
                    ).queue()
            }
            else -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("An error occurred")
                            .setImage(getMeow())
                            .build()
                    ).queue()
            }
        }
    }
}