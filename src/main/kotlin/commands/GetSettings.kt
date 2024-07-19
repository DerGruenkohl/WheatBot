package commands

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listeners.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import share.TrackingManager

class GetSettings: ICommand {
    override val name: String
        get() = "getsettings"
    override val description: String
        get() = "get your linked settings"
    override val options: List<OptionData>
        get() = ArrayList()

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply(true).complete()
        val manager = TrackingManager(event.user.idLong)
        runBlocking {
            val newSettings = manager.getSettings()
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