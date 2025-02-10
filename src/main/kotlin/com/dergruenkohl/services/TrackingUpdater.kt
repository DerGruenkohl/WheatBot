package com.dergruenkohl.services

import com.dergruenkohl.utils.calculators.ProfileCalculator
import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.database.ProfileDataRepo
import com.dergruenkohl.utils.scheduleRepeating
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@BService
object TrackingUpdater {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val logger = KotlinLogging.logger { }

    init {
        scope.scheduleRepeating(0, 1, TimeUnit.DAYS) {
            delay(30.seconds)
            logger.info { "Updating tracking data" }
            LinkRepo.getAllTrack().forEach { link ->
                try {
                    logger.info { "Updating tracking data for ${link.discordName}" }
                    val uuid = link.uuid
                    val calculator = ProfileCalculator(link)
                    ProfileDataRepo.saveProfileData(uuid, calculator)
                } catch (e: Exception) {
                    logger.warn { e }
                }
                delay(30.seconds)
            }
        }
    }
}