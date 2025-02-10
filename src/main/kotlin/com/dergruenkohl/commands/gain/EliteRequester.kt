package com.dergruenkohl.commands.gain

import com.dergruenkohl.api.client
import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getSelectedProfileID
import io.ktor.client.call.*
import io.ktor.client.request.*



suspend fun getSkillGraph(uuid: String, days: Int): IncomingSkills? {
    val profileID = hypixelClient.getSelectedProfileID(uuid)?: return null
    return client.request("https://api.elitebot.dev/graph/${uuid}/${profileID.replace("-", "")}/skills?days=$days&perDay=1").body()
}
suspend fun getCropGraph(uuid: String, days: Int): IncomingCollection? {
    val profileID = hypixelClient.getSelectedProfileID(uuid)?: return null
    return client.request("https://api.elitebot.dev/graph/${uuid}/${profileID.replace("-", "")}/crops?days=$days&perDay=1").body()
}