package com.dergruenkohl.commands.utility

import dev.freya02.botcommands.jda.ktx.components.Container
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import net.dv8tion.jda.api.entities.User

@Command
class Avatar: ApplicationCommand() {
    @JDASlashCommand("avatar", description = "Get the avatar of a user")
    fun getAvatar(event: GlobalSlashEvent, @SlashOption user: User) {
        val avatarUrl = user.effectiveAvatarUrl
        event.replyComponents(Container {
            textDisplay("# Avatar of ${user.effectiveName}")
            mediaGallery{item(avatarUrl)}
        }).useComponentsV2().setEphemeral(true).queue()
    }
    @JDAUserCommand(name = "avatar")
    fun getAvatar(event: GlobalUserEvent) {
        val avatarUrl = event.targetMember?.effectiveAvatarUrl?: event.target.effectiveAvatarUrl
        event.replyComponents(Container {
            textDisplay("# Avatar of ${event.target.effectiveName}")
            mediaGallery{item(avatarUrl)}
        }).useComponentsV2().setEphemeral(true).queue()
    }

}