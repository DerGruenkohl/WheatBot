package com.dergruenkohl.utils.database

import com.dergruenkohl.utils.calculators.ProfileCalculator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object ProfileDataTable : LongIdTable("profile_data") {
    val uuid: Column<String> = varchar("uuid", 36)
    val timestamp = long("timestamp")
    val weight: Column<Double> = double("weight")
    val skills = json("skills", Json, MapSerializer(String.serializer(), Double.serializer()))
    val farmingCollections = json("farming_collections", Json, MapSerializer(String.serializer(), Long.serializer()))
    val miningCollections = json("mining_collections", Json, MapSerializer(String.serializer(), Long.serializer()))
    val pestDrops = json("pest_drops", Json, MapSerializer(String.serializer(), Double.serializer()))
}

class ProfileDataEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ProfileDataEntity>(ProfileDataTable)

    var uuid by ProfileDataTable.uuid
    var timestamp by ProfileDataTable.timestamp
    var weight by ProfileDataTable.weight
    var skills by ProfileDataTable.skills
    var farmingCollections by ProfileDataTable.farmingCollections
    var miningCollections by ProfileDataTable.miningCollections
    var pestDrops by ProfileDataTable.pestDrops
}

object ProfileDataRepo {
    private val logger = KotlinLogging.logger {  }
    init {
        transaction {
            SchemaUtils.create(ProfileDataTable)
        }
    }
    private fun canInsert(day: Long, uuid: String): Boolean = transaction {
        ProfileDataEntity.find {
            (ProfileDataTable.timestamp eq day) and (ProfileDataTable.uuid eq uuid)
        }.firstOrNull() ?: return@transaction true
        return@transaction false

    }

    suspend fun saveProfileData(uuid: String, calculator: ProfileCalculator) {
        val lastProfileData = transaction {
            ProfileDataEntity.find { ProfileDataTable.uuid eq uuid }
                .orderBy(ProfileDataTable.timestamp to SortOrder.DESC)
                .firstOrNull()
        }
        val today = LocalDate.now().toEpochDay()
        if (!canInsert(today, uuid)) return logger.info { "Already inserted data for $uuid on $today" }

        val weight = calculator.getWeight() ?: lastProfileData?.weight ?: 0.0
        val skills = calculator.getSkills() ?: lastProfileData?.skills ?: emptyMap()
        val farmingCollections = calculator.getFarmingCollections() ?: lastProfileData?.farmingCollections ?: emptyMap()
        val miningCollections = calculator.getMiningCollections() ?: lastProfileData?.miningCollections ?: emptyMap()
        var pestDrops = calculator.getPestDrops()
        if (pestDrops.isEmpty()) pestDrops = lastProfileData?.pestDrops ?: emptyMap()
        transaction {
            ProfileDataEntity.new {
                this.uuid = uuid
                this.timestamp = today
                this.weight = weight
                this.skills = skills
                this.farmingCollections = farmingCollections
                this.miningCollections = miningCollections
                this.pestDrops = pestDrops

            }
        }
    }
    private fun getFieldData(uuid: String, field: (ProfileDataEntity) -> Any?): Map<Long, Any?> {
        return transaction {
            ProfileDataEntity.find { ProfileDataTable.uuid eq uuid }
                .orderBy(ProfileDataTable.timestamp to SortOrder.ASC)
                .associate { it.timestamp to field(it) }
        }
    }
    fun getWeightData(uuid: String): Map<Long, Double> {
        return getFieldData(uuid) { it.weight } as Map<Long, Double>
    }
    private fun getFieldDataByKey(uuid: String, key: String, field: (ProfileDataEntity) -> Map<String, Any?>): Map<Long, Any?> {
        return transaction {
            ProfileDataEntity.find { ProfileDataTable.uuid eq uuid }
                .orderBy(ProfileDataTable.timestamp to SortOrder.ASC)
                .associate { it.timestamp to field(it)[key] }
                .toSortedMap()
        }
    }

    fun getSkillsDataByKey(uuid: String, key: String): Map<Long, Double?> {
        return getFieldDataByKey(uuid, key) { it.skills } as Map<Long, Double?>
    }

    fun getFarmingCollectionsDataByKey(uuid: String, key: String): Map<Long, Long?> {
        return getFieldDataByKey(uuid, key) { it.farmingCollections } as Map<Long, Long?>
    }

    fun getMiningCollectionsDataByKey(uuid: String, key: String): Map<Long, Long?> {
        return getFieldDataByKey(uuid, key) { it.miningCollections } as Map<Long, Long?>
    }

    fun getPestDropsDataByKey(uuid: String, key: String): Map<Long, Double?> {
        return getFieldDataByKey(uuid, key) { it.pestDrops } as Map<Long, Double?>
    }

}