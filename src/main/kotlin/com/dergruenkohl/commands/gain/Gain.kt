package com.dergruenkohl.commands.gain

import com.dergruenkohl.listeners.Command

@Command(
    name = "gain",
    description = "get the gain of a specific user",
    subCommands = [
        CollectionGain::class,
        SkillGain::class,
        PestGain::class,
        WeightGain::class
    ]
)
class Gain