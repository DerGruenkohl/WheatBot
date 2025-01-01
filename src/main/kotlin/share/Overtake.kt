package share

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import utils.getMinecraftUsername
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.geom.RoundRectangle2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import javax.imageio.IIOException
import javax.imageio.ImageIO


class Overtake(private val graph: OutgoingGraph) {
    fun Double.round(decimals: Int): BigDecimal {
        return BigDecimal(this).setScale(decimals, RoundingMode.HALF_EVEN)
    }
    fun BigDecimal.formatBigDecimal(): String {
        return when {
            this >= BigDecimal("1000000000") -> {
                val billions = this.divide(BigDecimal("1000000000"))
                String.format("%.2fB", billions)
            }
            this >= BigDecimal("1000000") -> {
                val millions = this.divide(BigDecimal("1000000"))
                String.format("%.2fM", millions)
            }
            this >= BigDecimal("1000") -> {
                val thousands = this.divide(BigDecimal("1000"))
                String.format("%.2fk", thousands)
            }
            else -> this.toPlainString()
        }
    }

    val category = when(graph.p1.type){
        "" -> "weight"
        else -> graph.p2.type
    }

    private fun generateOvertakeDuration(): Pair<String, String?> {
        if (graph.p1.full > graph.p2.full){return Pair("already passed", null)}
        if (graph.p1.gain < graph.p2.gain){return Pair("Overtake not possible", null)}
        val diff = graph.p2.full - graph.p1.full
        val gain = graph.p1.gain - graph.p2.gain
        if (gain == 0.0){return Pair("Overtake not possible", null)}
        val neededHours = diff/gain
        val days = (neededHours/24).toLong()
        val remainingHours = days.mod(24)
        val estimatedTotal = ((graph.p1.gain * neededHours) + graph.p1.full).toBigDecimal().formatBigDecimal()
        return Pair("$days days and $remainingHours hours",estimatedTotal)
    }
    private fun generateOvertakeEmbed(): MessageEmbed {
        val builder = EmbedBuilder()
        builder.setTitle("Overtake for $category")
        val dur = generateOvertakeDuration()

        val duration = dur.first
        var total = ""
        dur.second?.let {
            total = "Estimated total on overtake: $it"
        }

        builder.setDescription("""
            **Using Gain from the past ${graph.p1.days} days**
            
            Player1: `${getMinecraftUsername(graph.p1.uuid)}`
                Total: ${graph.p1.full.toBigDecimal().formatBigDecimal()}
                AvgGain: ${graph.p1.gain.toBigDecimal().formatBigDecimal()}/h
            
            Player2 `${getMinecraftUsername(graph.p2.uuid)}`
                Total: ${graph.p2.full.toBigDecimal().formatBigDecimal()}
                AvgGain: ${graph.p2.gain.toBigDecimal().formatBigDecimal()}/h
                
            Estimated time to overtake `${getMinecraftUsername(graph.p2.uuid)}`:
                $duration
                $total
        """.trimIndent())
        builder.setImage("attachment://overtake.png")
        builder.setFooter("Wheat! Powered by elitebot.dev")
        return builder.build()


    }
    private fun drawCenteredString(g: Graphics, text: String, rect: Rectangle, font: Font) {
        // Get the FontMetrics
        val metrics = g.getFontMetrics(font)
        // Determine the X coordinate for the text
        val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        val y = rect.y + ((rect.height - metrics.height) / 2) + metrics.ascent
        // Set the font
        g.font = font
        // Draw the String
        g.drawString(text, x, y)
    }

    private fun generateOvertakeImage(): BufferedImage {

        val boldFont = javaClass.getResourceAsStream("/JetBrainsMono-ExtraBold.ttf")
        val font = Font.createFont(Font.TRUETYPE_FONT, boldFont)

        val baseImage = javaClass.getResourceAsStream("/wheatField.jpg")
        val image = ImageIO.read(baseImage)

        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)


