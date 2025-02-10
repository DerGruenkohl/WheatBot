package com.dergruenkohl.services

import com.dergruenkohl.config.Config
import dev.freya02.jda.emojis.unicode.Emojis
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.default
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> = enumSetOf()
    override val cacheFlags: Set<CacheFlag> = enumSetOf()
    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        default(
            token = config.token,
            activity = Activity.customStatus("Wheat ${Emojis.EAR_OF_RICE.formatted}"),
        )
    }
}