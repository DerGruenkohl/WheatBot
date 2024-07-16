package buttons.lbbuttons

import listeners.IButton
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageEditData
import share.Leaderboard

class RightButton: IButton {
    override val id: String
        get() = "lbright"

    override fun execute(event: ButtonInteractionEvent) {
        event.deferEdit().queue()
        val embed = event.message.embeds[0]
        val startindex = getPagefromLbEmbed(embed) + 10
        val lb = Leaderboard()
        val msg = lb.createUptimeLB(startindex)

        event.message.editMessage(MessageEditData.fromCreateData(msg)).queue()
    }
}