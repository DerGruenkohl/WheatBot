package com.dergruenkohl.commands.gain

import com.dergruenkohl.WheatBot
import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUUID
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.utils.FileUpload
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


@Command
object GainCommand: ApplicationCommand() {
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

    @TopLevelSlashCommandData(description = "Calculate the gain of a certain collection, pest or skill")
    @JDASlashCommand(name = "gain", description = "Calculate the gain of a certain collection, pest or skill", subcommand = "collection")
    suspend fun onCollectionGain(
        event: GlobalSlashEvent,
        @SlashOption(name = "user") user: String,
        @SlashOption(name = "collection") collection: String,
        @SlashOption(name = "days") days: Int = 7,
        @SlashOption(name = "goal") goal: Long = 1_000_000_000
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user)

            val gainData = Gain(uuid1, collection, days).getCollectionGain()?: return hook.editOriginal("No data found").queue()
            val overtake = GainImage(gainData, goal)
            val gen = overtake.generateGain()
            val os = ByteArrayOutputStream()
            withContext(WheatBot.IOContext) {
                ImageIO.write(gen.second, "png", os)
            }
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "overtake.png"))
                .setEmbeds(gen.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }
    }
    @JDASlashCommand(name = "gain", description = "Calculate the gain of a certain collection, pest or skill", subcommand = "pest")
    suspend fun onPestGain(
        event: GlobalSlashEvent,
        @SlashOption(name = "user") user: String,
        @SlashOption(name = "pest") collection: String,
        @SlashOption(name = "days") days: Int = 7,
        @SlashOption(name = "goal") goal: Long = 1_000_000_000
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user)

            val gainData = Gain(uuid1, collection, days).getPestGain()?: return hook.editOriginal("No data found").queue()
            val overtake = GainImage(gainData, goal)
            val gen = overtake.generateGain()
            val os = ByteArrayOutputStream()
            withContext(WheatBot.IOContext) {
                ImageIO.write(gen.second, "png", os)
            }
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "overtake.png"))
                .setEmbeds(gen.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }
    }
    @JDASlashCommand(name = "gain", description = "Calculate the gain of a certain collection, pest or skill", subcommand = "skill")
    suspend fun onSkillGain(
        event: GlobalSlashEvent,
        @SlashOption(name = "user") user: String,
        @SlashOption(name = "skill") collection: String,
        @SlashOption(name = "days") days: Int = 7,
        @SlashOption(name = "goal") goal: Long = 1_000_000_000
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user)

            val gainData = Gain(uuid1, collection, days).getSkillGain()?: return hook.editOriginal("No data found").queue()
            val overtake = GainImage(gainData, goal)
            val gen = overtake.generateGain()
            val os = ByteArrayOutputStream()
            withContext(WheatBot.IOContext) {
                ImageIO.write(gen.second, "png", os)
            }
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "overtake.png"))
                .setEmbeds(gen.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }
    }
    @JDASlashCommand(name = "gain", description = "Calculate the gain of a certain collection, pest or skill", subcommand = "weight")
    suspend fun onWeightGain(
        event: GlobalSlashEvent,
        @SlashOption(name = "user") user: String,
        @SlashOption(name = "days") days: Int = 7,
        @SlashOption(name = "goal") goal: Long = 10_000
    ) {
        try {
            if (days < 1) return event.reply("days must be greater than 0").setEphemeral(true).queue()
            if (days > 30) return event.reply("days must be less than 31").setEphemeral(true).queue()
            val hook = event.replyEmbeds(getLoading()).await()
            val uuid1 = getMinecraftUUID(user)

            val gainData = Gain(uuid1, "", days).getWeightGain()?: return hook.editOriginal("No data found").queue()
            val overtake = GainImage(gainData, goal)
            val gen = overtake.generateGain()
            val os = ByteArrayOutputStream()
            withContext(WheatBot.IOContext) {
                ImageIO.write(gen.second, "png", os)
            }
            hook.editOriginal("")
                .setFiles(FileUpload.fromData(os.toByteArray(), "overtake.png"))
                .setEmbeds(gen.first)
                .queue()

        } catch (e: Exception) {
            logger.warn { e }
            ErrorHandler.handle(e, event.hook)
        }
    }

}