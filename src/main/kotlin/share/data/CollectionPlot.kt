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
import org.jetbrains.kotlinx.kandy.util.color.Color
import share.Collections
import share.CropWeight
import share.Player
import share.UncountedCrops
import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class CollectionPlot(val playerData: List<Player>) {

    private fun getTimestamps(): List<Long> = playerData.map { it.timestamp }
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

    fun createCollectionPlot(name: String): Plot {
        val data = playerData.map { player ->
            val property = Collections::class.memberProperties
                .find { it.name == name }!!.getter.call(player.collections) as Long
            return@map property
        }

        return createPlot(data, name)
    }
    fun createPestPlot(name: String): Plot {
        val data = playerData.map { player ->
            val property = UncountedCrops::class.memberProperties
                .find { it.name == name }!!.getter.call(player.weight.uncountedCrops) as Int
            return@map property.toLong()
        }

        return createPlot(data, "$name from pests")
    }
    fun createWeightPlot(name: String): Plot {
        val data = playerData.map { player ->
            val property = CropWeight::class.memberProperties
                .find { it.name == name }!!.getter.call(player.weight.cropWeight) as Double
            return@map property.toLong()
        }

        return createPlot(data, name)
    }
    fun createWeightPlot(): Plot {
        val data = playerData.map { player ->
            player.weight.totalWeight.toLong()
        }

        return createPlot(data, "Farming Weight")
    }



    private fun createPlot(yAxis: List<Long>, name: String): Plot {
        val xAxis = getTimestamps()
        val color = Color.BLUE
        val plot = plot {
            val timestamps = xAxis.map {
                Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.of("America/New_York"))
                    .date
                    .toString()
            }

            x(xAxis)
            y(yAxis){
                scale = continuous(min = yAxis.first(), max = yAxis.last())
            }
            points {
                size = 3.5
                symbol = Symbol.BULLET
                this.color = Color.BLUE
            }
            line {
                this.color = Color.BLUE
            }

            layout{
                title = "Very nice Graph!"
                theme = Theme.DARCULA
                x.axis.name = "Date"
                y.axis.name = name
                x.axis.breaksLabeled(xAxis.toList(), timestamps)
                y.axis.breaksLabeled(yAxis, yAxis.toStringList())
                //y.limits = values.first()..values.last()
            }
        }
        return plot
    }
}