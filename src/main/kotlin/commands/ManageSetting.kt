package commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import listeners.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import share.TrackingManager

class ManageSetting: ICommand {
    override val name: String
        get() = "managesettings"
    override val description: String
        get() = "manage the tracking settings of your linked profile"
    override val options: List<OptionData>
        get() = listOf(
            OptionData(OptionType.STRING, "setting", "settings")
                .addChoices(
                    Command.Choice("Track my Data", "track"),
                    Command.Choice("Allow retrieval of gain by pests", "pestGain"),
                    Command.Choice("Allow retrieval of collection gain", "collectionGain"),
                    Command.Choice("Allow retrieval of historical uptime (default yes)", "uptime")
                )
                .setRequired(true)
        )

    override fun execute(event: SlashCommandInteractionEvent, ephemeral: Boolean) {
        val hook = event.deferReply(true).complete()
        val manager = TrackingManager(event.user.idLong)
        runBlocking {
            val setting = event.getOption("setting")!!.asString
            manager.updateSetting(setting)
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
            hook.editOriginal("Updated.").setEmbeds(embedBuilder.build()).queue()
        }

    }
}