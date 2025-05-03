package com.dergruenkohl.utils.database

import com.dergruenkohl.WheatBot
import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getGuildById
import com.dergruenkohl.hypixel.data.guild.Guild
import com.dergruenkohl.hypixel.data.guild.Member
import com.dergruenkohl.utils.database.UptimeRepo.save
import com.dergruenkohl.utils.hypixelutils.Time
import com.dergruenkohl.utils.hypixelutils.getAverageUptime
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
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
    private val logger = KotlinLogging.logger {}

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

    suspend fun updateGuilds() {
        val guilds = getGuildsToUpdate()
        logger.info { "Updating ${guilds.size} guilds" }
        guilds.forEach {
            try {
                val guild = hypixelClient.getGuildById(it).guild ?: return@forEach clearGuild(it)
                delay(Random.nextLong(30, 91).seconds)
                guild.save()
            } catch (e: Exception) {
                logger.error { e }
                clearGuild(it)
            }
        }
    }
    private val guildMarker =
            Cache.Builder<String, Boolean>()
                    .expireAfterWrite(2.days)
                    .build()
    private fun clearGuild(guildId: String) {
        guildMarker.get(guildId)?.run {
            logger.warn { "Guild $guildId already marked for deletion, removing permanently" }
            transaction {
                val guild =
                        GuildEntity.find { GuildTable.guildId eq guildId }.firstOrNull()
                                ?: return@transaction logger.error {
                                    "Guild $guildId not found in the database"
                                }
                guild.delete()
            }
            return
        }
        logger.warn { "Couldnt find guild $guildId on hypixel. Marking for deletion" }
        guildMarker.put(guildId, false)
    }

    private fun getGuildsToUpdate(): List<String> {
        val sixHoursAgo = System.currentTimeMillis() - 36.hours.inWholeMilliseconds
        return transaction {
            GuildEntity.find { GuildTable.lastUpdated lessEq sixHoursAgo }.map { it.guildId }
        }
    }

    fun Guild.save() {
        val guildId = this.id
        val members = this.members
        val saved = transaction {
            GuildEntity.find { GuildTable.guildId.eq(guildId) }.firstOrNull()
        }
        WheatBot.IO.launch {
            logger.info { "Saving ${members.size} members" }
            members.forEach {
                it.save()
            }
        }
        if (saved == null) {
            transaction {
                GuildEntity.new {
                    this.lastUpdated = System.currentTimeMillis()
                    this.guildId = guildId
                    this.members = members
                }
            }
        } else {
            transaction {
                saved.lastUpdated = System.currentTimeMillis()
                saved.members = members
            }
        }
    }
}
