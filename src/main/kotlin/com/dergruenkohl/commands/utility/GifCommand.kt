package com.dergruenkohl.commands.utility

import com.dergruenkohl.utils.upload
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import com.sksamuel.scrimage.webp.WebpWriter
import dev.freya02.botcommands.jda.ktx.components.Container
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.FileUpload
import org.apache.commons.io.output.ByteArrayOutputStream
import java.io.OutputStream
import org.bytedeco.javacv.*
import java.net.URL
import java.time.Duration


@Command
class GifCommand: ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }
    @JDASlashCommand("gif", description = "create a gif from any image/video file")
    suspend fun createGif(event: GlobalSlashEvent,
                  @SlashOption("file", "The file you want to convert") file: Message.Attachment,
                  @SlashOption("ephemeral", "Should the response message be ephemeral?") ephemeral: Boolean = false,
                  @SlashOption(name = "upload", description = "Upload the gif to img.dergruenkohl.com") upload: Boolean = false,
    ) {
        try {
            logger.info { "Converting ${file.fileName}" }
            event.reply("Converting...").setEphemeral(ephemeral).queue()
            if (!file.isImage && !file.isVideo){
                event.hook.editOriginal("File type is not supported").queue()
                return
            }
            val stream = ByteArrayOutputStream()

            // Create progress callback
            val progressCallback = { progress: Int ->
                val progressBar = "█".repeat(progress / 5) + "░".repeat(20 - progress / 5)
                event.hook.editOriginal("Converting... [$progressBar] $progress%").queue()
            }

            if (file.isImage) {
                convertImage(file, stream)
            } else {
                convertVideo(file, stream, progressCallback)
            }

            val fileUpload = FileUpload.fromData(stream.toByteArray(), "meow.gif")
//            val container = Container.of (
//                TextDisplay("Converted file: ${file.fileName} to gif"),
//                MediaGallery.of(
//                    MediaGalleryItem(fileUpload)
//                )
//            )
            val container = Container{
                textDisplay("## Converted file: ${file.fileName.substringBefore(".")} to gif")
                separator(isDivider = true)
                if(upload){
                    val url = upload(stream)
                    textDisplay("Upload url: $url")
                    mediaGallery{
                        item(url)
                    }
                }else{
                    mediaGallery{
                        item(fileUpload)
                    }
                }

            }
            event.hook.editOriginal("").setComponents(container).useComponentsV2().queue()
        } catch (e: Exception) {
            logger.error(e) { "Error while converting" }
            event.hook.editOriginal("An error occurred while converting").queue()
        }
    }

    fun convertVideo(attachment: Message.Attachment, stream: OutputStream, progressCallback: (Int) -> Unit = {}) {
        val grabber = FFmpegFrameGrabber(URL(attachment.url))
        val converter = Java2DFrameConverter()

        try {
            grabber.start()
            val frameRate = grabber.frameRate
            val videoDuration = grabber.lengthInTime / 1_000_000.0
            val targetFps = 15.0

            val totalFramesToExtract = (videoDuration * targetFps).toInt()
            val frameSkip = (frameRate / targetFps).toInt().coerceAtLeast(1)
            val frameDelayMs = (1000.0 / targetFps).toLong()

            val writer = StreamingGifWriter(Duration.ofMillis(frameDelayMs), true, false)
            val gif = writer.prepareStream(stream, java.awt.image.BufferedImage.TYPE_INT_RGB)
            var frameNumber = 0
            var extractedFrames = 0
            var frame = grabber.grabFrame()
            var lastProgress = -1

            while (frame != null && extractedFrames < totalFramesToExtract) {
                if (frameNumber % frameSkip == 0 && frame.image != null) {
                    val bufferedImage = converter.convert(frame)
                    val immutableImage = ImmutableImage.fromAwt(bufferedImage)

                    gif.writeFrame(immutableImage)
                    extractedFrames++

                    // Update progress every 5%
                    val progress = (extractedFrames * 100) / totalFramesToExtract
                    if (progress != lastProgress && progress % 5 == 0) {
                        progressCallback(progress)
                        lastProgress = progress
                    }
                }
                frame = grabber.grabFrame()
                frameNumber++
            }

            gif.close()

        } finally {
            grabber.stop()
            grabber.release()
        }
    }

    fun convertImage(attachment: Message.Attachment, stream: OutputStream) {
        val inputUrl = attachment.url
        val image = ImmutableImage.loader().fromUrl(URL(inputUrl))


        val writer = WebpWriter.DEFAULT.withLossless()
        writer.write(image, image.metadata, stream)
    }
}