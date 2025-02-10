package com.dergruenkohl.services

import com.dergruenkohl.utils.database.GuildRepo
import com.dergruenkohl.utils.database.LbHistoryEntity
import com.dergruenkohl.utils.hypixelutils.Time
import com.dergruenkohl.utils.scheduleRepeating
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.time.Duration.Companion.seconds

@BService
object UptimeLbUpdater {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val logger = KotlinLogging.logger { }

    init {
        scope.scheduleRepeating(15, 60, TimeUnit.MINUTES){
            GuildRepo.updateGuilds()
        }
        scope.scheduleRepeating(0, 1, TimeUnit.DAYS){
            delay(30.seconds)
            val lb = GuildRepo.getTopMembersByFarmingUptime(1)
            val average = lb.map { it.second.toMinutes() }.average()
            val avgHours = floor(average / 60).toInt()
            val mins = (average % 60).toInt()
            logger.info { "Average farming uptime: $avgHours hours, $mins minutes" }
            val time = Time(avgHours, mins)
            transaction {
                val canInsert = LbHistoryEntity.all().none { it.time == time }
                if(!canInsert) return@transaction logger.info { "Already inserted for today" }
                LbHistoryEntity.new {
                    this.time = time
                    this.timestamp = LocalDate.now().toEpochDay()
                }
            }

        }
    }
}