package com.gemwallet.android.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <T> Flow<T>.mutableStateIn(
    scope: CoroutineScope,
    initialValue: T,
): MutableStateFlow<T> {
    val flow = MutableStateFlow(initialValue)
    scope.launch {
        this@mutableStateIn.collect(flow)
    }
    return flow
}