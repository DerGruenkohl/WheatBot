package com.dergruenkohl.commands.utility

import dev.freya02.botcommands.jda.ktx.components.Container
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData

@Command
class TestCommand: ApplicationCommand() {
    @Test
    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @JDASlashCommand("test", description = "test command")
    fun test(event: GuildSlashEvent) {
        val container = Container {
            text("test")
        }
        event.replyComponents(container)
            .useComponentsV2()
            .queue()
    }
}