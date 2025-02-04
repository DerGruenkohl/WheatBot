package com.dergruenkohl.utils.dsl

import com.dergruenkohl.Multithreading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit

/**
 * Runs the given [block] asynchronously (now using coroutines).
 *
 * @see Multithreading.runAsync
 */
fun runAsync(block: () -> Unit) = CoroutineScope(Dispatchers.Default).runCatching{block.invoke()}

/**
 * Runs the given [block] asynchronously after the given [delay].
 *
 * @see Multithreading.schedule
 */
fun schedule(delay: Long, timeUnit: TimeUnit, block: () -> Unit) = Multithreading.schedule(block, delay, timeUnit)