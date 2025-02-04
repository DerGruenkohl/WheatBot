package com.dergruenkohl.share

import com.dergruenkohl.api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import com.dergruenkohl.utils.getMinecraftUsername
import java.time.temporal.WeekFields
import java.util.*

enum class Types {
    TOTAL, THIRTY, SEVEN, WEEKS
}

class HistoricalUptime(private val name: String, private val type: Types) {

    @Throws(Exception::class)
    private suspend fun getMember(): Member {
        val member = client.request("uptime/historical/$name").body<Member>()
        return member
    }
    private fun memberToDataFrame(member: Member): AnyFrame {
        val dates = mutableListOf<String>()
        val hours = mutableListOf<Double>()
        val player = mutableListOf<String>()

        for ((month, timeEntry) in member.expHistory) {
            dates.add(LocalDate.fromEpochDays(month.toInt()).toString())
            hours.add(timeEntry.toHours())
            }
        player.add(getMinecraftUsername(member.uuid))

        return dataFrameOf(
            "date" to dates,
            "uptime" to hours,
            "Player" to player
        )
    }
    @Throws(Exception::class)
    suspend fun createPlot(): Plot{
        val member = getMember()
        //val frame = memberToDataFrame(member)
        var xs = listOf<Long>()
        var ys = listOf<Double>()
        var ts = listOf<String>()
        when(type){
            Types.TOTAL -> {
                ys = member.expHistory.values.map { it.toHours() }
                xs = member.expHistory.keys.toList()
                ts = xs.map { LocalDate.fromEpochDays(it.toInt()).toString()}

            }
            Types.THIRTY -> {
                ys = member.expHistory.values.map { it.toHours() }.take(30)
                xs = member.expHistory.keys.toList().take(30)
                ts = xs.map { LocalDate.fromEpochDays(it.toInt()).toString()}
            }
            Types.SEVEN -> {
                ys = member.expHistory.values.map { it.toHours() }.take(7)
                xs = member.expHistory.keys.toList().takeLast(7)
                ts = xs.map { LocalDate.fromEpochDays(it.toInt()).toString()}
            }
            Types.WEEKS -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val map = member.expHistory.map {
                    it.key to it.value.toHours()
                }.toMap()

                val groupedMap = map.entries.groupBy { entry ->
                    // Convert EpochDay to LocalDate and get the week string (Year-WeekNumber)
                    val date = LocalDate.fromEpochDays(entry.key.toInt()).toJavaLocalDate()
                    val weekOfYear = date.get(weekFields.weekOfYear())
                    weekOfYear
                }
                    .mapValues { (_, entries) ->
                        // Calculate the average value for each group
                        entries.map { it.value }.average()
                    }

                ys = groupedMap.values.toList()
                xs = groupedMap.keys.map { it.toLong() }
                ts = xs.map {it.toString()}
            }
        }
        return plot{

            line {
                x(xs)
                y(ys)
                color = Color.rgb(117, 0, 0)
                width = 2.5
            }
            points {
                x(xs)
                y(ys)
                color = Color.rgb(117, 0, 0)
            }
            layout {
                this@plot.y.axis.name = "Uptime in hours"
                x.axis.breaksLabeled(xs, ts)
                title = "Historical Uptime"
                theme = Theme.HIGH_CONTRAST_DARK
            }
        }
    }
}