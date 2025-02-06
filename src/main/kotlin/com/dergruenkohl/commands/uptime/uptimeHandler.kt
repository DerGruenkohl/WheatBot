package com.dergruenkohl.commands.uptime

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.config.Config
import com.dergruenkohl.hypixel.client.HypixelClient
import com.dergruenkohl.hypixel.client.getGuildByPlayer
import com.dergruenkohl.utils.GaussianFilter
import com.dergruenkohl.utils.database.GuildRepo.save
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import com.dergruenkohl.utils.hypixelutils.getFarmingUptime
import hypixel.data.guild.Member
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.geom.RoundRectangle2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.math.floor
private val logger = KotlinLogging.logger{}

suspend fun getUptime(ign: String): BufferedImage? {
    logger.info { "Getting uptime for $ign" }
    val uuid = getMinecraftUUID(ign)
    val guild = hypixelClient.getGuildByPlayer(uuid).guild?: return null
    guild.save()
    val member = guild.members.find { it.uuid == uuid }?: return null
    return ImageGen(member, ign).createImage()
}

private class ImageGen(private val member: Member, private val ign: String){
    private val width = 530
    private val height = 450
    private val font by lazy {
        val boldFont = javaClass.getResourceAsStream("/JetBrainsMono-ExtraBold.ttf")
        Font.createFont(Font.TRUETYPE_FONT, boldFont)
    }
    private val color = Color.WHITE
    private val logger = KotlinLogging.logger{ }

    private suspend fun setUpImage(): BufferedImage{
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val imageUrl = URL("https://www.bakingbusiness.com/ext/resources/2022/05/17/crop-progress_wheat_AdobeStock_LEAD.jpeg")
        val g: Graphics2D = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val wheatImage = withContext(Dispatchers.IO) {
            ImageIO.read(imageUrl)
        }
        val filter = GaussianFilter()
        filter.radius = 7f
        val newWheat = filter.filter(wheatImage, null)

        g.drawImage(newWheat, 0, 0, width, height, 0, 0,
            newWheat.width, newWheat.height, null)
        return image
    }
    private fun drawBackground(g: Graphics2D){
        val rect = RoundRectangle2D.Float(15f, 5f, 500f, 430f, 50f, 50f)
        g.color = Color(0f, 0f, 0f, 0.2f)
        g.fill(rect)
        g.color = color
    }
    private suspend fun drawTitles(g: Graphics2D){

        g.font = font.deriveFont(32f)
            .deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))
            .deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_LOW_TWO_PIXEL))
        g.drawString("Uptime of: $ign", 20, 40)

        g.font = font.deriveFont(24f)
        g.drawString("Date", 20, 80)
        g.drawString("Uptime", 200, 80)
    }
    private fun drawUptime(g: Graphics2D){
        g.font = font.deriveFont(18f)
        val uptime = member.getFarmingUptime()

        var yPosition = 100
        for ((timestamp, timeSpent) in uptime) {
            g.drawString(timestamp, 20, yPosition)
            g.drawString(timeSpent.toString(), 200, yPosition)
            yPosition += 30
        }

        var totalhours = 0
        var totalmins = 0
        member.getFarmingUptime().forEach {
            totalmins += it.value.mins
            totalhours += it.value.hours
        }
        totalhours += floor(totalmins/60f).toInt()
        yPosition += 30
        g.drawString("$ign has farmed a total of $totalhours hours ", 20, yPosition)
        yPosition +=20
        g.drawString("and ${totalmins.mod(60)} mins this week", 20, yPosition)
        val avghrs = totalhours /7f
        val hoursInt = floor(avghrs).toInt()
        val minutes = ((avghrs - hoursInt) * 60).toInt()
        yPosition += 30
        g.drawString("$ign has farmed $hoursInt hours and $minutes mins ", 20, yPosition)
        yPosition += 20
        g.drawString("on average per day", 20, yPosition)
    }

    private suspend fun getSkin(): BufferedImage {
        lateinit var skin: BufferedImage
        var scale = 0.3
        var flip = false

        try {
            skin = withContext(Dispatchers.IO) {
                ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${member.uuid}/full"))
            }
        }catch (e: IIOException){
            logger.info("Failed to get skin from starlight, falling back to crafatar")

            skin = withContext(Dispatchers.IO) {
                ImageIO.read(URL("https://crafatar.com/renders/body/${member.uuid}"))
            }
            scale = 0.9
            flip = true
        }


        val w: Int = skin.width
        val h: Int = skin.height

        // Create a new image of the proper size
        val w2 = (w * scale).toInt()
        val h2 = (h * scale).toInt()
        val after = BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB)
        val scaleInstance = AffineTransform.getScaleInstance(scale, scale)

        if (flip){
            val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
            tx.translate(-skin.getWidth(null).toDouble(), 0.0)
            val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
            skin = op.filter(skin, null)
        }
        val scaleOp = AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR)
        scaleOp.filter(skin, after)
        return after
    }

    suspend fun createImage(): BufferedImage {
        val image = setUpImage()
        val g = image.graphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        drawBackground(g)
        drawTitles(g)
        drawUptime(g)
        g.drawImage(getSkin(), 350, 50, null)

        g.dispose()
        return image

    }
}
