package utils

import api.LocalAPI
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.IOException
import java.net.URL


@Throws(IOException::class)
fun getMinecraftUsername(uuid: String): String = runBlocking{
    val client = LocalAPI().client

    val url = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
    val text = client.request(url).bodyAsText()
    client.close()
    return@runBlocking Json.parseToJsonElement(text).jsonObject["name"].toString()
    //val json = JSONObject(jsonText)
   // return@runBlocking json.getString("name")
}

@Throws(IOException::class)
fun getMinecraftUUID(name: String): String = runBlocking{
    val url = URL("https://api.mojang.com/users/profiles/minecraft/$name")
    val client = LocalAPI().client
    val text = client.request(url).bodyAsText()
    client.close()
    return@runBlocking Json.parseToJsonElement(text).jsonObject["id"].toString()
   // val input = url.openConnection().getInputStream()
    //val jsonText = IOUtils.toString(input, "UTF-8")
    //val json = JSONObject(jsonText)
    //return json.getString("id")
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
