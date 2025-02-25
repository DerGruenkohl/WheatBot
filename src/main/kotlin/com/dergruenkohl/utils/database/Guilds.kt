package com.dergruenkohl.utils.database

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getGuildById
import com.dergruenkohl.utils.hypixelutils.Time
import com.dergruenkohl.utils.hypixelutils.getAverageUptime
import com.dergruenkohl.hypixel.data.guild.Guild
import com.dergruenkohl.hypixel.data.guild.Member
import com.dergruenkohl.utils.database.UptimeRepo.save
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


class GuildEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<GuildEntity>(GuildTable)
    var guildId by GuildTable.guildId
    private var membersJson by GuildTable.members
    var lastUpdated by GuildTable.lastUpdated

    var members: List<Member>
        get() = Json.decodeFromString(ListSerializer(Member.serializer()), membersJson)
        set(value) {
            membersJson = Json.encodeToString(ListSerializer(Member.serializer()), value)
        }
}
class LbHistoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<LbHistoryEntity>(LbHistoryTable)
    var timestamp by LbHistoryTable.timestamp
    var time by LbHistoryTable.time
}

object GuildTable : LongIdTable() {
    val guildId = text("guildId").uniqueIndex()
    val lastUpdated = long("lastUpdated")
    val members: Column<String> = text("members")
}
object LbHistoryTable : LongIdTable("lb_history") {
    val timestamp: Column<Long> = long("timestamp")
    val time = json("time", Json, Time.serializer())
}

object GuildRepo {
    private val logger = KotlinLogging.logger {  }

    init {
        transaction {
            SchemaUtils.create(GuildTable)
            SchemaUtils.create(LbHistoryTable)
        }
    }


    fun getMembers(guildId: String): List<Member> {
        return transaction {
            GuildEntity.find { GuildTable.guildId eq guildId }.firstOrNull()?.members ?: emptyList()
        }
    }

    fun getTopMembersByFarmingUptime(page: Int): List<Pair<String, Time>> {
        val pageSize = 10
        return transaction {
            GuildEntity.all()
                .asSequence()
                .flatMap { it.members }
                .sortedByDescending { it.expHistory.values.sum() }
                .map { member -> member.uuid to member.getAverageUptime() }
                .drop((page - 1) * pageSize)
                .take(pageSize)
                .toList()
        }
    }

    fun getPageForMember(memberUuid: String): Int {
        val pageSize = 10
        val members = transaction {
            GuildEntity.all()
                .flatMap { it.members }
                .sortedByDescending { it.expHistory.values.sum() }
                .map { it.uuid }
        }
        val index = members.indexOf(memberUuid)
        return if (index == -1) -1 else (index / pageSize) + 1
    }

    suspend fun updateGuilds(){
        val guilds = getGuildsToUpdate()
        logger.info { "Updating ${guilds.size} guilds" }
        guilds.forEach {
            try {
                val guild = hypixelClient.getGuildById(it).guild?: return@forEach clearGuild(it)
                // Don't get rate limited
                delay(Random.nextLong(30, 91).seconds)
                guild.save()
            } catch (e: Exception) {
                clearGuild(it)
            }
        }
    }
    private fun clearGuild(guildId: String){
        logger.warn { "Couldnt find guild $guildId on hypixel, removing members from the database" }
        transaction {
            val guild = GuildEntity.find { GuildTable.guildId eq guildId }.firstOrNull()?: return@transaction logger.error { "Guild $guildId not found in the database" }
            guild.delete()
        }
    }

    private fun getGuildsToUpdate(): List<String> {
        val sixHoursAgo = System.currentTimeMillis() - 36.hours.inWholeMilliseconds
        return transaction {
            GuildEntity.find { GuildTable.lastUpdated lessEq sixHoursAgo }
                .map { it.guildId }
        }
    }

    fun Guild.save() {
        val guildId = this.id
        val members = this.members
        val saved = transaction {
            GuildEntity.find { GuildTable.guildId.eq (guildId) }.firstOrNull()
        }
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "Saving ${members.size} members" }
            members.forEach {
                it.save()
            }
        }
        if (saved == null){
            transaction {
                GuildEntity.new {
                    this.lastUpdated = System.currentTimeMillis()
                    this.guildId = guildId
                    this.members = members
                }}
        }else{
            transaction {
                saved.lastUpdated = System.currentTimeMillis()
                saved.members = members
            }

        }
    }
}