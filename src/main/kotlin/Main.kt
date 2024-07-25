import buttons.lbbuttons.LeftButton
import buttons.lbbuttons.RightButton
import commands.*
import kotlinx.coroutines.runBlocking
import listeners.ButtonManager
import listeners.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.layers.area
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.statistics.kandy.stattransform.statSmooth
import share.TrackingManager
import share.data.CollectionPlot



lateinit var jda: JDA
const val test: Boolean = false
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
    manager.add(
        Uptime(),
        Wheat(),
        UptimeLb(),
        LinkDiscord(),
        ManageSetting(),
        GetSettings(),
        GetData(),
        UptimeGraph()
    )
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
