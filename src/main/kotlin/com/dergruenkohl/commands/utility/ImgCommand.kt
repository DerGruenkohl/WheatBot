package com.dergruenkohl.commands.utility

import com.dergruenkohl.api.client
import com.dergruenkohl.utils.upload
import dev.freya02.botcommands.jda.ktx.components.Container
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Message
import org.apache.commons.io.output.ByteArrayOutputStream

@Command
class ImgCommand {
    @JDASlashCommand(name = "img", description = "Upload a file to img.dergruenkohl.com")
    suspend fun onImg(event: GlobalSlashEvent,
                      @SlashOption(name = "file", description = "the file you want to upload") attachment: Message.Attachment)
    {
        event.deferReply().queue()
        val url = attachment.proxyUrl
        val bytes = client.get(url).bodyAsBytes()
        val stream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            stream.write(bytes)
        }
        val uploadURL = upload(stream, attachment.fileName)
        event.hook.editOriginalComponents(Container {
            text("File uploaded: $uploadURL")
            mediaGallery {
                item(uploadURL)
            }
        }).useComponentsV2().queue()
    }
}