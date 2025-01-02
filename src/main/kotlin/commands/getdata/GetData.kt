package commands.getdata

import listeners.Command

@Command(
    name = "getdata",
    description = "gets the tracked data for a specific type",
    subCommands = [
        Collection::class,
        Pests::class,
        CropWeight::class,
        Weight::class
    ]
)
class GetData {}