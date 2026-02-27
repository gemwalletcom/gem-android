package com.gemwallet.android.ext

import com.gemwallet.android.domains.percentage.formatAsPercentage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

fun tickerFlow(runed: Boolean, delay: Long, stepDelay: Long = delay, onTick: (TickerState) -> Unit): Flow<TickerState> {
    return flow {
        val steps = (delay / stepDelay)

        while (runed) {
            for (step in 1 .. steps) {
                delay(step * stepDelay)
                emit(
                    TickerState(
                        timeMillis = System.currentTimeMillis(),
                        delay = delay,
                        passed = step * stepDelay,
                        complete = step * stepDelay == delay,
                    )
                )
            }
        }
    }.onEach(onTick)
}

class TickerState(
    val timeMillis: Long,
    val delay: Long,
    val passed: Long,
    val complete: Boolean
) {
    val percentage: Double = passed.toDouble() / delay * 100.0
    val percentageFormatted: String by lazy { percentage.formatAsPercentage(0, false) }
}