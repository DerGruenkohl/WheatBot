package listeners

import jda
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Entitlement
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.concurrent.ConcurrentHashMap

class CommandManager : ListenerAdapter() {
    private val commands = ConcurrentHashMap<String, ICommand>() // ConcurrentMap for thread-safety
    private val scope = CoroutineScope(Dispatchers.Default) // Coroutine scope for async execution

    override fun onReady(event: ReadyEvent) {
        // Register all commands when the bot is ready
        for (command in commands.values) {
            val commandData: SlashCommandData = Commands.slash(command.name, command.description)
                .addOptions(command.options)
                .setIntegrationTypes(IntegrationType.ALL)
                .setContexts(InteractionContextType.ALL)

            command.subCommands.forEach { subCommand ->
                val subCommandData = SubcommandData(subCommand.name, subCommand.description)
                    .addOptions(subCommand.options)
                commandData.addSubcommands(subCommandData)
            }

            event.jda.upsertCommand(commandData).queue() // Queue the command registration
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = commands[event.name]
        if (command != null) {
            scope.launch {
                try {
                    val interaction = event.interaction
                    var ephemeral = false

                    if (!interaction.hasFullGuild() && event.channel is GuildChannel) {
                        val member = event.member
                        val channel = event.channel as GuildChannel
                        val permissions = member?.getPermissionsExplicit(channel)

                        if (permissions != null &&
                            (!permissions.contains(Permission.MESSAGE_EMBED_LINKS) ||
                                    !permissions.contains(Permission.MESSAGE_ATTACH_FILES))
                        ) {
                            ephemeral = true
                        }
                    }

                    event.subcommandName?.let { subCmd ->
                        val subCommand = command.subCommands.find { it.name == subCmd }
                        subCommand?.execute(event, ephemeral)
                        return@launch
                    }

                    command.execute(event, ephemeral)
                } catch (e: Exception) {
                    e.printStackTrace() // Log any exceptions for debugging
                }
            }
        }
    }

    fun add(vararg command: ICommand) {
        command.forEach { commands[it.name] = it } // Add commands safely to ConcurrentHashMap
    }
}



/*class CommandManager : ListenerAdapter() {
    private val commands = HashMap<String, ICommand>()
    override fun onReady(event: ReadyEvent) {
        for (command in commands.values) {
            val commandData: SlashCommandData = Commands.slash(command.name, command.description)
            commandData
                .addOptions(command.options)
                .setIntegrationTypes(IntegrationType.ALL)
                .setContexts(InteractionContextType.ALL)
            command.subCommands.forEach {
                val subCommandData = SubcommandData(it.name, it.description)
                    .addOptions(it.options)
                commandData.addSubcommands(subCommandData)
            }
            jda.upsertCommand(commandData).queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = commands[event.name]
        command?.let {
            runAsync {

                val int = event.interaction
                int.hasFullGuild()
                var ephemeral = false
                val type = int.commandType
                println(type)
                if(!int.hasFullGuild() && event.channel is GuildChannel){
                    try {
                        val mem = event.member
                        val chanel = event.channel as GuildChannel
                        val perms = mem!!.getPermissionsExplicit(chanel)
                        if (!perms.contains(Permission.MESSAGE_EMBED_LINKS) || !perms.contains(Permission.MESSAGE_ATTACH_FILES)){
                            println("meow")
                            ephemeral = true
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }
                event.subcommandName?.let { cmd ->
                    println(cmd)
                    command.subCommands.forEach {
                        println(it.name)
                    }
                    command.subCommands.find { it.name == cmd }?.execute(event, ephemeral)
                    return@runAsync
                }
                command.execute(event, ephemeral)
            }
        }
    }
    fun add(vararg command: ICommand) {
        command.forEach {
            commands[it.name] = it
        }
    }
}*/