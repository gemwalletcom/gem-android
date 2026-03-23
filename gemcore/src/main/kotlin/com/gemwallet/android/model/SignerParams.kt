package com.gemwallet.android.model

import com.wallet.core.primitives.FeePriority
import uniffi.gemstone.GemTransactionLoadMetadata
import java.math.BigInteger

data class SignerParams(
    val input: ConfirmParams,
    val data: List<Data>,
    val finalAmount: BigInteger = BigInteger.ZERO,
) {
    fun fee(priority: FeePriority = FeePriority.Normal): Fee {
        return data.firstOrNull { it.fee.priority == priority }?.fee ?: data.first().fee
    }

    fun data(priority: FeePriority = FeePriority.Normal): Data {
        return data.firstOrNull { it.fee.priority == priority } ?: data.first()
    }

    fun allFee(): List<Fee> = data.map { it.fee }

    data class Data(
        val fee: Fee,
        val chainData: ChainSignData,
    )
}

interface  ChainSignData {
    fun blockNumber(): String = ""

    fun toDto(): GemTransactionLoadMetadata
}