        var head1 = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        var head2 = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        //read the heads
        try {
            head1 =ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${graph.p1.uuid}/face") )
            head2 =ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${graph.p2.uuid}/face"))
        }catch (e: IIOException){
            println("Skin api died")
        }


        // Flip the image horizontally
        val tx = AffineTransform.getScaleInstance(-1.0, 1.0)

        tx.translate(-head1.getWidth(null).toDouble(), 0.0)

        val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
        // scale and draw the heads
        head1 = scaleImage(op.filter(head1, null))
        head2 = scaleImage(head2)

        g.drawImage(head1, 10+head2.width/2, 180- head1.height/2, null)
        g.drawImage(head2, image.width - head2.width -50, 180-head2.height/2, null)

        val rectX = (image.width - head2.width -10 - head2.width).toFloat()
        //Draw the text boxes
        val rects = listOf(
            RoundRectangle2D.Float(0f, 0f, image.width.toFloat(), 50f, 20f, 20f),
            RoundRectangle2D.Float(10f, 75f, head2.width.toFloat()*2, 50f, 20f, 20f),
            RoundRectangle2D.Float(10f, 250f, head2.width.toFloat()*2, 50f, 20f, 20f),
            RoundRectangle2D.Float(rectX, 75f, head2.width.toFloat()*2, 50f, 20f, 20f),
            RoundRectangle2D.Float(rectX, 250f, head2.width.toFloat()*2, 50f, 20f, 20f),
            RoundRectangle2D.Float(head2.width.toFloat()*2-10, (180- head1.height/2).toFloat()+10, head1.width.toFloat()*2+40, head1.height.toFloat(), 20f, 20f)

        )

        g.color = Color(0f, 0f, 0f, 0.5f)
        rects.forEach { g.fill(it) }

        g.color = Color.WHITE
        val data = generateOvertakeDuration()


        drawCenteredString(g, getMinecraftUsername(graph.p1.uuid), rects[1].bounds, font.deriveFont(20f).deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON)))
        drawCenteredString(g, getMinecraftUsername(graph.p2.uuid), rects[3].bounds, font.deriveFont(20f).deriveFont(mapOf(TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON)))
        g.color = Color.GREEN
        drawCenteredString(g,"+${graph.p1.gain.round(2).formatBigDecimal()}/h", rects[2].bounds, font.deriveFont(20f))
        drawCenteredString(g, "+${graph.p2.gain.round(2).formatBigDecimal()}/h", rects[4].bounds, font.deriveFont(20f))
        g.color = Color.WHITE
        drawCenteredString(g, data.first, rects[5].bounds, font.deriveFont(19f))
        data.second?.let {
            val newRect = rects[5].bounds.clone() as Rectangle
            newRect.setLocation(newRect.x, newRect.y+25)
            drawCenteredString(g, "Estimated Total: $it", newRect, font.deriveFont(19f))
            newRect.setLocation(newRect.x, newRect.y-50)
            drawCenteredString(g, "Overtake in:", newRect, font.deriveFont(19f))
        }
        var type = graph.p1.type
        if (type.isEmpty()) {type = "Weight"}
        drawCenteredString(g,"Overtake for: $type, Using Gain from the past ${graph.p1.days} days", rects[0].bounds, font.deriveFont(19f).deriveFont(mapOf(
            TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON)))
        g.dispose()

        return image
    }
    private fun scaleImage(target: BufferedImage): BufferedImage{
        val scale = 0.3

        val w: Int = target.width
        val h: Int = target.height

        // Create a new image of the proper size
        val w2 = (w * scale).toInt()
        val h2 = (h * scale).toInt()
        val after = BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB)
        val scaleInstance = AffineTransform.getScaleInstance(scale, scale)
        val scaleOp = AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR)

        scaleOp.filter(target, after)
        return after
    }

    fun generateOvertake(): Pair<MessageEmbed, BufferedImage> {
        val embed = generateOvertakeEmbed()
        val img = generateOvertakeImage()
        return Pair(embed, img)
    }
}
