package share

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