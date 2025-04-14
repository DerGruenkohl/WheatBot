package com.dergruenkohl.commands.stats

import com.dergruenkohl.api.hypixelClient
import com.dergruenkohl.hypixel.client.HypixelClient
import com.dergruenkohl.hypixel.client.getSelectedProfileID
import com.dergruenkohl.hypixel.client.getSelectedProfileMember
import com.dergruenkohl.utils.calculators.ProfileCalculator
import com.dergruenkohl.utils.database.Link
import com.dergruenkohl.utils.database.Settings
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import io.github.freya022.botcommands.api.core.service.annotations.BService
import kotlinx.io.IOException

@BService
class StatGenerator {
    data class ImageData(
        val uuid: String,
        val ign: String,
        val weight: Double,
        val collections: Map<String, Long>,
    )

    companion object{
        suspend fun fetchStats(ign: String): ImageData {
            val uuid = getMinecraftUUID(ign)
            val profileID = hypixelClient.getSelectedProfileID(uuid)?: throw IOException("ProfileID for $uuid not found")
            val link = Link(
                uuid = uuid,
                discordId = 0,
                discordName = "",
                settings = Settings(
                    uuid = uuid,
                    track = false,
                    customImage = false,
                    textColor = null,
                    profileID = profileID
                ),
            )
            val calc = ProfileCalculator(link)
            val weight = calc.getWeight()?: throw IOException("Weight for $uuid couldnt be calculated")
            val collections = calc.getFarmingCollections() ?: throw IOException("Collections for $uuid couldnt be calculated")
            return ImageData(
                uuid = uuid,
                ign = ign,
                weight = weight,
                collections = collections,
            )
        }
    }

    suspend fun generate(
        ign: String,
        link: Link?,
    ){
        val data = fetchStats(ign)
        val builder = ImageBuilder(data,link)
    }

    class ImageBuilder(
        private val data: ImageData,
        private val link: Link?,
    ){


        suspend fun build() {

        }
    }
}