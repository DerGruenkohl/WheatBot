package com.dergruenkohl.commands.utility

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import dev.freya02.botcommands.jda.ktx.components.Container
import dev.freya02.botcommands.jda.ktx.components.MediaGallery
import dev.freya02.botcommands.jda.ktx.components.MediaGalleryItem
import dev.freya02.botcommands.jda.ktx.components.TextDisplay
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.components.mediagallery.MediaGallery
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.FileUpload
import org.apache.commons.io.output.ByteArrayOutputStream
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.OutputStream
import java.net.URL
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.div
import kotlin.text.toInt
import kotlin.time.Duration.Companion.seconds

@Command
class GifCommand: ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }
    @JDASlashCommand("gif", description = "create a gif from any image/video file")
    fun createGif(event: GlobalSlashEvent,
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
                textDisplay("Converted file: ${file.fileName.substringBefore(".")} to gif")
            }
            event.hook.editOriginal("").setComponents(container).useComponentsV2().queue()
        } catch (e: Exception) {
            logger.error { e }
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
                        .scaleToWidth(320)

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
        val image = ImageIO.read(URL(attachment.url))
        ImageIO.write(image, "gif", stream)
    }
}