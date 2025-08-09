package com.dergruenkohl.services.server.lb

import com.dergruenkohl.services.server.models.*
import com.dergruenkohl.utils.database.GuildRepo
import com.dergruenkohl.utils.getUsernames
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.compareTo
import kotlin.text.get
import kotlin.toString

fun Route.lbRoutes() {
    get("/uptime") {
        try {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = 10

            val members = GuildRepo.getTopMembersByFarmingUptime(page)

            if (members.isEmpty()) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("no_data", "No leaderboard data found")
                )
            }

            // Get usernames for all UUIDs
            val uuids = members.map { it.first }
            val usernames = getUsernames(uuids)

            val entries = members.mapIndexed { index, (uuid, uptime) ->
                val rank = (page - 1) * pageSize + index + 1
                val username = usernames.getOrNull(index) ?: "Unknown"
                LeaderboardEntry(
                    rank = rank,
                    uuid = uuid,
                    username = username,
                    uptime = uptime.toString()
                )
            }

            val totalPages = GuildRepo.getLbSize() / pageSize + 1

            call.respond(
                UptimeLeaderboardResponse(
                    page = page,
                    totalPages = totalPages,
                    entries = entries
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }

    get("/uptime/search/{uuid}") {
        try {
            val uuid = call.parameters["uuid"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("missing_uuid", "UUID parameter is required")
            )

            val page = GuildRepo.getPageForMember(uuid)
            if (page == -1) {
                return@get call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse("player_not_found", "Player not found in leaderboard")
                )
            }

            call.respond(mapOf("page" to page))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("server_error", e.message ?: "Unknown error")
            )
        }
    }
}