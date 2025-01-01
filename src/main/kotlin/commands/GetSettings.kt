package commands

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listeners.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import share.TrackingManager

@Command(name = "getsettings", description = "get your linked settings")
class GetSettings {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply(true).complete()
        val manager = TrackingManager(event.user.idLong)
        runBlocking {
            val newSettings = manager.getSettings()
            if (newSettings == null){
                hook.editOriginal("Failed getting the link, are you linked?").queue()
                return@runBlocking
            }
            val json = Json {
                prettyPrint = true
                encodeDefaults = true
            }
            val embedBuilder = EmbedBuilder()
                .setDescription(
                    StringBuilder().apply {
                        append("DiscordID: ${newSettings.discordId} \n")
                        append("uuid: ${newSettings.uuid} \n")
                        append("Settings: ${json.encodeToString(newSettings.settings)}")
                    }.toString()
                )
            hook.editOriginal("").setEmbeds(embedBuilder.build()).queue()
        }
    }
}