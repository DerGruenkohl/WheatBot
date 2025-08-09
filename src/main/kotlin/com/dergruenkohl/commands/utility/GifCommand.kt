package com.dergruenkohl.commands.utility

import com.dergruenkohl.utils.upload
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.StreamingGifWriter
import dev.freya02.botcommands.jda.ktx.components.Container
import java.nio.file.Files
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
import javax.imageio.ImageWriteParam


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
        val tempDir = Files.createTempDirectory("gif_conversion")
        val inputFile = tempDir.resolve("input")
        val outputFile = tempDir.resolve("output.gif")

        try {
            // Download input file
            URL(attachment.url).openStream().use { input ->
                Files.copy(input, inputFile)
            }

            // Build FFmpeg command with high-quality settings
            val command = listOf(
                "ffmpeg", "-y", "-i", inputFile.toString(),
                "-vf", "fps=25,scale=-1:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=256:stats_mode=diff[p];[s1][p]paletteuse=dither=bayer:bayer_scale=5:diff_mode=rectangle",
                "-loop", "0",
                outputFile.toString()
            )

            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            // Monitor progress by reading FFmpeg output
            process.inputStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let {
                        // Parse FFmpeg progress output if needed
                        if (it.contains("time=")) {
                            // Extract progress and call progressCallback
                        }
                    }
                }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Files.copy(outputFile, stream)
            } else {
                throw RuntimeException("FFmpeg conversion failed with exit code: $exitCode")
            }

        } finally {
            // Cleanup temp files
            Files.deleteIfExists(inputFile)
            Files.deleteIfExists(outputFile)
            Files.deleteIfExists(tempDir)
        }
    }

    fun convertImage(attachment: Message.Attachment, stream: OutputStream) {
        val tempDir = Files.createTempDirectory("gif_conversion")
        val inputFile = tempDir.resolve("input")
        val outputFile = tempDir.resolve("output.gif")

        try {
            // Download input file
            URL(attachment.url).openStream().use { input ->
                Files.copy(input, inputFile)
            }

            // Build FFmpeg command for single image
            val command = listOf(
                "ffmpeg", "-y", "-i", inputFile.toString(),
                "-vf", "scale=-1:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=256:stats_mode=single[p];[s1][p]paletteuse=dither=bayer:bayer_scale=3",
                "-t", "1",
                outputFile.toString()
            )

            val process = ProcessBuilder(command).start()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Files.copy(outputFile, stream)
            } else {
                throw RuntimeException("FFmpeg conversion failed with exit code: $exitCode")
            }

        } finally {
            // Cleanup temp files
            Files.deleteIfExists(inputFile)
            Files.deleteIfExists(outputFile)
            Files.deleteIfExists(tempDir)
        }
    }
}