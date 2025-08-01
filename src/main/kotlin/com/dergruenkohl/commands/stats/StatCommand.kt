package com.dergruenkohl.commands.stats


import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUsername
import dev.freya02.botcommands.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.oshai.kotlinlogging.KotlinLogging

@Command
class StatCommand(private val gen: StatGenerator): ApplicationCommand() {
    private val logger = KotlinLogging.logger {}

    @Test
    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @JDASlashCommand(name = "stats", description = "View an Image for your farming Stats")
    suspend fun onStats(event: GlobalSlashEvent, @SlashOption(description = "Minecraft IGN") name: String? ){
        return event.reply("Not Implemented").queue()

        event.replyEmbeds(getLoading()).queue()
        try {
            //Get the minecraft username, if not provided, get the linked account and retrieve the ign for that
            val link = LinkRepo.getLink(event.user.idLong)
            val ign = name?: getMinecraftUsername(link?.uuid?: return event.hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "You need to provide a minecraft name or link your account"
            }).queue())

            gen.generate(ign, link)


        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, event.hook)
        }
    }
}