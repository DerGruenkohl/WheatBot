package com.dergruenkohl.share.data


import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.kotlinx.kandy.dsl.continuous
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import com.dergruenkohl.share.time

class LeaderboardPlot(val data: Map<Long, time>) {
    fun createPlot(): Plot {
       /* return plot{
            val xs = data.keys
            val timestamps = data.keys.map {
                Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.of("America/New_York"))
                    .date
                    .toString()
            }
            val ys = data.values.map { it.hours + it.mins/60f }

            statSmooth(xs, ys, method = SmoothMethod.LOESS(0.2), smootherPointCount = 150) {
                line {
                    x(Stat.x)
                    y(Stat.y)
                }
            }
            layout{
                title = "Uptime Leaderboard average"
                theme = Theme.HIGH_CONTRAST_DARK
                x.axis.name = "Date"
                y.axis.name = "Average Uptime in hours"
                x.axis.breaksLabeled(xs.toList(), timestamps)
                //y.limits = values.first()..values.last()
            }
        }*/
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
            line {
                this.color = Color.BLUE
            }

            layout{
                title = "Uptime Leaderboard average"
                theme = Theme.HIGH_CONTRAST_DARK
                x.axis.name = "Date"
                y.axis.name = "Average Uptime in hours"
                x.axis.breaksLabeled(longTimestamps.toList(), timestamps)
                //y.limits = values.first()..values.last()
            }
        }
        return plot
    }
}