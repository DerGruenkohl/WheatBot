package commands

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listeners.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import share.TrackingManager

@Command(
    name = "managesettings",
    description = "manage the tracking settings of your linked profile",
    options = [
        Option(
            type = OptionType.STRING,
            name = "setting",
            description = "settings",
            required = true,
            choices = [
                Choice("Track my Data", "track"),
                Choice("Allow retrieval of gain by pests", "pestGain"),
                Choice("Allow retrieval of collection gain", "collectionGain"),
                Choice("Allow retrieval of historical uptime (default yes)", "uptime"),
                Choice("Use custom image", "customImage")
            ]
        )
    ]
)
class ManageSetting {
    fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply(true).complete()
        val manager = TrackingManager(event.user.idLong)
        runBlocking {
            val setting = event.getOption("setting")!!.asString
            manager.updateSetting(setting)
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
            hook.editOriginal("Updated.").setEmbeds(embedBuilder.build()).queue()
        }

    }
}