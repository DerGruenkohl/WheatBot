package com.dergruenkohl.commands.gain

import com.dergruenkohl.api.client
import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getSelectedProfileID
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlin.time.Duration.Companion.minutes


private val SkillCache = Cache.Builder<Pair<String, Int>, IncomingSkills>()
    .expireAfterWrite(60.minutes)
    .build()
private val CropCache = Cache.Builder<Pair<String, Int>, IncomingCollection>()
    .expireAfterWrite(60.minutes)
    .build()

suspend fun getSkillGraph(uuid: String, days: Int): IncomingSkills? {
    val pair = Pair(uuid, days)
    SkillCache.get(pair)?.let {
        return it
    }
    val profileID = hypixelClient.getSelectedProfileID(uuid)?: return null
    val reply = client.request("https://api.elitebot.dev/graph/${uuid}/${profileID.replace("-", "")}/skills?days=$days&perDay=1").body<IncomingSkills>()
    SkillCache.put(pair, reply)
    return reply
}
suspend fun getCropGraph(uuid: String, days: Int): IncomingCollection? {
    val pair = Pair(uuid, days)
    CropCache.get(pair)?.let {
        return it
    }
    val profileID = hypixelClient.getSelectedProfileID(uuid)?: return null
    val reply = client.request("https://api.elitebot.dev/graph/${uuid}/${profileID.replace("-", "")}/crops?days=$days&perDay=1").body<IncomingCollection>()
    CropCache.put(pair, reply)
    return reply
}