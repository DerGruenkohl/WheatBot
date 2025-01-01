package utils

import api.LocalAPI
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


@Throws(IOException::class)
fun getMinecraftUsername(uuid: String): String = runBlocking{
    val client = LocalAPI().client

    val url = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
    val text = client.request(url).bodyAsText()
    client.close()
    return@runBlocking Json.parseToJsonElement(text).jsonObject["name"].toString().replace("\"", "")
    //val json = JSONObject(jsonText)
   // return@runBlocking json.getString("name")
}

@Throws(IOException::class)
fun getMinecraftUUID(name: String): String = runBlocking{
    val url = URL("https://api.mojang.com/users/profiles/minecraft/$name")
    val client = LocalAPI().client
    val text = client.request(url).bodyAsText()
    client.close()
    return@runBlocking Json.parseToJsonElement(text).jsonObject["id"].toString().replace("\"", "")
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
