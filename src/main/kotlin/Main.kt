import commands.Uptime
import commands.Wheat
import listeners.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.hypixel.api.HypixelAPI
import net.hypixel.api.http.HypixelHttpClient
import net.hypixel.api.reactor.ReactorHttpClient
import net.hypixel.api.reply.GuildReply
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.floor

lateinit var jda: JDA
val client: HypixelHttpClient = ReactorHttpClient(UUID.fromString(apikey))
val hypixelAPI = HypixelAPI(client)
fun main() {
    startup()
}

fun startup(){
    val builder = JDABuilder.createDefault(token)
    builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
    builder.addEventListeners(registerCommands())
    jda = builder.build()
    jda.awaitReady()
}

fun registerCommands(): CommandManager {
    val manager = CommandManager()
    manager.add(listOf(
        Uptime(),
        Wheat()
    ))
    return manager
}
fun GuildReply.Guild.Member.getExpHistory(): LinkedHashMap<LocalDate, Int>{
    val map = LinkedHashMap<LocalDate, Int>()
    for (i in 0..6){
        val date = LocalDate.now(ZoneId.of("America/New_York")).minusDays(i.toLong())
        map[date] = this.getExperienceEarned(date)
    }
    return map
}
fun GuildReply.Guild.Member.getFarmingUptime(): LinkedHashMap<LocalDate, Pair<Int, Int>>{
    val map = this.getExpHistory()
    val newMap = LinkedHashMap<LocalDate, Pair<Int, Int>>()
    map.forEach {
        val hours = it.value/9000f
        val hoursInt = floor(hours).toInt()
        val minutes = ((hours - hoursInt) * 60).toInt()
        val pair = Pair(hoursInt, minutes)
        newMap[it.key] = pair
    }
    return newMap
}