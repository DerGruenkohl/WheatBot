import buttons.approval.Accept
import buttons.approval.Deny
import buttons.lbbuttons.Averages
import buttons.lbbuttons.LeftButton
import buttons.lbbuttons.RightButton
import commands.*
import commands.gain.Gain
import commands.overtake.Overtake
import listeners.ButtonManager
import listeners.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent



lateinit var jda: JDA
const val test: Boolean = true
fun main() {
    startup()
}

fun startup(){
    val builder = if(test){
        JDABuilder.createDefault(testToken)
    }else{
        JDABuilder.createDefault(token)
    }
    builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
    builder.setEventPassthrough(true)
    builder.addEventListeners(
        registerCommands(),
        registerButtons(),
        )
    jda = builder.build()
    jda.awaitReady()
    println("${jda.selfUser.name} is ready")

}

fun registerCommands(): CommandManager {
    val manager = CommandManager()
    manager.loadCommandsFromDirectory("src/main/kotlin/commands")
    return manager
}
fun registerButtons(): ButtonManager{
    val manager = ButtonManager()
    manager.add(listOf(
        RightButton(),
        LeftButton(),
        Averages(),
        Accept(),
        Deny()
    ))
    return manager
}
