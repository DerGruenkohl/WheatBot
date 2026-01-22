package com.dergruenkohl.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.InteractionHook
import java.awt.Color
import java.net.ConnectException
import javax.imageio.IIOException

object ErrorHandler {
    private val errorEmbed = EmbedBuilder()
        .setTitle("Error")
        .setColor(Color.RED)
        .setFooter("Have this cat :)")


    suspend fun handle(e: Exception, hook: InteractionHook, msg: String = "") {
        when(e) {
            is HttpRequestTimeoutException, is ConnectException -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("Failed to connect to the API, please try again later")
                            .setImage(getCat())
                            .build()
                    ).queue()
            }
            is IIOException -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("Failed to generate an Image")
                            .setImage(getCat())
                            .build()
                    ).queue()
            }
            is ClientRequestException -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("Api request failed: ${e.response.bodyAsText()}")
                            .setImage(getCat())
                            .build()
                    ).queue()
            }
            else -> {
                hook.editOriginal("")
                    .setEmbeds(
                        errorEmbed
                            .setDescription("An error occurred")
                            .setImage(getCat())
                            .build()
                    ).queue()
            }
        }
    }
}