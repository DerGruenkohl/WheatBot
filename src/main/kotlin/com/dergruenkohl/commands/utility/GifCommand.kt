package com.dergruenkohl.commands.utility

import com.dergruenkohl.utils.upload
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.GifWriter
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
import org.bytedeco.ffmpeg.global.avutil
import java.io.OutputStream
import org.bytedeco.javacv.*
import java.io.File
import java.net.URI
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
            convertVideoWithRecoder(file, stream, progressCallback)
//            if (file.isImage) {
//                convertImage(file, stream)
//            } else {
//                convertVideo(file, stream, progressCallback)
//            }

            val fileUpload = FileUpload.fromData(stream.toByteArray(), "meow.gif")
//            val container = Container.of (
//                TextDisplay("Converted file: ${file.fileName} to gif"),
//                MediaGallery.of(
//                    MediaGalleryItem(fileUpload)
//                )
//            )
            val container = Container{
                text("## Converted file: ${file.fileName.substringBefore(".")} to gif")
                separator(isDivider = true)
                if(upload){
                    val url = upload(stream)
                    text("Upload url: $url")
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
    fun convertVideoWithRecoder(attachment: Message.Attachment, stream: OutputStream, progressCallback: (Int) -> Unit = {}){
        val file = File("images/temp/${attachment.idLong}.gif")
        file.createNewFile()
        try {
            val grabber = FFmpegFrameGrabber(URI(attachment.url).toURL())
            grabber.start()

            val recorder = FFmpegFrameRecorder.createDefault(file, attachment.width, attachment.height)
            recorder.pixelFormat = avutil.AV_PIX_FMT_RGB8
            recorder.frameRate = grabber.frameRate // Match input frame rate
            recorder.start()
            val totalFramesToExtract = (grabber.lengthInFrames).coerceAtLeast(1)
            var extractedFrames = 0
            var lastProgress = 0
            logger.info { "input framerate: ${grabber.frameRate}" }
            logger.info { "input frames: ${grabber.lengthInFrames}" }

            var frame = grabber.grabFrame(false, true, true, false)
            while (frame != null) {
                recorder.record(frame)
                frame = grabber.grabFrame(false, true, true, false)
                extractedFrames++
                // Update progress every 5%
                val progress = (extractedFrames * 100) / totalFramesToExtract
                if (progress != lastProgress && progress % 5 == 0) {
                    progressCallback(progress)
                    lastProgress = progress
                }
            }
            progressCallback(100)
            recorder.stop()
            grabber.stop()
            val inputStream = file.inputStream()
            inputStream.copyTo(stream)
            inputStream.close()
            file.delete()
        } catch (e: Exception) {
            logger.error(e) { "Error while converting" }
            file.delete()
        }
    }


    fun convertVideo(attachment: Message.Attachment, stream: OutputStream, progressCallback: (Int) -> Unit = {}) {
        val grabber = FFmpegFrameGrabber(URI(attachment.url).toURL())
        val converter = Java2DFrameConverter()

        try {
            grabber.start()
            val frameRate = grabber.frameRate
            val videoDuration = grabber.lengthInTime / 1_000_000.0
            logger.info { "Video frameRate = $frameRate, Video duration = $videoDuration" }
            val totalFramesToExtract = (grabber.lengthInFrames).coerceAtLeast(1)
            val frameDelayMs = (1000.0 / frameRate).toLong()

            val writer = StreamingGifWriter(Duration.ofMillis(frameDelayMs), true, false)
            val gif = writer.prepareStream(stream, java.awt.image.BufferedImage.TYPE_INT_RGB)
            var frameNumber = 0
            var extractedFrames = 0
            var frame = grabber.grabFrame()
            var lastProgress = -1
            while (frame != null && extractedFrames <= totalFramesToExtract) {
                if (frame.image != null) {
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
            logger.info { "Finished converting" }
        }
    }

    fun convertImage(attachment: Message.Attachment, stream: OutputStream) {
        val inputUrl = attachment.proxyUrl
        val image = ImmutableImage.loader().fromUrl(URI(inputUrl).toURL())
        val writer = WebpWriter.DEFAULT.withLossless()
        GifWriter.Progressive.write(image, image.metadata, stream)
    }
}