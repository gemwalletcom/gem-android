package com.gemwallet.android.model

import com.wallet.core.primitives.ScanTransaction
import java.math.BigInteger

data class SignerParams(
    val input: ConfirmParams,
    val chainData: ChainSignData,
    val finalAmount: BigInteger = BigInteger.ZERO,
    val scanTransaction: ScanTransaction? = null,
)

interface  ChainSignData {
    fun fee(speed: TxSpeed = TxSpeed.Normal): Fee

    fun gasFee(speed: TxSpeed = TxSpeed.Normal): GasFee = (fee(speed) as? GasFee) ?: throw Exception("Fee error: wait gas fee")

    fun allFee(): List<Fee> = emptyList()

    fun blockNumber(): String = ""
}

enum class TxSpeed {
    Slow,
    Normal,
    Fast,
}