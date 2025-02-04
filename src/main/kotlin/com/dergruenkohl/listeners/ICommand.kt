package com.dergruenkohl.listeners

import net.dv8tion.jda.api.interactions.commands.OptionType
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String,
    val description: String,
    val options: Array<Option> = [],
    val subCommands: Array<KClass<out Any>> = []
)

annotation class SubCommand(
    val name: String,
    val description: String,
    val options: Array<Option> = []
)

annotation class Option(
    val name: String,
    val type: OptionType,
    val description: String,
    val choices: Array<Choice> = [],
    val required: Boolean = false
)
annotation class Choice(
    val name: String,
    val value: String
)