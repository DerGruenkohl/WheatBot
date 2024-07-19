package listeners

import jda
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.InteractionContextType;
import utils.dsl.runAsync

class CommandManager : ListenerAdapter() {
    private val commands: MutableList<ICommand> = ArrayList()
    override fun onReady(event: ReadyEvent) {
        for (command in commands) {
            val commandData: SlashCommandData = Commands.slash(command.name, command.description)
            commandData
                .addOptions(command.options)
                .setIntegrationTypes(IntegrationType.ALL)
                .setContexts(InteractionContextType.ALL)
            jda.upsertCommand(commandData).queue()
        }

    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        for (command in commands) {
            if (command.name == event.name) {
                runAsync{
                    val ephemeral = !event.interaction.applicationPermissions.contains(Permission.MESSAGE_EMBED_LINKS)
                    command.execute(event, ephemeral)
                }
                return
            }
        }
    }

    fun add(vararg command: ICommand) {
        commands.addAll(command)
    }
}