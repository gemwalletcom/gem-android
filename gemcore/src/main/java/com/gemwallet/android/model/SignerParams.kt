package com.gemwallet.android.model

import java.math.BigInteger

data class SignerParams(
    val input: ConfirmParams,
    val chainData: ChainSignData,
    val finalAmount: BigInteger = BigInteger.ZERO,
)

interface  ChainSignData {
    fun fee(speed: TxSpeed = TxSpeed.Normal): Fee

    fun gasGee(speed: TxSpeed = TxSpeed.Normal): GasFee = (fee(speed) as? GasFee) ?: throw Exception("Fee error: wait gas fee")

    fun allFee(): List<Fee> = emptyList()

    fun blockNumber(): String = ""
}

enum class TxSpeed {
    Slow,
    Normal,
    Fast,
}