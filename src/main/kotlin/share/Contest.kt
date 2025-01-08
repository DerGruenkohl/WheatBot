package share


import api.ApiInstance.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


suspend inline fun getContest(uuid:String) : List<Contest> = client.request("https://api.elitebot.dev/contests/$uuid").body<List<Contest>>()

@Serializable
data class Contest(
    val crop: String,
    val timestamp: Long,
    val collected: Int,
    val position: Int,
    val participants: Int,
    val medal: String,
)

class ContestHandler(val contests: List<Contest>) {
    fun groupContestsByDay(): Map<LocalDate, List<Contest>> {
        return contests.groupBy { contest ->
            // Convert the timestamp to LocalDate based on system's default timezone
            Instant.ofEpochSecond(contest.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSortedMap()
    }
    fun groupContestsByWeekDay(): Map<DayOfWeek, List<Contest>> {
        return contests.groupBy { contest ->
            // Convert the timestamp to DayOfWeek based on system's default timezone
            Instant.ofEpochSecond(contest.timestamp).atZone(ZoneId.systemDefault()).dayOfWeek
        }.toSortedMap(compareBy { it.value })
    }
    private fun groupContestsByHour(): Map<Int, List<Contest>> {
        val c = contests.groupBy { contest ->
            // Convert the timestamp to hour based on system's default timezone
            Instant.ofEpochSecond(contest.timestamp).atZone(ZoneId.of("UTC")).hour
        }.toSortedMap()

        for (i in 0..23){
            if (c[i] == null) c[i] = mutableListOf()
        }
        return c
    }

    fun getContest() = groupContestsByHour().map { (day, contests) -> day to contests.size }

    fun print(){
        getContest().forEach { contest ->
            println(contest)
        }
    }

}