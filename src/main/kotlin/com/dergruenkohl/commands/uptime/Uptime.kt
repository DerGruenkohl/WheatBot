package com.dergruenkohl.commands.uptime

import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUsername
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Command
object Uptime: ApplicationCommand() {
    @JDASlashCommand(name = "uptime")
    suspend fun onUptime(event: GlobalSlashEvent, @SlashOption("ign", "The Person you want to view the Uptime off") name: String?) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            //Get the minecraft username, if not provided, get the linked account and retrieve the ign for that
            val ign = name?: getMinecraftUsername(LinkRepo.getLink(event.user.idLong)?.uuid?: return hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "You need to provide a minecraft name or link your account"
            }).queue())

            val uptime = getUptime(ign)?: return hook.editOriginalEmbeds(Embed {
                title = "Error"
                description = "Could not get uptime for $name"
            }).queue()

            val os = ByteArrayOutputStream()
            withContext(Dispatchers.IO) {
                ImageIO.write(uptime, "png", os)
            }
            hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(os.toByteArray(), "uptime.png"))
                .queue()
        } catch (e: Exception) {
            ErrorHandler.handle(e, hook)
        }
    }
}