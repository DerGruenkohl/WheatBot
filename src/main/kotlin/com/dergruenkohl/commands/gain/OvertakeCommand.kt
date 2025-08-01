package com.dergruenkohl.commands.gain

import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import com.sksamuel.scrimage.nio.PngWriter
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

typealias Choice = net.dv8tion.jda.api.interactions.commands.Command.Choice

@Command
object OvertakeCommand : ApplicationCommand() {
    override fun getOptionChoices(
        guild: Guild?,
        commandPath: CommandPath,
        optionName: String
    ): List<net.dv8tion.jda.api.interactions.commands.Command.Choice> {
        when (optionName) {
            "collection" -> return listOf(
                Choice("carrot", "carrot"),
                Choice("cactus", "cactus"),
                Choice("cane", "cane"),
                Choice("pumpkin", "pumpkin"),
                Choice("wheat", "wheat"),
                Choice("seeds", "seeds"),
                Choice("mushroom", "mushroom"),
                Choice("wart", "wart"),
                Choice("melon", "melon"),
                Choice("potato", "potato"),
                Choice("cocoa", "cocoa")
            )

            "pest" -> return listOf(
                Choice("mite", "mite"),
                Choice("cricket", "cricket"),
                Choice("moth", "moth"),
                Choice("worm", "worm"),
                Choice("slug", "slug"),
                Choice("beetle", "beetle"),
                Choice("locust", "locust"),
                Choice("rat", "rat"),
                Choice("mosquito", "mosquito"),
                Choice("fly", "fly")
            )

            "skill" -> return listOf(
                Choice("carpentry", "carpentry"),
                Choice("combat", "combat"),
                Choice("enchanting", "enchanting"),
                Choice("farming", "farming"),
                Choice("foraging", "foraging"),
                Choice("fishing", "fishing"),
                Choice("alchemy", "alchemy"),
                Choice("taming", "taming"),
                Choice("mining", "mining"),
                Choice("runecrafting", "runecrafting"),

                )
        }

        return super.getOptionChoices(guild, commandPath, optionName)
    }
    private val logger = KotlinLogging.logger {  }
    private val writer = PngWriter()

    @TopLevelSlashCommandData(description = "Calculate the overtake")
    @JDASlashCommand(name = "overtake", description = "Calculate the overtake", subcommand = "collection")
    suspend fun onCollectionOvertake(
        event: GlobalSlashEvent,
        @SlashOption(name = "user1") user1: String,
        @SlashOption(name = "user2") user2: String,
        @SlashOption(name = "collection") collection: String,
        @SlashOption(name = "days") days: Int = 7
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            if (user1 == user2) return event.reply("Users must be different").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user1)
            val uuid2 = getMinecraftUUID(user2)

            val overtake = Overtake(uuid1, uuid2, collection, days).getCollectionOvertake()?: return hook.editOriginal("Error calculating overtake").queue()
            val reply = OvertakeImage(overtake).generateOvertake()
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(reply.second.bytes(writer), "overtake.png"))
                .setEmbeds(reply.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }

    }
    @JDASlashCommand(name = "overtake", description = "Calculate the overtake", subcommand = "pest")
    suspend fun onPestOvertake(
        event: GlobalSlashEvent,
        @SlashOption(name = "user1") user1: String,
        @SlashOption(name = "user2") user2: String,
        @SlashOption(name = "pest") collection: String,
        @SlashOption(name = "days") days: Int = 7
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            if (user1 == user2) return event.reply("Users must be different").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user1)
            val uuid2 = getMinecraftUUID(user2)

            val overtake = Overtake(uuid1, uuid2, collection, days).getPestOvertake()?: return hook.editOriginal("Error calculating overtake").queue()
            val reply = OvertakeImage(overtake).generateOvertake()
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(reply.second.bytes(writer), "overtake.png"))
                .setEmbeds(reply.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }

    }
    @JDASlashCommand(name = "overtake", description = "Calculate the overtake", subcommand = "skill")
    suspend fun onSkillOvertake(
        event: GlobalSlashEvent,
        @SlashOption(name = "user1") user1: String,
        @SlashOption(name = "user2") user2: String,
        @SlashOption(name = "skill") collection: String,
        @SlashOption(name = "days") days: Int = 7
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            if (user1 == user2) return event.reply("Users must be different").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user1)
            val uuid2 = getMinecraftUUID(user2)

            val overtake = Overtake(uuid1, uuid2, collection, days).getSkillOverTake()?: return hook.editOriginal("Error calculating overtake").queue()
            val reply = OvertakeImage(overtake).generateOvertake()
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(reply.second.bytes(writer), "overtake.png"))
                .setEmbeds(reply.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }

    }
    @JDASlashCommand(name = "overtake", description = "Calculate the overtake", subcommand = "weight")
    suspend fun onWeightOvertake(
        event: GlobalSlashEvent,
        @SlashOption(name = "user1") user1: String,
        @SlashOption(name = "user2") user2: String,
        @SlashOption(name = "days") days: Int = 7
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            if (user1 == user2) return event.reply("Users must be different").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user1)
            val uuid2 = getMinecraftUUID(user2)

            val overtake = Overtake(uuid1, uuid2, "", days).getWeightOvertake()?: return hook.editOriginal("Error calculating overtake").queue()
            val reply = OvertakeImage(overtake).generateOvertake()
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(reply.second.bytes(writer), "overtake.png"))
                .setEmbeds(reply.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }

    }
}