package listeners

import jda
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
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
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

class CommandManager : ListenerAdapter() {
    private val commands = ConcurrentHashMap<String, Any>() // ConcurrentMap for thread-safety
    private val scope = CoroutineScope(Dispatchers.Default) // Coroutine scope for async execution
    private val toReg = mutableListOf<SlashCommandData>()
    override fun onReady(event: ReadyEvent) {
        scope.launch {
            // Register all commands when the bot is ready
            val cmdhook = event.jda.retrieveCommands().submit()

            for (command in commands.values) {
                val annotation = command::class.findAnnotation<Command>()!!
                val commandData: SlashCommandData = Commands.slash(annotation.name, annotation.description)
                    .addOptions(annotation.options.map { OptionData(it.type, it.name, it.description, it.required).addChoices(it.choices.map { net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value) }) })
                    .setIntegrationTypes(IntegrationType.ALL)
                    .setContexts(InteractionContextType.ALL)
                annotation.subCommands.forEach { subCmd ->
                    println(subCmd.annotations)
                    val subAnnotation = subCmd.findAnnotation<SubCommand>()!!
                    val subCommandData = SubcommandData(subAnnotation.name, subAnnotation.description)
                        .addOptions(subAnnotation.options.map { OptionData(it.type, it.name, it.description, it.required).addChoices(it.choices.map { net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value) }) })
                    commandData.addSubcommands(subCommandData)
                }
                toReg.add(commandData)
            }
            cmdhook.await().forEach { cmd ->
                if(toReg.find { it.name == cmd.name } == null){
                    cmd.delete().queue(
                        { println("Deleted command ${cmd.name}") },
                        { println("Failed to delete command ${cmd.name}") }
                    )
                }
            }
            toReg.forEach { cmd ->
                event.jda.upsertCommand(cmd).queue(
                    { println("Registered command ${cmd.name}") },
                    { println("Failed to register command ${cmd.name}") }
                )
            }
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
                        val cmdAnnotation = command::class.findAnnotation<Command>()
                        val subCmdsAnnotation = cmdAnnotation?.subCommands
                        val cmd = subCmdsAnnotation?.find { subCmdAnnotation ->
                            subCmd == subCmdAnnotation.findAnnotation<SubCommand>()!!.name
                        }?.createInstance()!!
                        val cmdMethod = cmd::class.members.find { it.name == "execute" }
                        println(subCmd)

                        cmdMethod?.call(cmd, event, ephemeral)
                        return@launch
                    }
                    val method = command::class.members.find { it.name == "execute" }
                    method?.call(command, event, ephemeral)
                } catch (e: Exception) {
                    e.printStackTrace() // Log any exceptions for debugging
                }
            }
        }
    }

    fun loadCommandsFromDirectory(directory: String) {
        val commandFiles = getAllKotlinFiles(File(directory))
        println("Found ${commandFiles.size} command files")
        commandFiles.forEach { file ->
            val className = file.relativeTo(File(directory)).path
                .replace(File.separatorChar, '.')
                .removeSuffix(".kt")
            try {
                val clazz = Class.forName("commands.$className").kotlin
                val annotation = clazz.findAnnotation<Command>()
                if (annotation != null) {
                    val commandInstance = clazz.createInstance()
                    println("Loaded command: ${annotation.name}")
                    commands[annotation.name] = commandInstance
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAllKotlinFiles(dir: File): List<File> {
        val kotlinFiles = mutableListOf<File>()
        dir.walkTopDown().forEach {
            if (it.isFile && it.extension == "kt") {
                kotlinFiles.add(it)
            }
        }
        return kotlinFiles
    }
}







/*class CommandManager : ListenerAdapter() {
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

    fun loadCommandsFromDirectory(directory: String) {
        val commandFiles = getAllKotlinFiles(File(directory))
        println("Found ${commandFiles.size} command files")
        commandFiles.forEach { file ->
            val className = file.relativeTo(File(directory)).path
                .replace(File.separatorChar, '.')
                .removeSuffix(".kt")
            try {
                val clazz = Class.forName("commands.$className").kotlin
                if (clazz.hasAnnotation<Command>()) {
                    val commandInstance = clazz.createInstance() as ICommand
                    println("Loaded command: ${commandInstance.name}")
                    add(commandInstance)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun getAllKotlinFiles(dir: File): List<File> {
        val kotlinFiles = mutableListOf<File>()
        dir.walkTopDown().forEach {
            if (it.isFile && it.extension == "kt") {
                kotlinFiles.add(it)
            }
        }
        return kotlinFiles
    }
}*/