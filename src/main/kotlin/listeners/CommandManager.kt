package listeners

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.io.File
import java.net.JarURLConnection
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

class CommandManager : ListenerAdapter() {
    private val commands = ConcurrentHashMap<String, Any>() // ConcurrentMap for thread-safety
    private val scope = CoroutineScope(Dispatchers.Default) // Coroutine scope for async execution
    private val toReg = mutableListOf<SlashCommandData>()
    private val logger = KtorSimpleLogger("CommandManager")

    override fun onReady(event: ReadyEvent) {
        scope.launch {
            logger.error("test error")
            logger.info("test info")
            // Register all commands when the bot is ready
            val cmdhook = event.jda.retrieveCommands().submit()

            for (command in commands.values) {
                val annotation = command::class.findAnnotation<Command>()!!
                val commandData: SlashCommandData = Commands.slash(annotation.name, annotation.description)
                    .addOptions(annotation.options.map { OptionData(it.type, it.name, it.description, it.required).addChoices(it.choices.map { net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value) }) })
                    .setIntegrationTypes(IntegrationType.ALL)
                    .setContexts(InteractionContextType.ALL)
                annotation.subCommands.forEach { subCmd ->

                    val subAnnotation = subCmd.findAnnotation<SubCommand>()!!
                    logger.info("Found subcommand ${subAnnotation.name}")
                    val subCommandData = SubcommandData(subAnnotation.name, subAnnotation.description)
                        .addOptions(subAnnotation.options.map { OptionData(it.type, it.name, it.description, it.required).addChoices(it.choices.map { net.dv8tion.jda.api.interactions.commands.Command.Choice(it.name, it.value) }) })
                    commandData.addSubcommands(subCommandData)
                }
                toReg.add(commandData)
            }
            cmdhook.await().forEach { cmd ->
                if(toReg.find { it.name == cmd.name } == null){
                    cmd.delete().queue(
                        { logger.info("Deleted command ${cmd.name}") },
                        { logger.error("Failed to delete command ${cmd.name}") }
                    )
                }
            }
            toReg.forEach { cmd ->
                event.jda.upsertCommand(cmd).queue(
                    { logger.info("Registered command ${cmd.name}") },
                    { logger.error("Failed to register command ${cmd.name}") }
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

                    val cmdAnnotation = command::class.findAnnotation<Command>()
                    event.subcommandName?.let { subCmd ->

                        val subCmdsAnnotation = cmdAnnotation?.subCommands
                        val cmd = subCmdsAnnotation?.find { subCmdAnnotation ->
                            subCmd == subCmdAnnotation.findAnnotation<SubCommand>()!!.name
                        }?.createInstance()!!
                        val cmdMethod = cmd::class.members.find { it.name == "execute" }?: return@launch
                        if(cmdMethod.isSuspend){
                            runBlocking {
                                logger.info("running subcommand: $subCmd from ${event.user.name}")
                                cmdMethod.call(cmd, event, ephemeral, this)
                            }
                        }else{
                            logger.info("running subcommand: $subCmd from ${event.user.name}")
                            cmdMethod.call(cmd, event, ephemeral)
                        }
                        return@launch
                    }
                    val method = command::class.members.find { it.name == "execute" }?: return@launch
                    if (method.isSuspend) {
                        runBlocking {
                            logger.info("running command: ${cmdAnnotation?.name} from ${event.user.name}")
                            method.call(command,event, ephemeral, this)
                        }
                    } else {
                        logger.info("running command: ${cmdAnnotation?.name} from ${event.user.name}")
                        method.call(command,event, ephemeral)
                    }
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }
    }

    fun loadCommandsFromJar() {
        val packageName = "commands"
        val commandClasses = getClassesFromPackage(packageName)
        logger.info("Found ${commandClasses.size} classes")
        commandClasses.forEach { clazz ->
            try {
                val kotlinClass = clazz.kotlin
                val annotation = kotlinClass.findAnnotation<Command>()
                if (annotation != null) {
                    val commandInstance = kotlinClass.createInstance()
                    logger.info("Loaded command: ${annotation.name}")
                    commands[annotation.name] = commandInstance
                }
            } catch (e: Exception) {
                logger.error("Error loading class ${clazz.name}", e)
            }
        }
        logger.info("Loaded ${commands.size} commands")
    }

    private fun getClassesFromPackage(packageName: String): List<Class<*>> {
        val packagePath = packageName.replace('.', '/')
        val classLoader = Thread.currentThread().contextClassLoader
        val urls = classLoader.getResources(packagePath)
        val classes = mutableListOf<Class<*>>()

        while (urls.hasMoreElements()) {
            val resource = urls.nextElement()
            val protocol = resource.protocol
            if (protocol == "jar") {
                val jarFile = (resource.openConnection() as JarURLConnection).jarFile
                jarFile.entries().asSequence()
                    .filter { it.name.startsWith(packagePath) && it.name.endsWith(".class") }
                    .map { it.name.removeSuffix(".class").replace('/', '.') }
                    .forEach { className ->
                        try {
                            classes.add(Class.forName(className))
                        } catch (e: ClassNotFoundException) {
                            logger.warn("Class $className not found", e)
                        }
                    }
            } else if (protocol == "file") {
                val directory = File(resource.toURI())
                directory.walkTopDown()
                    .filter { it.isFile && it.extension == "class" }
                    .map {
                        val relativePath = it.relativeTo(directory).path
                        "$packageName.${relativePath.removeSuffix(".class").replace(File.separatorChar, '.')}"
                    }
                    .forEach { className ->
                        try {
                            classes.add(Class.forName(className))
                        } catch (e: ClassNotFoundException) {
                            logger.warn("Class $className not found", e)
                        }
                    }
            }
        }
        return classes
    }



    fun loadCommandsFromDirectory(directory: String) {
        val commandFiles = getAllKotlinFiles(File(directory))
        logger.info("Found ${commandFiles.size} command files")
        commandFiles.forEach { file ->
            val className = file.relativeTo(File(directory)).path
                .replace(File.separatorChar, '.')
                .removeSuffix(".kt")
            try {
                val clazz = Class.forName("commands.$className").kotlin
                val annotation = clazz.findAnnotation<Command>()
                if (annotation != null) {
                    val commandInstance = clazz.createInstance()
                    logger.info("Loaded command: ${annotation.name}")
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