package com.dergruenkohl.commands.utility

import com.dergruenkohl.utils.isImageUrl
import com.dergruenkohl.utils.upload
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.metadata.ImageMetadata
import com.sksamuel.scrimage.nio.StreamingGifWriter
import com.sksamuel.scrimage.webp.WebpWriter
import dev.freya02.botcommands.jda.ktx.components.Container
import io.github.freya022.botcommands.api.commands.annotations.Command
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
import java.time.Duration
import java.util.UUID
import kotlin.math.roundToInt


@Command
class GifCommand {
    companion object {
        private const val MAX_LANDSCAPE_WIDTH = 1920
        private const val MAX_LANDSCAPE_HEIGHT = 1080
        private const val MAX_GIF_FPS = 15.0
    }

    private val logger = KotlinLogging.logger {  }
    @JDASlashCommand("gif", description = "create a gif from any image/video file")
    suspend fun createGif(event: GlobalSlashEvent,
                  @SlashOption("file", "The file you want to convert") file: Message.Attachment?,
                  @SlashOption("url", "The url of the file you want to convert") url: String? = null,
                  @SlashOption("ephemeral", "Should the response message be ephemeral?") ephemeral: Boolean = false,
                  @SlashOption(name = "upload", description = "Upload the gif to img.dergruenkohl.com") upload: Boolean = false,
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
            val fileurl = file?.url?: url!!
            if (fileurl.isImageUrl() && upload){
                convertImage(fileurl, stream)
            } else {
                convertVideoWithRecoder(fileurl, stream, progressCallback)
            }
            logger.info { "Stream size: ${stream.size()/1000/1000} MB" }

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
        var grabber: FFmpegFrameGrabber? = null
        var recorder: FFmpegFrameRecorder? = null
        var scaleFilter: FFmpegFrameFilter? = null
        var firstRecordedFrame: Frame? = null
        try {
            grabber = FFmpegFrameGrabber(URI(url).toURL())
            grabber.start()
            val activeGrabber = grabber
            val (targetWidth, targetHeight) = calculateTargetDimensions(activeGrabber.imageWidth, activeGrabber.imageHeight)
            logger.info { "input resolution: ${activeGrabber.imageWidth}x${activeGrabber.imageHeight}, output resolution: ${targetWidth}x${targetHeight}" }
            val sourceFps = activeGrabber.frameRate.takeIf { it > 0.0 } ?: 24.0
            val targetFps = minOf(sourceFps, MAX_GIF_FPS)
            val frameStep = kotlin.math.ceil(sourceFps / targetFps).toInt().coerceAtLeast(1)

            if (targetWidth != activeGrabber.imageWidth || targetHeight != activeGrabber.imageHeight) {
                scaleFilter = FFmpegFrameFilter("scale=$targetWidth:$targetHeight:flags=lanczos", targetWidth, targetHeight)
                scaleFilter.start()
            }

            recorder = FFmpegFrameRecorder.createDefault(file, targetWidth, targetHeight)
            recorder.format = "gif"
            recorder.pixelFormat = avutil.AV_PIX_FMT_RGB8
            recorder.frameRate = targetFps
            recorder.setVideoOption("gifflags", "+transdiff")
            recorder.start()
            val totalFramesToExtract = (activeGrabber.lengthInFrames).coerceAtLeast(1)
            var processedFrames = 0
            var recordedFrames = 0
            var lastProgress = 0
            logger.info { "input framerate: $sourceFps, output framerate: $targetFps, frame step: $frameStep" }
            logger.info { "input frames: ${activeGrabber.lengthInFrames}" }

            var frame = activeGrabber.grabFrame(false, true, true, false)
            while (frame != null) {
                if (processedFrames % frameStep == 0 && scaleFilter != null) {
                    scaleFilter.push(frame)
                    var filteredFrame = scaleFilter.pullImage()
                    while (filteredFrame != null) {
                        if (firstRecordedFrame == null) {
                            firstRecordedFrame = filteredFrame.clone()
                        }
                        recorder.record(filteredFrame)
                        recordedFrames++

                        filteredFrame = scaleFilter.pullImage()
                    }
                } else if (processedFrames % frameStep == 0 && frame.image != null) {
                    if (firstRecordedFrame == null) {
                        firstRecordedFrame = frame.clone()
                    }
                    recorder.record(frame)
                    recordedFrames++
                }

                processedFrames++
                val progress = (processedFrames * 100) / totalFramesToExtract
                if (progress != lastProgress && progress % 5 == 0) {
                    progressCallback(progress.coerceIn(1..100))
                    lastProgress = progress
                }

                frame = activeGrabber.grabFrame(false, true, true, false)
            }
            logger.info { "Processed frames: $processedFrames, recorded frames: $recordedFrames" }
            if (recordedFrames == 1){
                firstRecordedFrame?.let(recorder::record)
            }
            progressCallback(100)
            recorder.stop()
            recorder.release()
            recorder = null
            scaleFilter?.stop()
            scaleFilter?.release()
            scaleFilter = null
            activeGrabber.stop()
            activeGrabber.release()
            grabber = null
            file.inputStream().use { inputStream ->
                inputStream.copyTo(stream)
            }
            logger.info { "Output gif size: ${file.length() / 1024 / 1024} MB" }
        } catch (e: Exception) {
            logger.error(e) { "Error while converting" }
            throw e
        } finally {
            firstRecordedFrame?.close()
            runCatching { scaleFilter?.stop() }
            runCatching { scaleFilter?.release() }
            runCatching { recorder?.stop() }
            runCatching { recorder?.release() }
            runCatching { grabber?.stop() }
            runCatching { grabber?.release() }
            file.delete()
        }
    }

    private fun calculateTargetDimensions(width: Int, height: Int): Pair<Int, Int> {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val (maxWidth, maxHeight) = if (safeWidth >= safeHeight) {
            MAX_LANDSCAPE_WIDTH to MAX_LANDSCAPE_HEIGHT
        } else {
            MAX_LANDSCAPE_HEIGHT to MAX_LANDSCAPE_WIDTH
        }

        val scaleFactor = minOf(
            1.0,
            maxWidth.toDouble() / safeWidth,
            maxHeight.toDouble() / safeHeight,
        )

        return (
            safeWidth * scaleFactor
        ).roundToInt().coerceAtLeast(1) to (
            safeHeight * scaleFactor
        ).roundToInt().coerceAtLeast(1)
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

    fun convertImage(url: String, stream: OutputStream) {
        val image = ImmutableImage.loader().fromUrl(URI(url).toURL())
        val writer = WebpWriter.DEFAULT.withLossless()
        val metadata = ImageMetadata.fromImage(image)
        logger.info { "metadata: $metadata" }
        writer.write(image, image.metadata, stream)
    }

}