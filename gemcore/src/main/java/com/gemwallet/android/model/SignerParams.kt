package com.gemwallet.android.model

import java.math.BigInteger

data class SignerParams(
    val input: ConfirmParams,
    val finalAmount: BigInteger = BigInteger.ZERO,
    val owner: String,
    val info: SignerInputInfo,
)

interface  SignerInputInfo {
    fun fee(speed: TxSpeed = TxSpeed.Normal): Fee

    fun allFee(): List<Fee> = emptyList()
}

enum class TxSpeed {
    Slow,
    Normal,
    Fast,
}