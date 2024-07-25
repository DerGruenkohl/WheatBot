package share.data




import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.kotlinx.kandy.dsl.continuous
import org.jetbrains.kotlinx.kandy.letsplot.*
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.area
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.translator.toLetsPlot
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.statistics.kandy.layers.smoothLine
import org.jetbrains.kotlinx.statistics.kandy.stattransform.statSmooth
import org.jetbrains.kotlinx.statistics.plotting.smooth.SmoothMethod
import share.Tracking
import utils.getMinecraftUsername
import kotlin.collections.List

class CollectionPlot(val tracking: Tracking) {

    val timestamps = tracking.data.map {
       Instant.fromEpochMilliseconds(it.timeStamp).toLocalDateTime(TimeZone.currentSystemDefault())
           .date
           .toString()
    }
    val longTimestamps = tracking.data.map { it.timeStamp }
    val carrotCollections = tracking.data.map { it.collections.carrot }
    val cactusCollections = tracking.data.map { it.collections.cactus }
    val caneCollections = tracking.data.map { it.collections.cane }
    val pumpkinCollections = tracking.data.map { it.collections.pumpkin }
    val wheatCollections = tracking.data.map { it.collections.wheat}
    val seedsCollections = tracking.data.map { it.collections.seeds }
    val mushroomCollections = tracking.data.map { it.collections.mushroom }
    val wartCollections = tracking.data.map { it.collections.wart }
    val melonCollections = tracking.data.map { it.collections.melon }
    val potatoCollections = tracking.data.map { it.collections.potato }
    val pestNames = tracking.data.flatMap { it.pest.map { pest -> pest.pestName } }.distinct()
    val pestData = pestNames.associateWith { pestName ->
        tracking.data.map { data ->
            data.pest.find { it.pestName == pestName }?.collection ?: 0L
        }
    }
    fun Long.formatLong(): String {
        return if (this >= 1000000000){
            val billions = this / 1000000000.0
            String.format("%.3fB", billions)
        }
        else if (this >= 1000000) {
            val millions = this / 1000000.0
            String.format("%.3fM", millions)
        } else if (this >= 1000) {
            val thousands = this / 1000.0
            String.format("%.3fk", thousands)
        } else {
            this.toString()
        }
    }
    fun List<Long>.toStringList(): List<String>{
        return this.map { it.formatLong() }
    }

    val data = mapOf(
       // "timestamp" to timestamps,
        "carrot" to carrotCollections,
        "cactus" to cactusCollections,
        "cane" to caneCollections,
        "pumpkin" to pumpkinCollections,
        "wheat" to wheatCollections,
        "seeds" to seedsCollections,
        "mushroom" to mushroomCollections,
        "wart" to wartCollections,
        "melon" to melonCollections,
        "potato" to potatoCollections
    ) + pestData
    val colorMap = mapOf(
        "carrot" to Color.ORANGE,
        "cactus" to Color.GREEN,
        "cane" to Color.LIGHT_GREEN,
        "pumpkin" to Color.ORANGE,
        "wheat" to Color.YELLOW,
        "seeds" to Color.YELLOW,
        "mushroom" to Color.rgb(180, 163, 146),
        "wart" to Color.RED,
        "melon" to Color.rgb(255, 106, 106),
        "potato" to Color.rgb(255, 194, 132)
    )+ pestNames.associateWith { Color.BLUE }

    fun createPlot(name: String): Plot{
        val color = colorMap[name] ?: Color.BLACK
        val username = getMinecraftUsername(tracking.uuid)
        val plot = plot {
                val timestamps = longTimestamps
                val values = data[name]!!

            x(timestamps)
            y(values){
                scale = continuous(min = values.first(), max = values.last())
            }
                points {
                    size = 3.5
                    symbol = Symbol.BULLET
                    this.color = Color.BLUE
                }
                line {
                    this.color = color
                }
               /* smoothLine(longTimestamps, values, method = SmoothMethod.LOESS(span = 0.3)) {
                    this.color = color

                }*/
                layout{
                    title = "${name.replace("_1", "")} collection of $username"
                    theme = Theme.DARCULA
                    x.axis.name = "Time"
                    y.axis.name = "collection"
                    x.axis.breaksLabeled(longTimestamps, this@CollectionPlot.timestamps)
                    y.axis.breaksLabeled(values, values.toStringList())
                    //y.limits = values.first()..values.last()
                }
            }
        return plot
    }


}