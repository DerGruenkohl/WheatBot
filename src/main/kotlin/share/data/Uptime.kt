package share.data




import io.ktor.utils.io.bits.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okhttp3.internal.format
import org.jetbrains.kotlinx.kandy.letsplot.*
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.area
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.scales.guide.model.AxisPosition
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.statistics.kandy.layers.smoothLine
import org.joda.time.format.DateTimeFormatter
import share.Tracking
import utils.getMinecraftUsername
import java.io.File
import java.util.*
import kotlin.reflect.full.memberProperties

class CollectionPlot(val tracking: Tracking) {

    val timestamps = tracking.data.map {
       Instant.fromEpochMilliseconds(it.timeStamp).toLocalDateTime(TimeZone.currentSystemDefault())
           .date
           .toString()
    }.asReversed()
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

    val data = mapOf(
        "timestamp" to timestamps,
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
        val username = getMinecraftUsername(tracking.uuid)
        val plots = (data.filterKeys { it != "timestamp" }.keys).map { key ->
            val color = colorMap[key] ?: Color.BLACK
            key to plot {
                val timestamps = data["timestamp"]!!
                val values = data[key]!!
                //println("$key: $values")
                area {
                    fillColor = color
                    x(timestamps)
                    y(values)
                }
                layout{
                    title = "${key.replace("_1", "")} collection of $username"
                    theme = Theme.DARCULA
                    x.axis.name = "Time"
                    y.axis.name = "collection"

                }
            }
        }.toMap()
        return plots[name]!!
    }


}