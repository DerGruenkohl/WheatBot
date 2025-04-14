package com.dergruenkohl.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

private val logger = KotlinLogging.logger {  }


fun CoroutineScope.submitScheduled(delay: Long, timeUnit: TimeUnit, block: suspend () -> Unit): Job {
    return launch {
        delay(timeUnit.toMillis(delay))
        block()
    }
}

fun CoroutineScope.scheduleRepeating(
    initialDelay: Duration = Duration.ZERO,
    delay: Duration,
    block: suspend () -> Unit
): Job {
    return launch {
        delay(initialDelay)
        while (isActive) {
            block()
            delay(delay)
        }
    }
}

fun CoroutineScope.scheduleRepeating(
    initialDelay: Long,
    delay: Long,
    timeUnit: TimeUnit,
    block: suspend () -> Unit
): Job {
    return launch {
        delay(timeUnit.toMillis(initialDelay))
        while (isActive) {
            block()
            delay(timeUnit.toMillis(delay))
        }
    }
}
fun CoroutineScope.scheduleWeekly(
    dayOfWeek: DayOfWeek,
    time: LocalTime,
    timeZone: ZoneId = ZoneId.systemDefault(),
    block: suspend () -> Unit
): Job {
    return launch {
        val now = LocalDateTime.now(timeZone)
        val nextRun = now.withHour(time.hour).withMinute(time.minute).withSecond(time.second).withNano(0)
            .with(dayOfWeek)
            .let {
                if (it.isBefore(now)) it.plusWeeks(1) else it
            }
        logger.info { "Scheduled weekly task for $nextRun" }
        val initialDelay = java.time.Duration.between(now, nextRun).toMillis()
        delay(initialDelay)
        while (isActive) {
            block()
            delay(TimeUnit.DAYS.toMillis(7))
        }
    }
}