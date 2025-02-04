package com.dergruenkohl.share


import com.dergruenkohl.api.ApiInstance
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import com.dergruenkohl.utils.getPairsInRange

class Leaderboard {
    @Throws(Exception::class)
    fun createUptimeLB(startIndex: Int): MessageCreateData= runBlocking {
        val messageBuilder = MessageCreateBuilder()
        val builder = EmbedBuilder()

        val response = ApiInstance.client.request("leaderboard/uptime").bodyAsText()
        val lb = Json.decodeFromString<UptimeLeaderboard>(response)
        val pairsInRange = getPairsInRange(lb.members, startIndex)

        builder.setTitle("Uptime Leaderboard from ${startIndex+1} till ${startIndex+10}")
        var counter = 1+startIndex
        pairsInRange.forEach {
            builder.addField("**$counter.** `${getMinecraftUsername(it.first)}`: ", "${it.second.hours} hours and ${it.second.mins} mins on average", false)
            counter++
        }

        messageBuilder.setEmbeds(builder.build())
        messageBuilder.addActionRow(
            Button.primary("lbleft", Emoji.fromUnicode("⬅\uFE0F")),
            Button.primary("lbright", Emoji.fromUnicode("➡\uFE0F")),
        )
        messageBuilder.addActionRow(Button.success("avg", "Leaderboard averages"))
        return@runBlocking messageBuilder.build()

    }
    fun getStartIndex(name: String): Int= runBlocking {
        val messageBuilder = MessageCreateBuilder()
        val builder = EmbedBuilder()
        val response = ApiInstance.client.request("leaderboard/uptime").bodyAsText()
        val lb = Json.decodeFromString<UptimeLeaderboard>(response)
        val uuid = getMinecraftUUID(name)

        return@runBlocking lb.members.keys.indexOfFirst { it.replace("-", "") == uuid }
    }

/*
    @Throws(Exception::class)
    fun createUptimeLB(startIndex: Int): MessageCreateData {
        val manager = UptimeSQLManager()
        val map = createLeaderboardMap(manager.getAllSavedGuilds())
        val builder = EmbedBuilder()
        val pairsInRange = getPairsInRange(map, startIndex)
        builder.setTitle("Uptime Leaderboard from ${startIndex+1} till ${startIndex+10}")
        var counter = 1+startIndex
        pairsInRange.forEach {
            builder.addField("**$counter.**${getMinecraftUsername(it.first)}: ", "~${it.second} hours/day on average", false)
            counter++
        }
        val messageBuilder = MessageCreateBuilder()
        messageBuilder.setEmbeds(builder.build())
        messageBuilder.addActionRow(
            Button.primary("lbleft", Emoji.fromUnicode("⬅\uFE0F")),
            Button.primary("lbright", Emoji.fromUnicode("➡\uFE0F"))
        )
        return messageBuilder.build()
    }
   private fun createLeaderboardMap(list: List<OuterMap>): Map<String, Int> {
        val expAverageMap = HashMap<String, Int>()
        list.forEach { outerMap: OuterMap ->
            outerMap.forEach { outerEntry: Map.Entry<String, InnerMap> ->
                expAverageMap[outerEntry.key] = (outerEntry.value.values.average()).div(9000).roundToInt()
            }
        }
        return expAverageMap.toList()
            .sortedByDescending { (_, value) -> value }
            .toMap()
    }*/
}