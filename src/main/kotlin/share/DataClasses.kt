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
data class SettingModifier(
    val discordId: Long,
    val modifiedSetting: String
)
@Serializable
data class Tracking(
    val uuid: String,
    val data: List<TrackedData>
)
@Serializable
data class TrackedData(
    val timeStamp: Long,
    val pest: List<PestGain>,
    val collections: Collections,
    val uptime: Member? = null
)
@Serializable
data class PestGain(
    val pestName: String,
    val collection: Long,
)
@Serializable
data class Collections(
    @SerialName("CARROT_ITEM")
    val carrot: Long,
    @SerialName("CACTUS")
    val cactus: Long,
    @SerialName("SUGAR_CANE")
    val cane: Long,
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
    val potato: Long
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
    val profileID: String? = null
)
