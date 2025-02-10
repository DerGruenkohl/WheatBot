package com.dergruenkohl.utils.hypixelutils


import com.dergruenkohl.hypixel.data.guild.Member

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.floor


fun Member.getFarmingUptime(): LinkedHashMap<String, Time>{
    val map = this.expHistory
    val newMap = LinkedHashMap<String, Time>()
    map.forEach {
        val hours = it.value/9000f
        val hoursInt = floor(hours).toInt()
        val minutes = ((hours - hoursInt) * 60).toInt()
        val pair = Time(hoursInt, minutes)
        newMap[it.key] = pair
    }
    return newMap
}
fun Member.getLastUptimeEntry(): Pair<Int, Time> {
    val map = this.getFarmingUptime()
    val last = map.entries.last()
    val date = LocalDate.parse(last.key, DateTimeFormatter.ISO_DATE)
    return Pair(date.toEpochDay().toInt(), last.value)
}

fun Member.getAverageUptime(): Time {
    var totalhours = 0
    var totalmins = 0
    this.getFarmingUptime().forEach {
        totalmins += it.value.mins
        totalhours += it.value.hours
    }
    totalhours += floor(totalmins/60f).toInt()
    val avghrs = totalhours /7f
    val hoursInt = floor(avghrs).toInt()
    val minutes = ((avghrs - hoursInt) * 60).toInt()
    return Time(hoursInt, minutes)

}

@Serializable
data class Time(
    val hours: Int,
    val mins: Int
) : Comparable<Time> {
    override fun compareTo(other: Time): Int {
        return compareValuesBy(this, other, Time::hours, Time::mins)
    }
    override fun toString(): String {
        return "${this.hours}h, ${this.mins}m"
    }
    fun toMinutes(): Int {
        return this.hours * 60 + this.mins
    }
}