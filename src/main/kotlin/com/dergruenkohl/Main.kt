package com.dergruenkohl

import com.dergruenkohl.buttons.approval.Accept
import com.dergruenkohl.buttons.approval.Deny
import com.dergruenkohl.buttons.lbbuttons.Averages
import com.dergruenkohl.buttons.lbbuttons.LeftButton
import com.dergruenkohl.buttons.lbbuttons.RightButton
import io.ktor.util.logging.*
import com.dergruenkohl.listeners.ButtonManager
import com.dergruenkohl.listeners.CommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent


lateinit var jda: JDA
const val test: Boolean = false
private val LOGGER = KtorSimpleLogger("Main")

fun main() {
    startup()
}

fun startup(){
    val builder = JDABuilder.createDefault(Config.token)
    builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
    builder.setEventPassthrough(true)
    builder.addEventListeners(
        registerCommands(),
        registerButtons(),
        )
    jda = builder.build()
    jda.awaitReady()
    LOGGER.info("${jda.selfUser.name} is ready")

}

fun registerCommands(): CommandManager {
    val manager = CommandManager()
    manager.loadCommandsFromJar()
    return manager
}
fun registerButtons(): ButtonManager {
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
