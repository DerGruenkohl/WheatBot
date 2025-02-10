package com.dergruenkohl.commands.data

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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


fun createPlot(data: Map<Long, Number>, plotTitle: String, type: String): Plot{
    val data = data.filter { it.value.toLong() > 0 }

    val xAxis = data.keys
    val yAxis = data.values.map { it.toLong() }
    return plot {
        val timestamps = xAxis.map {
            LocalDate.fromEpochDays(it.toInt()).toString()

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
            title = plotTitle
            theme = Theme.DARCULA
            x.axis.name = "Date"
            y.axis.name = type
            x.axis.breaksLabeled(xAxis.toList(), timestamps)
            y.axis.breaksLabeled(yAxis, yAxis.toStringList())
        }
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