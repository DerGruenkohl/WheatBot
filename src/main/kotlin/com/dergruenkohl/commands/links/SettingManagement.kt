package com.dergruenkohl.commands.links

import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.database.Settings
import com.dergruenkohl.utils.ErrorHandler
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.builder.button.ButtonFactory
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.api.modals.shortTextInput
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.components.ActionRow

@Command
class SettingManagement(private val buttons: Buttons, private val modals: Modals): ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }
    @JDASlashCommand("link", description = "Manage your link settings", subcommand = "manage")
    suspend fun onLinkManage(event: GlobalSlashEvent) {
        val hook = event.deferReply(true).await()
        try {
            val link = LinkRepo.getLink(event.user.idLong)?: return hook.editOriginal("You need to link your account first").queue()

            hook.editOriginal("")
                .setEmbeds(link.toEmbed())
                .setComponents(createSettingButtons(link.settings))
                .queue()


        }catch (e: Exception){
            logger.error{ e }
            ErrorHandler.handle(e, hook)
        }
    }


    @JDAButtonListener
    suspend fun handleBoolean(event: ButtonEvent, @ComponentData setting: String, @ComponentData enabled: Boolean){
        val hook = event.deferEdit().await()
        val link = LinkRepo.getLink(event.user.idLong)?: return
        val msgHook = hook.setEphemeral(true).sendMessage("Setting $setting to ${!enabled}" ).await()
        val settings = link.settings
        val newSettings = when(setting){
            "Track" -> settings.copy(track = !enabled)
            "Custom Image" -> settings.copy(customImage = !enabled)
            else -> settings
        }
        LinkRepo.createOrUpdateLink(link.copy(settings = newSettings))
        val newLink = LinkRepo.getLink(event.user.idLong)?: return

        hook.editOriginal("")
            .setEmbeds(newLink.toEmbed())
            .setComponents(createSettingButtons(newLink.settings))
            .await()
        msgHook.editMessage("$setting set to ${!enabled}").await()
    }
    @JDAButtonListener
    suspend fun handleText(event: ButtonEvent){
        val link = LinkRepo.getLink(event.user.idLong)?: return
        val modal = modals.create("TextColor"){
            shortTextInput("color", "Enter a Hex Color"){
                isRequired = true
                placeholder = "FFFFFF"
                maxLength = 6
                minLength = 6
            }
            bindTo("TextColor")
        }
        event.replyModal(modal).queue()
    }
    @ModalHandler("TextColor")
    suspend fun handleTextModal(event: ModalEvent, @ModalInput("color") color: String){
        val hook = event.deferEdit().await()
        val link = LinkRepo.getLink(event.user.idLong)?: return
        val msgHook = hook.setEphemeral(true).sendMessage("Setting Text Color").await()
        val newSettings = link.settings.copy(textColor = color)
        val newLink = link.copy(settings = newSettings)
        LinkRepo.createOrUpdateLink(newLink)

        hook.editOriginal("")
            .setEmbeds(newLink.toEmbed())
            .setComponents(createSettingButtons(newLink.settings))
            .await()
        msgHook.editMessage("Text Color Set to $color").await()
    }


    private suspend fun ButtonFactory.booleanSetting(name: String, enabled: Boolean) = this.persistent{
        bindWith(::handleBoolean, name, enabled)
    }
    private suspend fun createButton(name: String, enabled: Boolean) =
        if (enabled) buttons.success(name).booleanSetting(name, true)
        else buttons.danger(name).booleanSetting(name, false)

    private suspend fun createSettingButtons(settings: Settings): List<ActionRow> {
        return listOf(
            row(
                createButton("Track", settings.track),
                createButton("Custom Image", settings.customImage),
                buttons.primary("TextColor").persistent {
                    bindWith(::handleText)
                }
            )
        )
    }
}