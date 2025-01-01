package commands

import api.LocalAPI
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listeners.Command
import listeners.Option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
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
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val name = event.getOption("username")!!.asString
        val hook = event.reply("Linking your account...").setEphemeral(true).complete()
        val link = Link(
            event.user.idLong,
            getMinecraftUUID(name),
            event.user.name
        )
        runBlocking {
            val client = LocalAPI().client
            println(Json.encodeToString(link))
            val response: HttpResponse = client.post("link/add") {
                contentType(ContentType.Application.Json)
                setBody(link)
            }
            if(response.status.value >= 300){
                hook.editOriginal("Failed to Link your discord account with status code ${response.status} : ${response.bodyAsText()}").queue()
            }
            else{
                hook.editOriginal("Successfully linked your mc account").queue()
            }
            client.close()
        }

    }
}