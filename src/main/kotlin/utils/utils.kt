package utils

import kong.unirest.json.JSONObject
import net.hypixel.api.reply.GuildReply
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.floor

@Throws(IOException::class)
fun getMinecraftUsername(uuid: String): String {
    val url = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
    val input = url.openConnection().getInputStream()
    val jsonText = IOUtils.toString(input, "UTF-8")
    val json = JSONObject(jsonText)
    return json.getString("name")
}

@Throws(IOException::class)
fun getMinecraftUUID(name: String): String {
    val url = URL("https://api.mojang.com/users/profiles/minecraft/$name")
    val input = url.openConnection().getInputStream()
    val jsonText = IOUtils.toString(input, "UTF-8")
    val json = JSONObject(jsonText)
    return json.getString("id")
}
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
