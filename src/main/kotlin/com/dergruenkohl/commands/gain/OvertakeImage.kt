package com.dergruenkohl.commands.gain

import com.dergruenkohl.config.Data
import com.dergruenkohl.utils.getMinecraftUsername
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.canvas.drawables.DrawableImage
import com.sksamuel.scrimage.canvas.drawables.FilledRect
import com.sksamuel.scrimage.canvas.drawables.Text
import dev.freya02.botcommands.jda.ktx.messages.Embed
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.AffineTransform
import java.awt.geom.RoundRectangle2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import javax.imageio.IIOException
import javax.imageio.ImageIO


class OvertakeImage(private val graph: OutgoingGraph) {
    companion object{
        private val logger = KotlinLogging.logger {  }

        private fun Double.round(decimals: Int): BigDecimal {
            return BigDecimal(this).setScale(decimals, RoundingMode.HALF_EVEN)
        }
        private fun BigDecimal.formatBigDecimal(): String {
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
        private val baseImage = ImmutableImage.loader().fromPath(Data.folder.resolve("images/wheatField.jpg"))
        private val boldFont = Data.folder.resolve("fonts/JetBrainsMono-ExtraBold.ttf").toFile().apply {
            logger.info { "File: ${this.name}, size: ${this.totalSpace}" }
        }
        private val font = Font.createFont(Font.TRUETYPE_FONT, boldFont)
        private val WIDTH = baseImage.width
        private val HEIGHT = baseImage.height
        private val blackBox = FilledRect(
            0, 0, WIDTH, HEIGHT
        ) {
            it.color = Color(0f, 0f, 0f, 0.2f)
        }
        private val steve1 = ImmutableImage.loader().fromPath(Data.folder.resolve("images/steve_face.png"))

        private fun getSkin(uuid: String) =
            try {
                val img = ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${uuid}/face"))
                ImmutableImage.wrapAwt(img)
            } catch (e: Exception) {
                steve1
            }.scale(0.3)


    }
    private val canvas = baseImage
        .copy()
        .toCanvas()


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
    private suspend fun generateOvertakeEmbed(): MessageEmbed {
        return Embed {
            title = "Overtake for $category"
            description = """
                **Using Gain from the past ${graph.p1.days} days**
                
                Player1: `${getMinecraftUsername(graph.p1.uuid)}`
                    Total: ${graph.p1.full.toBigDecimal().formatBigDecimal()}
                    AvgGain: ${graph.p1.gain.toBigDecimal().formatBigDecimal()}/h
                
                Player2 `${getMinecraftUsername(graph.p2.uuid)}`
                    Total: ${graph.p2.full.toBigDecimal().formatBigDecimal()}
                    AvgGain: ${graph.p2.gain.toBigDecimal().formatBigDecimal()}/h
                    
                Estimated time to overtake `${getMinecraftUsername(graph.p2.uuid)}`:
                    ${generateOvertakeDuration().first}
                    ${generateOvertakeDuration().second?.let { "Estimated total on overtake: $it" }}
            """.trimIndent()
            image = "attachment://overtake.png"
            footer {
                this.name = "Wheat! Powered by elitebot.dev"
            }
        }


    }
    private val type = graph.p1.type.ifEmpty { "weight" }
    private val titleText = "Overtake for $type. Using Gain from the past ${graph.p1.days} days"
    private val title = Text(
        titleText,
        getCenteredStringCoordinates(
            titleText,
            WIDTH/2,
            30,
            font.deriveFont(20f)
        ).first,
        30
    ) {
        it.font = font.deriveFont(20f)
        it.color = Color.WHITE
        it.setAntiAlias(true)
    }
    private val skin1 = DrawableImage(
        getSkin(graph.p1.uuid).flipX(),
        50,
        80
    ){
        it.setAntiAlias(true)
    }
    private val skin2Image = getSkin(graph.p2.uuid)
    private val skin2 = DrawableImage(
        skin2Image,
        WIDTH-50-skin2Image.width,
        80
    ){
        it.setAntiAlias(true)
    }

    private val ign1 = runBlocking { getMinecraftUsername(graph.p1.uuid) }
    private val ign2 = runBlocking { getMinecraftUsername(graph.p2.uuid) }

    private val username1 = Text(
        runBlocking {
            getMinecraftUsername(graph.p1.uuid)
        },
        getCenteredStringCoordinates(
            ign1,
            50 + skin2Image.width/2,
            75,
            font.deriveFont(18f)
        ).first,
        75
    ) {
        it.font = font.deriveFont(18f)
        it.color = Color.WHITE
        it.setAntiAlias(true)
    }

    private val username2 = Text(
        ign2,
        getCenteredStringCoordinates(
            ign2,
            WIDTH - 50 - skin2Image.width/2,
            75,
            font.deriveFont(18f)
        ).first,
        75
    ) {
        it.font = font.deriveFont(18f)
        it.color = Color.WHITE
        it.setAntiAlias(true)
    }
    private val overtakeTextString = "Overtake in:"
    private val overtakeText1 = Text(
        overtakeTextString,
        getCenteredStringCoordinates(
            overtakeTextString,
            WIDTH/2,
            150,
            font.deriveFont(18f)
        ).first,
        150,
    ){
        it.font = font.deriveFont(18f)
        it.color = Color.WHITE
        it.setAntiAlias(true)
    }
    private val data = generateOvertakeDuration()

    private val overtakeTextString2 = data.first
    private val overtakeText2 = Text(
        overtakeTextString2,
        getCenteredStringCoordinates(
            overtakeTextString2,
            WIDTH/2,
            175,
            font.deriveFont(18f)
        ).first,
        175
    ){
        it.font = font.deriveFont(18f)
        it.color = Color.WHITE
        it.setAntiAlias(true)
    }
    private val overtakeTextString3 = "Estimated total: ${data.second?:"You have to farm more :)"}"
    private val overtakeText3 = Text(
        overtakeTextString3,
        getCenteredStringCoordinates(
            overtakeTextString3,
            WIDTH/2,
            200,
            font.deriveFont(18f)
        ).first,
        200
    ){
        it.font = font.deriveFont(18f)
        it.color = Color.WHITE
        it.setAntiAlias(true)
    }

