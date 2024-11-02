package share.data

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.kotlinx.kandy.dsl.continuous
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.area
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.path
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.statistics.kandy.layers.smoothLine
import org.jetbrains.kotlinx.statistics.plotting.smooth.SmoothMethod

import share.time
import utils.getMinecraftUsername

class LeaderboardPlot(val data: Map<Long, time>) {
    fun createPlot(): Plot {
        val color = Color.BLUE
        val plot = plot {
            val longTimestamps = data.keys
            val timestamps = data.keys.map {
                Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.of("America/New_York"))
                    .date
                    .toString()
            }
            val values = data.values.map { it.hours + it.mins/60f }

            x(longTimestamps)
            y(values){
                scale = continuous(min = 0f, max = 24f)
            }
            points {
                size = 3.5
                symbol = Symbol.BULLET
                this.color = Color.BLUE
            }
            area {
                this.borderLine.color = Color.BLUE
                this.fillColor = Color.LIGHT_BLUE
            }

            layout{
                title = "Uptime Leaderboard average"
                theme = Theme.DARCULA
                x.axis.name = "Date"
                y.axis.name = "Average Uptime"
                x.axis.breaksLabeled(longTimestamps.toList(), timestamps)
                //y.limits = values.first()..values.last()
            }
        }
        return plot
    }
}