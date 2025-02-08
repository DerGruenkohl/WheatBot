package com.dergruenkohl.services

import com.dergruenkohl.utils.database.GuildRepo
import com.dergruenkohl.utils.database.LbHistoryEntity
import com.dergruenkohl.utils.hypixelutils.Time
import com.dergruenkohl.utils.scheduleRepeating
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import kotlin.math.floor

@BService
object UptimeLbUpdater {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val logger = KotlinLogging.logger { }

    init {
        scope.scheduleRepeating(15, 60, TimeUnit.MINUTES){
            GuildRepo.updateGuilds()
        }
        scope.scheduleRepeating(30, 2000, TimeUnit.SECONDS){
            val lb = GuildRepo.getTopMembersByFarmingUptime(1)
            val average = lb.map { it.second.toMinutes() }.average()
            val avgHours = floor(average / 60).toInt()
            val mins = (average % 60).toInt()
            logger.info { "Average farming uptime: $avgHours hours, $mins minutes" }
            val time = Time(avgHours, mins)
            LbHistoryEntity.new {
                this.time = time
                this.timestamp = System.currentTimeMillis()
            }
        }
    }
}