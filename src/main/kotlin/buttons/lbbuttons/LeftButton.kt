package buttons.lbbuttons

import listeners.IButton
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageEditData
import share.Leaderboard

class LeftButton: IButton {
    override val id: String
        get() = "lbleft"

    override fun execute(event: ButtonInteractionEvent) {
        val embed = event.message.embeds[0]
        val startindex = getPagefromLbEmbed(embed) - 10
        if (startindex < 0){
            event.reply(":)").setEphemeral(true).queue()
            return
        }
        event.deferEdit().queue()
        val message = event.message
        val lb = Leaderboard()
        val msg = lb.createUptimeLB(startindex)
        message.editMessage(MessageEditData.fromCreateData(msg)).queue()
    }
}