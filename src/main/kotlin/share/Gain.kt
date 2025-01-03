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
import java.awt.geom.AffineTransform
import java.awt.geom.RoundRectangle2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import javax.imageio.ImageIO


class GainGenerator(private val gain: GraphPlayer, private val goal: Long) {
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

    val category = when(gain.type){
        "" -> "weight"
        else -> gain.type
    }

    private fun generateOvertakeDuration(): String{
        if (gain.full > goal){return "already reached"}
        val diff = goal - gain.full
        val gain = gain.gain
        if (gain == 0.0){return "You are not making any gains"}
        val neededHours = diff/gain
        val days = (neededHours/24).toLong()
        val remainingHours = days.mod(24)
        return "$days days and $remainingHours hours"
    }
    private fun generateOvertakeEmbed(): MessageEmbed {
        val builder = EmbedBuilder()
        builder.setTitle("Estimated time to reach ${goal.toBigDecimal().formatBigDecimal()} $category")
        val duration = generateOvertakeDuration()

        builder.setDescription("""
            **Using Gain from the past ${gain.days} days**
            
            Player: `${getMinecraftUsername(gain.uuid)}`
                Total: ${gain.full.toBigDecimal().formatBigDecimal()}
                AvgGain: ${gain.gain.toBigDecimal().formatBigDecimal()}/h
                
            Estimated time to reach goal:
                $duration
              
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

        //read the heads
        var head1 =ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${gain.uuid}/full"))

        // scale and draw the heads
        head1 = scaleImage(head1)

        g.drawImage(head1, image.width - head1.width -50, 65, null)

        //Draw the text boxes
        val rects = listOf(
            RoundRectangle2D.Float(0f, 0f, image.width.toFloat(), 50f, 20f, 20f),
            RoundRectangle2D.Float(10f, 65f, 450f, 250f, 20f, 20f),

        )
        g.color = Color(0f, 0f, 0f, 0.5f)
        rects.forEach { g.fill(it) }

        g.color = Color.WHITE
        val data = generateOvertakeDuration()

        g.font = font.deriveFont(22f)
        g.drawString("Average Gain for ${getMinecraftUsername(gain.uuid)}:", 20, 100)
        g.font = font.deriveFont(20f)
        g.color = Color.GREEN
        g.drawString("+${gain.gain.round(2).formatBigDecimal()}/h", 25, 120)
        g.color = Color.WHITE
        g.drawString("Progress: ${gain.full.round(2).formatBigDecimal()}/${goal.toBigDecimal().formatBigDecimal()}", 20, 160)
        val percentage = (gain.full/goal)

        val scale = percentage * 10
        println(scale)
        println(percentage)
        val progressRect = Rectangle(20, 170, 300, 30)
        g.drawRect(progressRect.x, progressRect.y, progressRect.width, progressRect.height)
        g.drawString("${(scale*10).toInt()}%", 340, progressRect.y+25)

        if (percentage >= 1){
            g.fill(progressRect)
        }
        else{
            g.fillRect(progressRect.x, progressRect.y, (progressRect.width*percentage).toInt(), progressRect.height)
        }


        g.drawString("Time to reach: ", 20, 230)
        g.drawString(data, 20, 250)

        var type = gain.type
        if (type.isEmpty()) {type = "Weight"}
        "Time to reach ${goal.toBigDecimal().formatBigDecimal()} for: $type"
        "Using Gain from the past ${gain.days} days"
        g.drawString("Time to reach ${goal.toBigDecimal().formatBigDecimal()} for: $type", 10, 20)
        g.drawString("Using Gain from the past ${gain.days} days", 10, 40)
        g.dispose()

        return image
    }
    private fun scaleImage(target: BufferedImage): BufferedImage{
        val scale = 0.35

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

    fun generateGain(): Pair<MessageEmbed, BufferedImage> {
        val embed = generateOvertakeEmbed()
        val img = generateOvertakeImage()
        //val file = File("images/image.png")
        //ImageIO.write(img, "png", file)
        println(embed.description)
        return Pair(embed, img)
    }
}

fun main() = runBlocking {
    val data = GainBody(
        "Sau_Del",
        "collection",
        "wheat",
        7
    )
    val client = LocalAPI().client
    val resp =client.post("gain"){
        contentType(ContentType.Application.Json)
        setBody(data)
    }
    val gain =GainGenerator(resp.body<GraphPlayer>(), 10_000_000_000)
    val genGain =gain.generateGain()
}
