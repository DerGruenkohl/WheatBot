package com.dergruenkohl.services.server.user

import com.dergruenkohl.commands.uptime.getUptime
import com.dergruenkohl.services.server.models.*
import com.dergruenkohl.utils.database.*
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import com.dergruenkohl.utils.database.UptimeRepo
import com.dergruenkohl.utils.hypixelutils.getFarmingUptime


fun Route.userRoutes() {
    get("/profile/{ign}") {
        try {
            val username = call.parameters["ign"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "Ign parameter is required")
            )

            val uuid = getMinecraftUUID(username)

            // Get weight data using existing repository method
            val weightHistory = ProfileDataRepo.getWeightData(uuid)
            val currentWeight = weightHistory.values.lastOrNull()
            val weightData = if (currentWeight != null) {
                WeightData(
                    current = currentWeight,
                    history = weightHistory.mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                )
            } else null

            // Get latest profile data from most recent entry for current values
            val latestData = getLatestProfileData(uuid)

            // Get full historical data for all fields
            val skillsHistory = getAllSkillsHistory(uuid)
            val farmingCollectionsHistory = getAllFarmingCollectionsHistory(uuid)
            val miningCollectionsHistory = getAllMiningCollectionsHistory(uuid)
            val pestDropsHistory = getAllPestDropsHistory(uuid)

            val uptimeData = getUptime(username)?.getFarmingUptime()
            val formattedUptimeData = if (uptimeData?.isNotEmpty() == true) {
                uptimeData.mapValues { it.value.toString() }
            } else null

            // Get uptime history using existing repository method
            val uptimeHistory = UptimeRepo.getUptimeEntries(uuid)
                .mapKeys { LocalDate.fromEpochDays(it.key).toString() }
                .mapValues { it.value.toString() }

            call.respond(ProfileResponse(
                uuid = uuid,
                username = username,
                weight = weightData,
                skills = skillsHistory,
                farmingCollections = farmingCollectionsHistory,
                miningCollections = miningCollectionsHistory,
                pestDrops = pestDropsHistory,
                uptimeData = formattedUptimeData,
                uptimeHistory = uptimeHistory
            ))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

// Add these routes to your userRoutes() function
            get("/uptime/{uuid}") {
                try {
                    val uuid = call.parameters["uuid"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_uuid", "UUID parameter is required")
                    )

                    val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0

                    var uptimeData = UptimeRepo.getUptimeEntries(uuid)

                    if (days > 0) {
                        val cutoff = java.time.LocalDate.now().toEpochDay() - days
                        uptimeData = uptimeData.filterKeys { it > cutoff }
                    }

                    if (uptimeData.isEmpty()) {
                        return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("no_data", "No uptime data found for this user")
                        )
                    }

                    val username = getMinecraftUsername(uuid)
                    val formattedData = uptimeData.mapKeys {
                        LocalDate.fromEpochDays(it.key).toString()
                    }.mapValues { it.value.toString() }

                    call.respond(UptimeHistoryResponse(
                        uuid = uuid,
                        username = username,
                        uptimeHistory = formattedData
                    ))

                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("server_error", e.message ?: "Unknown error")
                    )
                }
            }

    get("/uptime/compare") {
        try {
            val uuids = call.request.queryParameters.getAll("uuid") ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuids", "At least one UUID parameter is required")
            )

            if (uuids.size > 5) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("too_many_uuids", "Maximum 5 UUIDs allowed")
                )
            }

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0

            val compareData = mutableListOf<UptimeCompareEntry>()

            for (uuid in uuids) {
                var uptimeData = UptimeRepo.getUptimeEntries(uuid)

                if (days > 0) {
                    val cutoff = java.time.LocalDate.now().toEpochDay() - days
                    uptimeData = uptimeData.filterKeys { it > cutoff }
                }

                if (uptimeData.isNotEmpty()) {
                    val username = getMinecraftUsername(uuid)
                    val formattedData = uptimeData.mapKeys {
                        LocalDate.fromEpochDays(it.key).toString()
                    }.mapValues { it.value.toMinutes() }

                    compareData.add(UptimeCompareEntry(
                        uuid = uuid,
                        username = username,
                        uptimeData = formattedData
                    ))
                }
            }

            if (compareData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No uptime data found for any of the requested users")
                )
            }

            call.respond(compareData)

        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/profile/{uuid}/weight") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0
            var weightData = ProfileDataRepo.getWeightData(uuid)

            if (days > 0) {
                weightData = weightData.filterKeys { it > java.time.LocalDate.now().toEpochDay() - days }
            }

            if (weightData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No weight data found")
                )
            }

            val response = weightData.mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
            call.respond(response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/profile/{uuid}/skills/{skill}") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val skill = call.parameters["skill"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_skill", "Skill parameter is required")
            )

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0
            var skillData = ProfileDataRepo.getSkillsDataByKey(uuid, skill)

            if (days > 0) {
                skillData = skillData.filterKeys { it > java.time.LocalDate.now().toEpochDay() - days }
            }

            if (skillData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No skill data found for $skill")
                )
            }

            val response = skillData
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value ?: 0.0 }
            call.respond(response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/profile/{uuid}/collections/farming/{collection}") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val collection = call.parameters["collection"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_collection", "Collection parameter is required")
            )

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0
            var collectionData = ProfileDataRepo.getFarmingCollectionsDataByKey(uuid, collection)

            if (days > 0) {
                collectionData = collectionData.filterKeys { it > java.time.LocalDate.now().toEpochDay() - days }
            }

            if (collectionData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No farming collection data found for $collection")
                )
            }

            val response = collectionData
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value ?: 0 }
            call.respond(response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/profile/{uuid}/collections/mining/{collection}") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val collection = call.parameters["collection"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_collection", "Collection parameter is required")
            )

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0
            var collectionData = ProfileDataRepo.getMiningCollectionsDataByKey(uuid, collection)

            if (days > 0) {
                collectionData = collectionData.filterKeys { it > java.time.LocalDate.now().toEpochDay() - days }
            }

            if (collectionData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No mining collection data found for $collection")
                )
            }

            val response = collectionData
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value ?: 0 }
            call.respond(response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/profile/{uuid}/pestdrops/{collection}") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val collection = call.parameters["collection"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_collection", "Collection parameter is required")
            )

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0
            var pestData = ProfileDataRepo.getPestDropsDataByKey(uuid, collection)

            if (days > 0) {
                pestData = pestData.filterKeys { it > java.time.LocalDate.now().toEpochDay() - days }
            }

            if (pestData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No pest drops data found for $collection")
                )
            }

            val response = pestData
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value ?: 0.0 }
            call.respond(response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/profile/{uuid}/uptime") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 0
            var uptimeData = UptimeRepo.getUptimeEntries(uuid)

            if (days > 0) {
                uptimeData = uptimeData.filterKeys { it > java.time.LocalDate.now().toEpochDay() - days }
            }

            if (uptimeData.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No uptime data found")
                )
            }

            val response = uptimeData
                .mapKeys { LocalDate.fromEpochDays(it.key).toString() }
                .mapValues { it.value.toString() }
            call.respond(response)
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }
}

