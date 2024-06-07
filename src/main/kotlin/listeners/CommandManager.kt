package listeners

import jda
import listeners.ICommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandManager : ListenerAdapter() {
    private val commands: MutableList<ICommand> = ArrayList()
    override fun onReady(event: ReadyEvent) {
        for (command in commands) {
            if (command.options == null) {
                jda.upsertCommand(command.name, command.description).queue()
            } else {
                jda.upsertCommand(command.name, command.description).addOptions(command.options).queue()
            }
        }

    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        for (command in commands) {
            if (command.name == event.name) {
                Thread{
                    command.execute(event)
                }.start()
                return
            }
        }
    }

    fun add(command: ICommand) {
        commands.add(command)
    }
    fun add(commands: List<ICommand>){
        commands.forEach {
            add(it)
        }
    }
}