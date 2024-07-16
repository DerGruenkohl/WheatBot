import buttons.lbbuttons.LeftButton
import buttons.lbbuttons.RightButton
import commands.Uptime
import commands.UptimeLb
import commands.Wheat
import listeners.ButtonManager
import listeners.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.hypixel.api.HypixelAPI
import net.hypixel.api.reactor.ReactorHttpClient
import java.util.*

lateinit var jda: JDA
val hypixelAPI = HypixelAPI(ReactorHttpClient(UUID.fromString(apikey)))
const val test = true
fun main() {
    startup()
}

fun startup(){
    val builder = if(test){
        JDABuilder.createDefault(testToken)
    }else{
        JDABuilder.createDefault(token)
    }
    builder.enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
    builder.addEventListeners(
        registerCommands(),
        registerButtons()
        )
    jda = builder.build()
    jda.awaitReady()
    println("${jda.selfUser.name} is ready")
}

fun registerCommands(): CommandManager {
    val manager = CommandManager()
    manager.add(listOf(
        Uptime(),
        Wheat(),
        UptimeLb()
    ))
    return manager
}
fun registerButtons(): ButtonManager{
    val manager = ButtonManager()
    manager.add(listOf(
        RightButton(),
        LeftButton()
    ))
    return manager
}
