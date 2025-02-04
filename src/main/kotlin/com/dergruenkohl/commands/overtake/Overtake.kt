package com.dergruenkohl.commands.overtake

import com.dergruenkohl.listeners.Command


@Command(
    name = "overtake",
    description = "overtake prediction",
    subCommands = [
        Collection::class,
        Pests::class,
        Skills::class,
        Weight::class
    ]
)
class Overtake