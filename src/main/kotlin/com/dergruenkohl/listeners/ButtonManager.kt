package com.dergruenkohl.listeners

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


class ButtonManager: ListenerAdapter() {
    private val buttons = mutableListOf<IButton>()
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        buttons.forEach {
            if (it.id == event.componentId){
                //no need for async as this will die
                it.execute(event)
                return
            }
        }
    }
    fun add(command: IButton) {
        buttons.add(command)
    }
    fun add(commands: List<IButton>){
        commands.forEach {
            add(it)
        }
    }

}
interface IButton {
    val id: String
    fun execute(event: ButtonInteractionEvent)
}