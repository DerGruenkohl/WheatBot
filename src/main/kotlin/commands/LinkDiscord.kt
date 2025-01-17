package commands

import api.ApiInstance
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listeners.Command
import listeners.Option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import share.ErrorHandler
import share.Link
import utils.getMinecraftUUID

@Command(
    name = "link",
    description = "Link your account",
    options = [
        Option(
            type = OptionType.STRING,
            name = "username",
            description = "Your Username",
            required = true
        )
    ]
)
class LinkDiscord {
    private val LOGGER = KtorSimpleLogger("LinkDiscord")
    suspend fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val name = event.getOption("username")!!.asString
        val hook = withContext(Dispatchers.IO) {
            event.reply("Linking your account...").setEphemeral(true).complete()
        }
        try {
            val link = Link(
                event.user.idLong,
                getMinecraftUUID(name),
                event.user.name
            )
            ApiInstance.client.post("link/add") {
                contentType(ContentType.Application.Json)
                setBody(link)
            }
            hook.editOriginal("Successfully linked your mc account").queue()
        } catch (e: Exception) {
            LOGGER.error(e)
            ErrorHandler.handle(e, hook)
        }
    }
}