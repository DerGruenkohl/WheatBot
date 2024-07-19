package share

import api.LocalAPI
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TrackingManager(private val discordID: Long) {

    suspend fun getTracking(): Tracking{
        val api = LocalAPI().client
        val response: HttpResponse = api.get("track/get/$discordID")
        val body = response.body<Tracking>()
        api.close()
        return body
    }
    suspend fun getSettings(): Link{
        val api = LocalAPI().client
        val response: HttpResponse = api.get("link/get/$discordID")
        val body = response.body<Link>()
        println(body)
        api.close()
        return body
    }
    suspend fun updateSetting(setting: String): Boolean{
        val api = LocalAPI().client
        val modifier = SettingModifier(
            discordID,
            setting
        )
        val response: HttpResponse = api.post("link/update") {
            contentType(ContentType.Application.Json)
            setBody(modifier)
        }
        api.close()
       return response.status.value <= 300
    }
}