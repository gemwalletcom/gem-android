package com.gemwallet.android.model

import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.ScanTransaction
import java.math.BigInteger

data class SignerParams(
    val input: ConfirmParams,
    val chainData: ChainSignData,
    val finalAmount: BigInteger = BigInteger.ZERO,
    val scanTransaction: ScanTransaction? = null,
)

interface  ChainSignData {
    fun fee(speed: FeePriority = FeePriority.Normal): Fee

    fun gasFee(feePriority: FeePriority = FeePriority.Normal): GasFee = (fee(feePriority) as? GasFee) ?: throw Exception("Fee error: wait gas fee")

    fun allFee(): List<Fee> = emptyList()

    fun blockNumber(): String = ""
}