// Helper function to get the most recent profile data entry
private fun getLatestProfileData(uuid: String): ProfileDataEntity? {
    return transaction {
        ProfileDataEntity.find { ProfileDataTable.uuid eq uuid }
            .orderBy(ProfileDataTable.timestamp to SortOrder.DESC)
            .firstOrNull()
    }
}
// Add these helper functions
private fun getAllSkillsHistory(uuid: String): Map<String, Map<String, Double>> {
    return transaction {
        val skillNames = setOf("carpentry", "combat", "enchanting", "farming", "foraging", "fishing", "alchemy", "taming", "mining", "runecrafting")
        skillNames.associateWith { skill ->
            ProfileDataRepo.getSkillsDataByKey(uuid, skill)
                .filterValues { it != null }
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value!! }
        }.filterValues { it.isNotEmpty() }
    }
}

private fun getAllFarmingCollectionsHistory(uuid: String): Map<String, Map<String, Long>> {
    return transaction {
        val collectionNames = setOf("carrot", "cactus", "cane", "pumpkin", "wheat", "seeds", "mushroom", "wart", "melon", "potato", "cocoa")
        collectionNames.associateWith { collection ->
            ProfileDataRepo.getFarmingCollectionsDataByKey(uuid, collection)
                .filterValues { it != null }
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value!! }
        }.filterValues { it.isNotEmpty() }
    }
}

private fun getAllMiningCollectionsHistory(uuid: String): Map<String, Map<String, Long>> {
    return transaction {
        val collectionNames = setOf("gemstone", "coal", "iron", "gold", "lapis", "redstone", "diamond", "emerald", "quartz", "obsidian", "mithril", "endstone", "umber", "sand", "tungsten", "glacite")
        collectionNames.associateWith { collection ->
            ProfileDataRepo.getMiningCollectionsDataByKey(uuid, collection)
                .filterValues { it != null }
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value!! }
        }.filterValues { it.isNotEmpty() }
    }
}

private fun getAllPestDropsHistory(uuid: String): Map<String, Map<String, Double>> {
    return transaction {
        val pestNames = setOf("carrot", "cactus", "sugarCane", "pumpkin", "wheat", "mushroom", "netherWart", "melon", "potato", "cocoaBeans")
        pestNames.associateWith { pest ->
            ProfileDataRepo.getPestDropsDataByKey(uuid, pest)
                .filterValues { it != null }
                .mapKeys { LocalDate.fromEpochDays(it.key.toInt()).toString() }
                .mapValues { it.value!! }
        }.filterValues { it.isNotEmpty() }
    }
}