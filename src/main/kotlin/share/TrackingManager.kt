package share

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TrackingManager(private val discordID: Long) {

    suspend fun getTracking(): List<Player>{
        val api = LocalAPI().client
        val response: HttpResponse = api.get("track/$discordID")
        api.close()
        val body = response.body<List<Player>>()
        return body
    }
    suspend fun getSettings(): Link?{
        val api = LocalAPI().client
        val response: HttpResponse = api.get("link/get/$discordID")
        if (response.status.value >= 300){return null}
        val body = response.body<Link>()
        println(body)
        api.close()
        return body
    }
    suspend fun setColor(color: String): Boolean{
        val api = LocalAPI().client
        val settings = getSettings()?.let {
            val new =it.copy(settings = it.settings.copy(textColor = color))
            val response: HttpResponse = api.post("link/update") {
                contentType(ContentType.Application.Json)
                setBody(new)
            }
            println(response.request.content.toString())
            api.close()
            return response.status.value <= 300
        }
        return false
    }
    suspend fun updateSetting(setting: String): Boolean{
        val api = LocalAPI().client
        getSettings()?.let { link ->
            val new = link.copy(
                settings = when (setting) {
                    "track" -> link.settings.copy(track = !link.settings.track)
                    "pestGain" -> link.settings.copy(pestGain = !link.settings.pestGain)
                    "collectionGain" -> link.settings.copy(collectionGain = !link.settings.collectionGain)
                    "uptime" -> link.settings.copy(uptime = !link.settings.uptime)
                    "customImage" -> link.settings.copy(customImage = !link.settings.customImage)
                    else -> throw IllegalArgumentException("Invalid setting name: $setting")
                }
            )
            val response: HttpResponse = api.post("link/update") {
                contentType(ContentType.Application.Json)
                setBody(new)
            }
            println(response.request.content.toString())
            api.close()
            return response.status.value <= 300
        }
        return false
    }
}