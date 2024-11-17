package share.data

import io.ktor.http.*
import kotlinx.datetime.LocalDate
import share.Member
import utils.GaussianFilter
import utils.getMinecraftUsername
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.geom.RoundRectangle2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.floor


class UptimeImage(private val member: Member, private val custom: String? = null, private var color: Color? = null) {
    fun createImage(width: Int = 530, height: Int = 450): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        if (color == null){color = Color.WHITE}
        val g: Graphics2D = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        var imageURL = URL("https://www.bakingbusiness.com/ext/resources/2022/05/17/crop-progress_wheat_AdobeStock_LEAD.jpeg")

        custom?.let {
            val file = File("images/$custom").listFiles()!![0]
            imageURL = file.toURI().toURL()
        }


       //  val imageURL = URL("https://images.stockcake.com/public/8/1/2/812761e3-f576-4cce-acbe-68aea54aba55_large/golden-wheat-field-stockcake.jpg")
        val wheatImage = ImageIO.read(imageURL)
        //var newWheat = BufferedImage(width, height, wheatImage.type)



        val filter = GaussianFilter()
        filter.radius = 7f
        val newWheat = filter.filter(wheatImage, null)

        g.drawImage(newWheat, 0, 0, width, height, 0, 0,
            newWheat.width, newWheat.height, null)



        val minecraftFont = javaClass.getResourceAsStream("/Minecraft.ttf")
        val boldFont = javaClass.getResourceAsStream("/JetBrainsMono-ExtraBold.ttf")
        val font = Font.createFont(Font.TRUETYPE_FONT, boldFont)
        font.deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))

        val ign = getMinecraftUsername(member.uuid)

        val rect = RoundRectangle2D.Float(15f, 5f, 500f, 430f, 50f, 50f)

        g.color = Color(0f, 0f, 0f, 0.2f)
        g.fill(rect)

        g.color = color

        g.font = font.deriveFont(32f).deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_LOW_TWO_PIXEL))
        g.drawString("Uptime of: $ign", 20, 40)

        g.font = font.deriveFont(24f)
        g.drawString("Date", 20, 80)
        g.drawString("Uptime", 200, 80)

        g.font = font.deriveFont(18f)

        var yPosition = 100
        for ((timestamp, timeSpent) in member.expHistory) {
            g.drawString(LocalDate.fromEpochDays(timestamp.toInt()).toString(), 20, yPosition)
            g.drawString(timeSpent.toString(), 200, yPosition)
            yPosition += 30
        }

        var totalhours = 0
        var totalmins = 0
        member.expHistory.forEach {
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

        val skin = ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${member.uuid}/full"))

        val scale = 0.3

        val w: Int = skin.width
        val h: Int = skin.height

        // Create a new image of the proper size
        val w2 = (w * scale).toInt()
        val h2 = (h * scale).toInt()
        val after = BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB)
        val scaleInstance = AffineTransform.getScaleInstance(scale, scale)
        val scaleOp = AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR)

        scaleOp.filter(skin, after)

        g.drawImage(after, 350, 50, null)

        g.dispose()

        return image
    }

    fun saveImage(image: BufferedImage, filePath: String) {
        val outputFile = File(filePath)
        ImageIO.write(image, "png", outputFile)
    }

}