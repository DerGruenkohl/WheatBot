package com.dergruenkohl.commands.uptime

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.config.Data
import com.dergruenkohl.hypixel.client.getGuildByPlayer
import com.dergruenkohl.hypixel.data.guild.Member
import com.dergruenkohl.utils.database.GuildRepo.save
import com.dergruenkohl.utils.database.Link
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.hypixelutils.getAverageUptime
import com.dergruenkohl.utils.hypixelutils.getFarmingUptime
import com.dergruenkohl.utils.hypixelutils.getTotalUptime
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.drawables.DrawableImage
import com.sksamuel.scrimage.canvas.drawables.FilledRect
import com.sksamuel.scrimage.canvas.drawables.Text
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Color
import java.awt.Font
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}
private val scope = CoroutineScope(Dispatchers.IO)
private val cache = Cache.Builder<String, ImmutableImage>()
    .expireAfterWrite(60.minutes)
    .build()

suspend fun getCachedUptime(ign: String, link: Link?): ImmutableImage? {
    cache.get(ign)?.let {
        logger.info { "Found cached uptime for $ign" }
        return it
    }
    val uptime = getUptime(ign, link) ?: return null
    cache.put(ign, uptime)
    return uptime
}

suspend fun getUptime(ign: String, link: Link?): ImmutableImage? {
    logger.info { "Getting uptime for $ign" }
    val uuid = getMinecraftUUID(ign)
    val guild = hypixelClient.getGuildByPlayer(uuid).guild ?: return null
    scope.launch {
        guild.save()
    }
    val member = guild.members.find { it.uuid == uuid } ?: return null
    return NewImageGen(member, ign, link).createImage()
}

suspend fun getUptime(ign: String): Member? {
    logger.info { "Getting uptime for $ign" }
    val uuid = getMinecraftUUID(ign)
    val guild = hypixelClient.getGuildByPlayer(uuid).guild ?: return null
    scope.launch {
        guild.save()
    }
    val member = guild.members.find { it.uuid == uuid } ?: return null
    return member
}


private class NewImageGen(
    private val member: Member,
    private val ign: String,
    private val link: Link?
) {
    companion object {
        private const val WIDTH = 530
        private const val HEIGHT = 450

        private val imageLoader = ImmutableImage.loader()
        private val baseImage = imageLoader.fromPath(Data.folder.resolve("images/base_wheat.jpeg"))
        private val baseSkin = imageLoader.fromPath(Data.folder.resolve("images/no_skin.png"))

        private val base_font = run {
            val boldFont = Data.folder.resolve("fonts/JetBrainsMono-ExtraBold.ttf").inputStream()
            Font.createFont(Font.TRUETYPE_FONT, boldFont)
                .deriveFont(18f)
        }
        private val title_font = base_font
            .deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))
            .deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_LOW_TWO_PIXEL))
            .deriveFont(32f)
        private val header_font = base_font.deriveFont(24f)

        private val blackBox = FilledRect(
            0, 0, WIDTH, HEIGHT
        ) {
            it.color = Color(0f, 0f, 0f, 0.2f)
        }
        private val footer = Text(
            "This image was generated by WheatBot",
            20,
            430
        ) {
            it.font = base_font.deriveFont(16f)
            it.color = Color.WHITE
            it.setAntiAlias(true)
        }


        private val logger = KotlinLogging.logger { }

        private fun getSkin(uuid: String) =
            try {
                val img = ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${uuid}/full"))
                ImmutableImage.wrapAwt(img)
            } catch (e: Exception) {
                baseSkin
            }.scale(0.4)
    }


    private val color = if (link?.settings?.textColor is String) {
        try {
            Color.decode("#${link.settings.textColor}")
        } catch (e: Exception) {
            logger.warn { "Failed to decode ${link.settings.textColor}, falling back to white" }
            Color.WHITE
        }
    } else {
        Color.WHITE
    }
    private val skin = DrawableImage(
        getSkin(member.uuid),
        350,
        75
    )

    private val canvas = if (link?.settings?.customImage == true) {
        try {
            ImmutableImage.loader().fromPath(Data.folder.resolve("images/${link.discordId}.png"))
        } catch (e: Exception) {
            baseImage
        }
    } else {
        baseImage
    }
        .scaleTo(WIDTH, HEIGHT)
        .toCanvas()
    private val title = Text(
        "Uptime of: $ign",
        20,
        40
    ) {
        it.font = title_font
        it.color = color
        it.setAntiAlias(true)
    }
    private val dateText = Text(
        "Date",
        20,
        80
    ) {
        it.font = header_font
        it.color = color
        it.setAntiAlias(true)
    }
    private val uptimeText = Text(
        "Uptime",
        200,
        80
    ) {
        it.font = header_font
        it.color = color
        it.setAntiAlias(true)
    }

    private val dates = Text(
        "",
        20,
        100
    ) {
        it.font = base_font
        it.color = color
        it.setAntiAlias(true)

        val uptime = member.getFarmingUptime()
        var yPosition = 100
        for ((timestamp, timeSpent) in uptime) {
            it.drawString(timestamp, 20, yPosition)
            it.drawString(timeSpent.toString(), 200, yPosition)
            yPosition += 30
        }

    }

    private val averages = Text(
        "",
        0,
        0
    ) {
        it.setAntiAlias(true)
        it.font = base_font
        it.color = color

        val avgUptime = member.getAverageUptime()
        val totalUptime = member.getTotalUptime()

        it.drawString("Average Uptime:", 20, 350)
        it.drawString("${avgUptime.hours}h, ${avgUptime.mins}m", 200, 350)

        it.drawString("Total Uptime:", 20, 380)
        it.drawString("${totalUptime.hours}h, ${totalUptime.mins}m", 200, 380)

    }


    fun createImage(): ImmutableImage {
        canvas.drawInPlace(
            blackBox,
            skin,
            footer,
            title,
            dateText,
            uptimeText,
            dates,
            averages
        )
        return canvas.image
    }

}