package com.dergruenkohl.utils.database

import com.dergruenkohl.hypixel.data.guild.Member
import com.dergruenkohl.utils.hypixelutils.Time
import com.dergruenkohl.utils.hypixelutils.getLastUptimeEntry
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.transactions.transaction

object UptimeHistoryTable : LongIdTable("uptime_history") {
    val timestamp = integer("timestamp")
    val uuid = varchar("uuid", 36)
    val time = json("time", Json, Time.serializer())
}
class UptimeHistoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UptimeHistoryEntity>(UptimeHistoryTable)
    var timestamp by UptimeHistoryTable.timestamp
    var uuid by UptimeHistoryTable.uuid
    var time by UptimeHistoryTable.time
}
object UptimeRepo {
    init {
        transaction {
            SchemaUtils.create(UptimeHistoryTable)
        }
    }
    fun Member.save(){
        val uptime = this.getLastUptimeEntry()
        if (canInsert(uptime.first, this.uuid)){
            addEntry(uptime.second, this.uuid, uptime.first)
        }
    }
    private fun canInsert(day: Int, uuid: String): Boolean = transaction {
            UptimeHistoryEntity.find {
                (UptimeHistoryTable.timestamp eq day) and (UptimeHistoryTable.uuid eq uuid)
            }.firstOrNull() ?: return@transaction true
        return@transaction false

    }

    private fun addEntry(time: Time, uuid: String, timestamp: Int){
        transaction {
            UptimeHistoryEntity.new {
                this.time = time
                this.uuid = uuid
                this.timestamp = timestamp
            }
        }
    }
    fun getUptimeEntries(uuid: String): Map<Int, Time> = transaction {
        UptimeHistoryEntity.find {
            UptimeHistoryTable.uuid eq uuid
        }.associate { it.timestamp to it.time }.toSortedMap()
    }
}