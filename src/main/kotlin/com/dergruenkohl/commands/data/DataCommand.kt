package com.dergruenkohl.commands.data

import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.database.ProfileDataRepo
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUsername
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import java.time.LocalDate

typealias Choice = net.dv8tion.jda.api.interactions.commands.Command.Choice

@Command
object DataCommand : ApplicationCommand() {
    val logger = KotlinLogging.logger {  }
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

            "pestcollection" -> return listOf(
                Choice("carrot", "carrot"),
                Choice("cactus", "cactus"),
                Choice("cane", "sugarCane"),
                Choice("pumpkin", "pumpkin"),
                Choice("wheat", "wheat"),
                Choice("mushroom", "mushroom"),
                Choice("wart", "netherWart"),
                Choice("melon", "melon"),
                Choice("potato", "potato"),
                Choice("cocoa", "cocoaBeans")
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
            "miningcollection" -> return listOf(
                Choice("gemstone", "gemstone"),
                Choice("coal", "coal"),
                Choice("iron", "iron"),
                Choice("gold", "gold"),
                Choice("lapis", "lapis"),
                Choice("redstone", "redstone"),
                Choice("diamond", "diamond"),
                Choice("emerald", "emerald"),
                Choice("quartz", "quartz"),
                Choice("obsidian", "obsidian"),
                Choice("mithril", "mithril"),
                Choice("endstone", "endstone"),
                Choice("umber", "umber"),
                Choice("sand", "sand"),
                Choice("tungsten", "tungsten"),
                Choice("glacite", "glacite"),
            )
        }

