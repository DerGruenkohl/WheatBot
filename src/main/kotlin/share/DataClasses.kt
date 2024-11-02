package share

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias expHistory = LinkedHashMap<Long, time>

@Serializable
data class Guild(
    val guildID: String,
    val members: List<Member>
)
@Serializable
data class Member(
    val uuid: String,
    val expHistory: expHistory
)

@Serializable
data class time(
    val hours: Int,
    val mins: Int
) : Comparable<time> {
    override fun compareTo(other: time): Int {
        return compareValuesBy(this, other, time::hours, time::mins)
    }

    override fun toString(): String {
        return "${this.hours}h, ${this.mins}m"
    }
    fun toMinutes(): Int = this.hours * 60 + this.mins
}
@Serializable
data class GuildEntry(
    val uuid: String,
    val rank: String,
    val joined: Long,
    val expHistory: LinkedHashMap<Long, Int>
)
@Serializable
data class UptimeLeaderboard(
    val size: Int,
    val members: Map<String, time>
)
@Serializable
data class Player(
    @SerialName("playerUuid")
    val uuid: String,
    val timestamp: Long = System.currentTimeMillis(),
    val collections: Collections,
    @SerialName("farmingWeight")
    val weight: Weight
)

@Serializable
data class Collections(
    @SerialName("CARROT_ITEM")
    val carrot: Long,
    @SerialName("CACTUS")
    val cactus: Long,
    @SerialName("SUGAR_CANE")
    val sugarCane: Long,
    @SerialName("PUMPKIN")
    val pumpkin: Long,
    @SerialName("WHEAT")
    val wheat: Long,
    @SerialName("SEEDS")
    val seeds: Long,
    @SerialName("MUSHROOM_COLLECTION")
    val mushroom: Long,
    @SerialName("NETHER_STALK")
    val wart: Long,
    @SerialName("MELON")
    val melon: Long,
    @SerialName("POTATO_ITEM")
    val potato: Long,
    @SerialName("INK_SACK:3")
    val cocoaBeans: Long
)

//WEIGHT SHIT
@Serializable
data class Weight(
    @SerialName("cropWeight")
    val cropWeight: CropWeight,
    @SerialName("totalWeight")
    val totalWeight: Double,
    @SerialName("uncountedCrops")
    val uncountedCrops: UncountedCrops
)

@Serializable
data class CropWeight(
    @SerialName("Cactus")
    val cactus: Double,
    @SerialName("Carrot")
    val carrot: Double,
    @SerialName("Cocoa Beans")
    val cocoaBeans: Double,
    @SerialName("Melon")
    val melon: Double,
    @SerialName("Mushroom")
    val mushroom: Double,
    @SerialName("Nether Wart")
    val wart: Double,
    @SerialName("Potato")
    val potato: Double,
    @SerialName("Pumpkin")
    val pumpkin: Double,
    @SerialName("Sugar Cane")
    val sugarCane: Double,
    @SerialName("Wheat")
    val wheat: Double
)
@Serializable
data class UncountedCrops(
    @SerialName("Cactus")
    val cactus: Int,
    @SerialName("Carrot")
    val carrot: Int,
    @SerialName("Cocoa Beans")
    val cocoaBeans: Int,
    @SerialName("Melon")
    val melon: Int,
    @SerialName("Mushroom")
    val mushroom: Int,
    @SerialName("Nether Wart")
    val wart: Int,
    @SerialName("Potato")
    val potato: Int,
    @SerialName("Pumpkin")
    val pumpkin: Int,
    @SerialName("Sugar Cane")
    val sugarCane: Int,
    @SerialName("Wheat")
    val wheat: Int
)
@Serializable
data class Link(
    val discordId: Long,
    val uuid: String,
    val discordName: String? = null,
    val settings: Settings = Settings()
)
@Serializable
data class Settings(
    val track: Boolean = false,
    val pestGain: Boolean = false,
    val collectionGain: Boolean = false,
    val uptime: Boolean = true,
    val customImage: Boolean = false,
    val textColor: String? = null,
    val profileID: String? = null
)
@Serializable
data class OvertakeBody(
    val name1: String,
    val name2: String,
    val type: String,
    val lookup: String,
    val days: Int
)
@Serializable
data class OutgoingGraph(
    val p1: GraphPlayer,
    val p2: GraphPlayer
)
@Serializable
data class GraphPlayer(
    val uuid: String,
    val type: String,
    val days: Int,
    val gain: Double,
    val full: Double
)
@Serializable
data class GainBody(
    val name1: String,
    val type: String,
    val lookup: String,
    val days: Int
)
