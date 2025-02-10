package com.dergruenkohl.utils

import com.dergruenkohl.api.client
import dev.minn.jda.ktx.messages.Embed
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.datetime.Clock

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class MojangResponse(
    val name: String,
    val id: String
)
private val logger = KotlinLogging.logger {}
private val cache = ConcurrentHashMap<String, CachedMojangResponse>()

private data class CachedMojangResponse(
    val response: MojangResponse,
    val timestamp: Instant
)

private suspend fun getMojangResponse(identifier: String): MojangResponse {
    val now = Instant.now()
    val cachedResponse = cache[identifier]

    if (cachedResponse != null && Duration.between(cachedResponse.timestamp, now).toHours() < 1) {
        return cachedResponse.response
    }

    logger.info { "Getting Mojang response for $identifier" }
    val response = client.get("https://mowojang.matdoes.dev/$identifier").body<MojangResponse>()
    cache[identifier] = CachedMojangResponse(response, now)
    return response
}

suspend fun getMinecraftUsername(uuid: String): String = getMojangResponse(uuid).name

suspend fun getMinecraftUUID(name: String): String = getMojangResponse(name).id

fun <K, V> getPairsInRange(map: Map<K, V>, startIndex: Int, range: Int = 10): List<Pair<K, V>> {
    // Convert map entries to a list
    val entriesList = map.entries.toList()
    // Calculate the end index
    val endIndex = (startIndex + range).coerceAtMost(entriesList.size)
    // Get the sublist of the desired range
    return if (startIndex in entriesList.indices) {
        entriesList.subList(startIndex, endIndex).map { it.toPair() }
    } else {
        emptyList()
    }
}
fun getLoading(): MessageEmbed = Embed {
    title = "Loading..."
    description = "Please wait a bit"
    image = getMeow()
}

fun getMeow() : String{
    // The Cat API endpoint for random cat images
    val apiUrl = "https://api.thecatapi.com/v1/images/search"

    // Create a URL object with the API endpoint
    val url = URL(apiUrl)

    // Open a connection to the URL
    val connection = url.openConnection() as HttpURLConnection
    connection.addRequestProperty("x-api-key", "live_ZooEr4cnhvcy325C4QwSyRA9lhxJAQq5FBztGXN06VeH1UqITCSHFJSZTVqQtS0j")
    connection.requestMethod = "GET"

    // Get the response code
    val responseCode = connection.responseCode

    // Check if the request was successful (status code 200)
    if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()

        // Parse the JSON response to extract the image URL
        val imageUrl = response.toString().split("\"url\":\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("\",\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        // Print the image URL
        println("Random Cat Image URL: $imageUrl")
        return imageUrl
    } else {
        println("Error: Unable to fetch cat image. Response Code: $responseCode")
    }

    // Close the connection
    connection.disconnect()
    return "https://cdn2.thecatapi.com/images/QUdOiX2hP.jpg"
}
fun toLevel(skill: Double): Int{
    val levelThresholds = listOf(
        0L, 50L, 175L, 375L, 675L, 1175L, 1925L, 2925L, 4425L, 6425L,
        9925L, 14925L, 22425L, 32425L, 47425L, 67425L, 97425L, 147425L,
        222425L, 322425L, 522425L, 822425L, 1222425L, 1722425L, 2322425L,
        3022425L, 3822425L, 4722425L, 5722425L, 6822425L, 8022425L, 9322425L,
        10722425L, 12222425L, 13822425L, 15522425L, 17322425L, 19222425L,
        21222425L, 23322425L, 25522425L, 27822425L, 30222425L, 32722425L,
        35322425L, 38072425L, 40972425L, 44072425L, 47472425L, 51172425L,
        55172425L, 59472425L, 64072425L, 68972425L, 74172425L, 79672425L,
        85472425L, 91572425L, 97972425L, 104672425L, 111672425L
    )

    // Find the corresponding level
    for ((level, threshold) in levelThresholds.withIndex()) {
        if (skill < threshold) {
            return level - 1
        }
    }
    // If the XP is greater than the highest threshold, return the max level
    return levelThresholds.size - 1
}