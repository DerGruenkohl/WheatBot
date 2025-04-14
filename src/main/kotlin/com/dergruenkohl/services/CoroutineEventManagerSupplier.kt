package com.dergruenkohl.services


import com.dergruenkohl.WheatBot
import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.core.ICoroutineEventManagerSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope

@BService
object CoroutineEventManagerSupplier : ICoroutineEventManagerSupplier {
    override fun get(): CoroutineEventManager {
        return CoroutineEventManager(WheatBot)
    }
}