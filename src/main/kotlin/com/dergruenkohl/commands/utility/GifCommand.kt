package com.dergruenkohl.commands.utility

import com.dergruenkohl.utils.upload
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.GifWriter
import com.sksamuel.scrimage.nio.StreamingGifWriter
import dev.freya02.botcommands.jda.ktx.components.Container
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.util.url
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
import java.util.UUID


@Command
class GifCommand: ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }
    @JDASlashCommand("gif", description = "create a gif from any image/video file")
    suspend fun createGif(event: GlobalSlashEvent,
                  @SlashOption("file", "The file you want to convert") file: Message.Attachment?,
                  @SlashOption("url", "The url of the file you want to convert") url: String? = null,
                  @SlashOption("ephemeral", "Should the response message be ephemeral?") ephemeral: Boolean = false,
                  @SlashOption(name = "upload", description = "Upload the gif to catbox.moe") upload: Boolean = false,
    ) {
        try {

            if(file == null && url == null) {
                event.reply("You must provide either a file or a url").setEphemeral(ephemeral).queue()
                return
            }
            if (file != null && url != null){
                event.reply("You can only provide either a file or a url").setEphemeral(ephemeral).queue()
                return
            }
            if (file!= null){
                logger.info { "Converting ${file.fileName}" }


                if (!file.isImage && !file.isVideo){
                    event.hook.editOriginal("File type is not supported").queue()
                    return
                }
            }
            event.reply("Converting...").setEphemeral(ephemeral).queue()
            val stream = ByteArrayOutputStream()

            // Create progress callback
            val progressCallback = { progress: Int ->
                val progressBar = "█".repeat(progress / 5) + "░".repeat(20 - progress / 5)
                event.hook.editOriginal("Converting... [$progressBar] $progress%").queue()
            }
            convertVideoWithRecoder(file?.url?: url!!, stream, progressCallback)

            val fileUpload = FileUpload.fromData(stream.toByteArray(), "meow.gif")
            val container = Container{
                text("## Converted file: ${(file?.fileName)?:"input" .substringBefore(".")} to gif")
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
    fun convertVideoWithRecoder(url: String, stream: OutputStream, progressCallback: (Int) -> Unit = {}){
        if(url.substringAfterLast(".").substring(0..2).contains("gif")){
            val inputStream = URI(url).toURL().openStream()
            inputStream.copyTo(stream)
            inputStream.close()
            return
        }
        val file = File("images/temp/${UUID.randomUUID()}.gif")
        file.createNewFile()
        try {
            val grabber = FFmpegFrameGrabber(URI(url).toURL())
            grabber.start()
            val recorder = FFmpegFrameRecorder.createDefault(file, grabber.imageWidth, grabber.imageHeight)
            recorder.pixelFormat = avutil.AV_PIX_FMT_RGB8
            recorder.frameRate = grabber.frameRate // Match input frame rate
            recorder.start()
            val totalFramesToExtract = (grabber.lengthInFrames).coerceAtLeast(1)
            var extractedFrames = 0
            var lastProgress = 0
            logger.info { "input framerate: ${grabber.frameRate}" }
            logger.info { "input frames: ${grabber.lengthInFrames}" }

            var frame = grabber.grabFrame(false, true, true, false)
            val firstFrame = frame.clone()
            while (frame != null) {
                recorder.record(frame)
                frame = grabber.grabFrame(false, true, true, false)
                extractedFrames++
                // Update progress every 5%
                val progress = (extractedFrames * 100) / totalFramesToExtract
                if (progress != lastProgress && progress % 5 == 0) {
                    progressCallback(progress.coerceIn(1..100))
                    lastProgress = progress
                }
            }
            logger.info { "Extracted frames: $extractedFrames" }
            if (extractedFrames == 1){
                recorder.record(firstFrame)
            }
            progressCallback(100)
            recorder.stop()
            grabber.stop()
            firstFrame.close()
            val inputStream = file.inputStream()
            inputStream.copyTo(stream)
            inputStream.close()
            file.delete()
        } catch (e: Exception) {
            logger.error(e) { "Error while converting" }
            file.delete()
            throw e
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

}