    private fun drawNewOvertakeImage(): ImmutableImage{
        canvas.drawInPlace(
            blackBox,
            title,
            skin1,
            skin2,
            username1,
            username2,
            overtakeText1,
            overtakeText2,
            overtakeText3
        )
        return canvas.image
    }
    fun getCenteredStringCoordinates(text: String, centerX: Int, centerY: Int, font: Font): Pair<Int, Int> {
        val g = baseImage.awt().graphics
        // Get the FontMetrics
        val metrics: FontMetrics = g.getFontMetrics(font)
        // Determine the width and height of the text
        val textWidth = metrics.stringWidth(text)
        val textHeight = metrics.height
        // Calculate the top-left corner coordinates
        val x = centerX - textWidth / 2
        val y = centerY - textHeight / 2 + metrics.ascent
        return Pair(x, y)
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

    private suspend fun generateOvertakeImage(): BufferedImage {
        val baseImage = javaClass.getResourceAsStream("/wheatField.jpg")
        val image = ImageIO.read(baseImage)

        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)


        var head1 = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
        var head2 = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
        //read the heads
        try {
            head1 =ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${graph.p1.uuid}/face") )
            head2 =ImageIO.read(URL("https://starlightskins.lunareclipse.studio/render/isometric/${graph.p2.uuid}/face"))

            // Flip the image horizontally
            val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
            tx.translate(-head1.getWidth(null).toDouble(), 0.0)
            val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
            // scale and draw the heads
            head1 = scaleImage(op.filter(head1, null))
            head2 = scaleImage(head2)

            g.drawImage(head1, 10+head2.width/2, 180- head1.height/2, null)
            g.drawImage(head2, image.width - head2.width -50, 180-head2.height/2, null)
        }catch (e: IIOException){
            logger.info { "Skin api died" }
        }
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

    suspend fun generateOvertake(): Pair<MessageEmbed, ImmutableImage> {
        val embed = generateOvertakeEmbed()
        val img = generateOvertakeImage()
        return Pair(embed, ImmutableImage.wrapAwt(img))
    }
}