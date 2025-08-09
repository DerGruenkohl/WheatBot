package com.dergruenkohl.services

import com.dergruenkohl.WheatBot
import com.dergruenkohl.utils.calculators.ProfileCalculator
import com.dergruenkohl.utils.database.LinkRepo
import com.dergruenkohl.utils.database.ProfileDataRepo
import com.dergruenkohl.utils.scheduleDaily
import com.dergruenkohl.utils.scheduleRepeating
import com.dergruenkohl.utils.scheduleWeekly
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@BService
object TrackingUpdater {
    private val logger = KotlinLogging.logger { }

    init {
        WheatBot.AUTO.scheduleDaily(LocalTime.of(0, 0)) {
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
                delay(Random.nextLong(30, 300).seconds)
            }
        }
    }
}