package com.gemwallet.android.domains.price

enum class PriceState {
    None,
    Up,
    Down,
}

fun Double.getPriceState() = when {
    this >= 0.0 -> PriceState.Up
    this < 0.0 -> PriceState.Down
    else -> PriceState.None
}