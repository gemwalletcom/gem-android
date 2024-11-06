package com.gemwallet.android.model

data class Balance<T>(
    val available: T,
    val frozen: T,
    val locked: T,
    val staked: T,
    val pending: T,
    val rewards: T,
    val reserved: T,
)