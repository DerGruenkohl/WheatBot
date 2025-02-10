package com.dergruenkohl.utils.calculators

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.getProfileMember
import com.dergruenkohl.utils.database.Link
import com.dergruenkohl.hypixel.data.profile.ProfileMember
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProfileCalculator(private val link: Link) {
    private val logger = KotlinLogging.logger {  }
    private val pestDrops = mapOf(
        "mite" to 16_722.29,
        "cricket" to 27_637.94,
        "moth" to 24_422.40,
        "worm" to 41_805.71,
        "slug" to 8_361.14,
        "beetle" to 24_512.00,
        "locust" to 27_817.14,
        "rat" to 8_361.14,
        "mosquito" to 16_722.29,
        "fly" to 8_361.14,
    )
    private val removedCollection = mapOf(
        "mite" to 1_778.36,
        "cricket" to 2_554.51,
        "moth" to 1_284.30,
        "worm" to 966.89,
        "slug" to 757.16,
        "beetle" to 3_725.63,
        "locust" to 2_873.50,
        "rat" to 63.87,
        "mosquito" to 93.19,
        "fly" to 0.0,
    )
    private val weights = mapOf(
        "cactus" to 178_730.65,
        "carrot" to 300_000.00,
        "cocoa" to 276_733.75,
        "melon" to 488_435.88,
        "mushroom" to 90_944.27,
        "netherwart" to 248_606.81,
        "potato" to 298_328.17,
        "pumpkin" to 99_236.12,
        "sugarcane" to 198_885.45,
        "wheat" to 100_000.00,
    )

    private suspend fun getPestData(): Map<String, Int>? {
        getData()
        return (data?.bestiary?.kills?: return null )
            .jsonObject
            .filter { it.key.contains("pest") }
            .map { it.key.split("_")[1] to it.value.jsonPrimitive.int }
            .toMap()
    }
    private var pests: Map<String, Int> = mapOf()
    private suspend fun retrievePests(){
        if (pests.isNotEmpty()) return
        pests = getPestData()?: return
    }
    private var data: ProfileMember? = null
    private suspend fun getData(){
        if (data != null) return
        try {
            data = hypixelClient.getProfileMember(link.uuid, link.settings.profileID)
        } catch (e: Exception) {
            logger.warn { e }
        }
    }

    private suspend fun getCollectionData(): Map<String, Double>? {
        retrievePests()
        getData()
        val collections = data?.collections?: return null
        return mapOf(
            "cactus" to calculateWeight("cactus", collections.cactus, "mite"),
            "carrot" to calculateWeight("carrot", collections.carrot, "cricket"),
            "cocoa" to calculateWeight("cocoa", collections.cocoabeans, "moth"),
            "melon" to calculateWeight("melon", collections.melon, "worm"),
            "mushroom" to calculateWeight("mushroom", collections.mushroom, "slug"),
            "netherwart" to calculateWeight("netherwart", collections.netherwart, "beetle"),
            "potato" to calculateWeight("potato", collections.potato, "locust"),
            "pumpkin" to calculateWeight("pumpkin", collections.pumpkin, "rat"),
            "sugarcane" to calculateWeight("sugarcane", collections.sugarcane, "mosquito"),
            "wheat" to calculateWeight("wheat", collections.wheat, "fly"),
        )
    }

    private fun calculateWeight(name: String, amount: Long, pestName: String): Double {
        val weights = weights[name]?: return 0.0
        val pest = pests[pestName]?: return 0.0
        val pestDrop = removedCollection[pestName]?: return 0.0
        val toremove = pest*pestDrop

        return (amount-toremove) /weights
    }
    suspend fun getPestDrops(): Map<String, Double> {
        retrievePests()
        return pests.map {
            val drops = pestDrops[it.key]?: return@map it.key to 0.0
            it.key to drops*it.value
        }.toMap().filterNot { it.key == "mouse" }

    }
    suspend fun getWeight(): Double?{
        val collectionData = getCollectionData()?.values?.sum()?: return null
        return collectionData
    }
    suspend fun getSkills(): Map<String, Double>?{
        getData()
        val skills = data?.playerData?.skills?: return null
        return mapOf(
            "farming" to skills.farming,
            "foraging" to skills.foraging,
            "mining" to skills.mining,
            "combat" to skills.combat,
            "fishing" to skills.fishing,
            "enchanting" to skills.enchanting,
            "alchemy" to skills.alchemy,
            "taming" to skills.taming,
            "carpentry" to skills.carpentry,
            "runecrafting" to skills.runecrafting,
        )
    }
    suspend fun getFarmingCollections(): Map<String, Long>? {
        getData()
        val collections = data?.collections?: return null
        return mapOf(
            "cactus" to collections.cactus,
            "carrot" to collections.carrot,
            "cocoa" to collections.cocoabeans,
            "melon" to collections.melon,
            "mushroom" to collections.mushroom,
            "netherwart" to collections.netherwart,
            "potato" to collections.potato,
            "pumpkin" to collections.pumpkin,
            "sugarcane" to collections.sugarcane,
            "wheat" to collections.wheat,
            "seeds" to collections.seeds,
        )
    }
    suspend fun getMiningCollections(): Map<String, Long>?{
        getData()
        val collections = data?.collections?: return null
        return mapOf(
            "gemstone" to collections.gemstone,
            "coal" to collections.coal,
            "iron" to collections.ironingot,
            "gold" to collections.goldingot,
            "lapis" to collections.lapislazuli,
            "redstone" to collections.redstone,
            "diamond" to collections.diamond,
            "emerald" to collections.emerald,
            "quartz" to collections.netherquartz,
            "obsidian" to collections.obsidian,
            "mithril"  to collections.mithril,
            "endstone" to collections.endstone,
            "umber" to collections.umber,
            "sand" to collections.sand,
            "tungsten" to collections.tungsten,
            "glacite" to collections.glacite,
        )
    }


}
