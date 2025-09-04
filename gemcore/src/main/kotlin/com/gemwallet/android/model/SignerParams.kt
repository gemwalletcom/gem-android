package com.gemwallet.android.model

import com.wallet.core.primitives.FeePriority
import java.math.BigInteger

data class SignerParams(
    val input: ConfirmParams,
    val chainData: ChainSignData,
    private val fee: List<Fee>,
    val finalAmount: BigInteger = BigInteger.ZERO,
) {
    fun fee(priority: FeePriority = FeePriority.Normal): Fee {
        return fee.firstOrNull { it.priority == priority } ?: fee.first()
    }

    fun gasFee(feePriority: FeePriority = FeePriority.Normal): GasFee = (fee(feePriority) as? GasFee) ?: throw Exception("Fee error: wait gas fee")

    fun allFee(): List<Fee> = fee
}

interface  ChainSignData {
//    fun fee(speed: FeePriority = FeePriority.Normal): Fee
//
//    fun gasFee(feePriority: FeePriority = FeePriority.Normal): GasFee = (fee(feePriority) as? GasFee) ?: throw Exception("Fee error: wait gas fee")
//
//    fun allFee(): List<Fee> = emptyList()

    fun blockNumber(): String = ""
}