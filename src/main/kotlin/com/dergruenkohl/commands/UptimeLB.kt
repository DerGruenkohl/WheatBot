package com.dergruenkohl.commands

import com.dergruenkohl.utils.ErrorHandler
import com.dergruenkohl.utils.database.GuildRepo
import com.dergruenkohl.utils.database.LbHistoryEntity
import com.dergruenkohl.utils.getLoading
import com.dergruenkohl.utils.getMinecraftUUID
import com.dergruenkohl.utils.getMinecraftUsername
import com.dergruenkohl.utils.getUsernames
import dev.freya02.botcommands.jda.ktx.components.TextInput
import dev.freya02.botcommands.jda.ktx.components.row
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.Embed
import dev.freya02.jda.emojis.unicode.Emojis
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toPNG
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.layers.points
import org.jetbrains.kotlinx.kandy.letsplot.settings.Symbol
import org.jetbrains.kotlinx.kandy.letsplot.style.Theme
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import kotlin.time.Duration.Companion.minutes

@Command
class UptimeLB(private val buttons: Buttons, private val modals: Modals): ApplicationCommand() {
    private val logger = KotlinLogging.logger {  }

    @JDASlashCommand("uptimelb", description = "View the uptime leaderboard")
    suspend fun onUptimeLeaderboard(event: GlobalSlashEvent,
                                    @SlashOption(name = "page", description = "The page of the leaderboard") page: Int?,
                                    @SlashOption(name = "ign", description = "The ign of the player you want to lookup") ign: String?) {
        val hook = event.replyEmbeds(getLoading()).await()
        try {
            if (ign != null && page != null){
                return hook.editOriginal("You can only use one option at a time")
                    .setEmbeds()
                    .queue()
            }
            var lbpage = 1
            if (ign != null){
                GuildRepo.getPageForMember(getMinecraftUUID(ign)).let {
                    lbpage = it
                }
            }
            if (page != null){
                lbpage = page
            }
            hook.editOriginalEmbeds(
                getLb(lbpage)
            )
                .setComponents(createRow())
                .queue()

        }
        catch (e: Exception){
            logger.error{ e }
            ErrorHandler.handle(e, hook)
        }
    }
    private suspend fun getLb(page: Int) = Embed {
        val members = GuildRepo.getTopMembersByFarmingUptime(page)
        val uuids = members.map { it.first }
        val usernames = getUsernames(uuids)

        val lb = members.mapIndexed { index, pair ->
            val uptime = pair.second
            val rank = (page - 1) * 10 + index + 1
            val ign = usernames[index]
            "**$rank.** `$ign` - $uptime"
        }

        title = "Uptime Leaderboard"
        description = lb.joinToString("\n")
        footer {
            name = "Page $page"
        }
    }

    private fun getPage(embed: MessageEmbed): Int {
        return embed.footer?.text?.split(" ")?.get(1)?.toInt() ?: 1
    }
    @JDAButtonListener
    suspend fun pageUp(event: ButtonEvent){
        val hook = event.deferEdit().await()
        val msgHook = hook.setEphemeral(true).sendMessage("Loading...").await()
        val page = getPage(event.message.embeds[0])
        hook.editOriginalEmbeds(
            getLb(page + 1)
        ).await()
        msgHook.editMessage("Loaded").await()

    }
    @JDAButtonListener
    suspend fun pageDown(event: ButtonEvent){
        val hook = event.deferEdit().await()
        val msgHook = hook.setEphemeral(true).sendMessage("Loading...").await()
        val page = getPage(event.message.embeds[0])
        if (page == 1){
            return msgHook.editMessage("Already at the first page").queue()
        }
        hook.editOriginalEmbeds(
            getLb(page -1)
        ).await()
        msgHook.editMessage("Loaded").await()
    }
    @JDAButtonListener
    suspend fun searchForIGN(event: ButtonEvent){
        val modal = modals.create("Search for IGN") {
            label("ign"){
                child = TextInput("ign", TextInputStyle.SHORT, isRequired = true)
            }
            bindTo("ignsearch")
        }
        event.replyModal(modal).await()
    }
    @ModalHandler("ignsearch")
    suspend fun onSearchForIGN(event: ModalEvent, @ModalInput("ign") ign: String){
        val hook = event.deferEdit().await()
        val msgHook = hook.setEphemeral(true).sendMessage("Loading...").await()
        try {
            hook.editOriginalEmbeds(
                getLb(GuildRepo.getPageForMember(getMinecraftUUID(ign)))
            ).await()
            msgHook.editMessage("Loaded").await()
        }catch (e: Exception){
            logger.error { e }
            msgHook.editMessage("Failed to find player").await()
            return
        }

    }

    private suspend fun createRow() = listOf(
        row(
            buttons.primary(Emojis.ARROW_LEFT).persistent {
                bindWith(::pageDown)
            },
            buttons.primary(Emojis.ARROW_RIGHT).persistent {
                bindWith(::pageUp)
            }
        ),
        row(
            buttons.primary("Search for IGN").persistent {
                bindWith(::searchForIGN)
            },
            buttons.primary("get averages").persistent {
                bindWith(::getAverages)
            }
        )
    )
    @JDAButtonListener
    suspend fun getAverages(event: ButtonEvent){
        val hook = event.deferReply().await()
        val averages = transaction {
            LbHistoryEntity.all().associate { it.timestamp to it.time }
        }
        val timestamps = averages.keys.map {
            LocalDate.fromEpochDays(it.toInt()).toString()
        }
        val yValues = averages.values.map { it.toMinutes() }
        val plot = plot {
            x(averages.keys)
            y(yValues)
            points {
                size = 3.5
                symbol = Symbol.BULLET
                this.color = Color.BLUE
            }
            line {
                this.color = Color.BLUE
            }

            layout{
                title = "Uptime Leaderboard average"
                theme = Theme.HIGH_CONTRAST_DARK
                x.axis.name = "Date"
                y.axis.name = "Average Uptime from the top 10"
                x.axis.breaksLabeled(averages.keys.toList(), timestamps)
                y.axis.breaksLabeled(yValues, averages.values.map { it.toString() })
                //y.limits = values.first()..values.last()
            }
        }.toPNG()

        hook.editOriginal("")
            .setFiles(FileUpload.fromData(plot, "plot.png"))
            .queue()
    }


}