        return super.getOptionChoices(guild, commandPath, optionName)
    }


    @TopLevelSlashCommandData(description = "Data commands")
    @JDASlashCommand(name = "getdata", description = "get Weight data", subcommand = "weight")
    suspend fun onGetDataWeight(
        event: GlobalSlashEvent,
        @SlashOption(name = "user", description = "The user to get data for") user: User?,
        @SlashOption(name = "days", description = "The number of days to get data for") days: Int = 0
    ) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            val user = user ?: event.user
            val userId = user.idLong
            hook.setEphemeral(true).sendMessage("Getting data for ${user.name}").queue()

            val link =
                LinkRepo.getLink(userId) ?: return hook.editOriginal("${user.name} isnt linked to the bot").queue()
            var data = ProfileDataRepo.getWeightData(link.uuid)
            if (days > 0) {
                data = data.filterKeys { it > LocalDate.now().toEpochDay() - days }
            }
            if (data.isEmpty()) {
                return hook.editOriginal("${user.name} has no tracked data").queue()
            }

            val ign = getMinecraftUsername(link.uuid)
            val plot = createPlot(data, "Weight gain graph for $ign", "weight").toPNG()
            hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png")).queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }

    @JDASlashCommand(name = "getdata", description = "get Farming Collection data", subcommand = "farmingcollections")
    suspend fun onFarmingCollectionData(
        event: GlobalSlashEvent,
        @SlashOption(
            name = "user",
            description = "The user to get data for"
        ) user: User?,
        @SlashOption(
            name = "collection",
            description = "The collection to get data for"
        ) collection: String,
        @SlashOption(name = "days", description = "The number of days to get data for") days: Int = 0
    ) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            val user = user ?: event.user
            val userId = user.idLong
            hook.setEphemeral(true).sendMessage("Getting data for ${user.name}").queue()

            val link =
                LinkRepo.getLink(userId) ?: return hook.editOriginal("${user.name} isnt linked to the bot").queue()
            var data = ProfileDataRepo.getFarmingCollectionsDataByKey(link.uuid, collection).mapValues {
                it.value ?: 0
            }
            if (days > 0) {
                data = data.filterKeys { it > LocalDate.now().toEpochDay() - days }
            }
            if (data.isEmpty()) {
                return hook.editOriginal("${user.name} has no tracked data").queue()
            }
            val ign = getMinecraftUsername(link.uuid)
            val plot = createPlot(data, "$collection collection graph for $ign", collection).toPNG()
            hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png")).queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }

    @JDASlashCommand(name = "getdata", description = "Get Gain from pests", subcommand = "pestgain")
    suspend fun onPestCollectionData(
        event: GlobalSlashEvent,
        @SlashOption(name = "user", description = "The user to get data for") user: User?,
        @SlashOption(
            name = "pestcollection",
            description = "The collection to get data for"
        ) collection: String,
        @SlashOption(name = "days", description = "The number of days to get data for") days: Int = 0
    ) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            val user = user ?: event.user
            val userId = user.idLong
            hook.setEphemeral(true).sendMessage("Getting data for ${user.name}").queue()

            val link =
                LinkRepo.getLink(userId) ?: return hook.editOriginal("${user.name} isnt linked to the bot").queue()
            var data = ProfileDataRepo.getPestDropsDataByKey(link.uuid, collection).mapValues {
                it.value ?: 0.0
            }
            if (days > 0) {
                data = data.filterKeys { it > LocalDate.now().toEpochDay() - days }
            }
            if (data.isEmpty()) {
                return hook.editOriginal("${user.name} has no tracked data").queue()
            }
            val ign = getMinecraftUsername(link.uuid)
            val plot = createPlot(data, "$collection pest collection graph for $ign", collection).toPNG()
            hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png")).queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }
    @JDASlashCommand(name = "getdata", description = "Get a skill xp graph", subcommand = "skills")
    suspend fun onSkillData(
        event: GlobalSlashEvent,
        @SlashOption(name = "user", description = "The user to get data for") user: User?,
        @SlashOption(
            name = "skill",
            description = "The skill to get data for"
        ) collection: String,
        @SlashOption(name = "days", description = "The number of days to get data for") days: Int = 0
    ) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            val user = user ?: event.user
            val userId = user.idLong
            hook.setEphemeral(true).sendMessage("Getting data for ${user.name}").queue()

            val link =
                LinkRepo.getLink(userId) ?: return hook.editOriginal("${user.name} isnt linked to the bot").queue()
            var data = ProfileDataRepo.getSkillsDataByKey(link.uuid, collection).mapValues {
                it.value ?: 0.0
            }
            if (days > 0) {
                data = data.filterKeys { it > LocalDate.now().toEpochDay() - days }
            }
            if (data.isEmpty()) {
                return hook.editOriginal("${user.name} has no tracked data").queue()
            }
            val ign = getMinecraftUsername(link.uuid)
            val plot = createPlot(data, "$collection xp graph for $ign", collection).toPNG()
            hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png")).queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }
    @JDASlashCommand(name = "getdata", description = "get mining Collection data", subcommand = "miningcollections")
    suspend fun onMiningCollectionData(
        event: GlobalSlashEvent,
        @SlashOption(
            name = "user",
            description = "The user to get data for"
        ) user: User?,
        @SlashOption(
            name = "miningcollection",
            description = "The collection to get data for"
        ) collection: String,
        @SlashOption(name = "days", description = "The number of days to get data for") days: Int = 0
    ) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            val user = user ?: event.user
            val userId = user.idLong
            hook.setEphemeral(true).sendMessage("Getting data for ${user.name}").queue()

            val link =
                LinkRepo.getLink(userId) ?: return hook.editOriginal("${user.name} isnt linked to the bot").queue()
            var data = ProfileDataRepo.getMiningCollectionsDataByKey(link.uuid, collection).mapValues {
                it.value ?: 0
            }
            if (days > 0) {
                data = data.filterKeys { it > LocalDate.now().toEpochDay() - days }
            }
            if (data.isEmpty()) {
                return hook.editOriginal("${user.name} has no tracked data").queue()
            }
            val ign = getMinecraftUsername(link.uuid)
            val plot = createPlot(data, "$collection collection graph for $ign", collection).toPNG()
            hook.editOriginal("")
                .setEmbeds()
                .setFiles(FileUpload.fromData(plot, "plot.png")).queue()
        } catch (e: Exception) {
            logger.error { e }
            ErrorHandler.handle(e, hook)
        }
    }
}