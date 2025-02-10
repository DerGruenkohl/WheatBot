package com.dergruenkohl.commands

import com.dergruenkohl.config.Data
import com.dergruenkohl.config.Environment
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

@Command
class UploadCommand(private val buttons: Buttons): ApplicationCommand() {
    @JDASlashCommand(name = "upload")
    suspend fun onUpload(event: GlobalSlashEvent, @SlashOption("file", "The file you want to upload") file: Attachment) {
        event.reply("Uploading").setEphemeral(true).queue()
        val channel = if(Environment.isDev){event.channel} else {event.jda.getTextChannelById(1252738320107180215)!!}
        if(!file.isImage) {
            event.hook.editOriginal("File is not supported").queue()
            return
        }

        event.hook.editOriginal("").setEmbeds(
            Embed {
                title = "File uploaded"
                description = "File: ${file.fileName} successfully uploaded, awaiting approval"
            }
        )

        val os = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            ImageIO.write(ImageIO.read(URL(file.url)), "png", os)
        }

        channel.sendMessage(event.user.id)
            .addActionRow(
                buttons.success("approve").persistent {
                    bindWith(::handle, true, event.user)
                },
                buttons.danger("deny").persistent {
                    bindWith(::handle, false, event.user)
                }
            )
            .addFiles(FileUpload.fromData(os.toByteArray(), "${file.fileName}.png"))
            .queue()
    }
    @JDAButtonListener
    suspend fun handle(event: ButtonEvent, @ComponentData approved: Boolean, @ComponentData user: User){
        val file = event.message.attachments[0]
        if(approved){
            event.reply("Approved").queue()

            val folder = Data.folder.resolve("images").toFile()
            if(!folder.exists()){
                folder.mkdirs()
            }
            val img = folder.resolve("${user.id}.${file.fileExtension}")
            img.writeBytes(URL(file.url).readBytes())
            user.openPrivateChannel().queue { it.sendMessage("Approved your uploaded image(${file.fileName})").queue() }
        } else {
            event.reply("Denied").queue()
            user.openPrivateChannel().queue { it.sendMessage("Denied your uploaded image (${file.fileName})").queue() }
        }
    }
}