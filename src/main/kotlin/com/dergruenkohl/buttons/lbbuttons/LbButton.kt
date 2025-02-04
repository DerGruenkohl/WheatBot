package com.dergruenkohl.buttons.lbbuttons

import net.dv8tion.jda.api.entities.MessageEmbed

fun getPagefromLbEmbed(embed: MessageEmbed): Int{
    return embed.title!!.split(" ")[3].toInt() -1
}