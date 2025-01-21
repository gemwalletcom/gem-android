package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.services.SolanaFeeService
import com.gemwallet.android.blockchain.clients.solana.services.getPriorityFees
import com.gemwallet.android.blockchain.clients.solana.services.rentExemption
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import kotlin.math.max
import kotlin.math.min

class SolanaFeeCalculator(
    private val feeService: SolanaFeeService
) {

    private val staticBaseFee = 5_000L
    private val tokenAccountSize = 165

    suspend fun calculate(params: ConfirmParams): List<GasFee> = when (params) {
        is ConfirmParams.Stake -> calculate(params)
        is ConfirmParams.SwapParams -> calculate(params)
        is ConfirmParams.TokenApprovalParams -> throw IllegalArgumentException("Token approval doesn't support")
        is ConfirmParams.TransferParams -> calculate(params)
    }

    suspend fun calculate(params: ConfirmParams.Stake): List<GasFee> {
        return calculate(
            gasLimit = 100_000L,
            multipleOf = 1_500_000L,
        )
    }

    suspend fun calculate(params: ConfirmParams.TransferParams): List<GasFee> {
        return calculate(
            gasLimit = 100_000L,
            multipleOf = if (params.assetId.type() == AssetSubtype.NATIVE) 100_000L else 1_000_000L,
        )
    }

    suspend fun calculate(params: ConfirmParams.SwapParams): List<GasFee> {
        return calculate(
            gasLimit = 420_000L,
            multipleOf = 2_500_000,
        )
    }

    private suspend fun calculate(gasLimit: Long, multipleOf: Long): List<GasFee> {
        val priorityFees = feeService.getPriorityFees()

        return TxSpeed.entries.map { speed ->
            val speedCoefficient = when (speed) {
                TxSpeed.Slow -> 1f
                TxSpeed.Normal -> 3f
                TxSpeed.Fast -> 5f
            }
            val minerFee = if (priorityFees.isEmpty()) {
                multipleOf
            } else {
                val averagePriorityFee = priorityFees.map { it.prioritizationFee }.sortedDescending()
                    .subList(0, min(5, priorityFees.size - 1))
                    .fold(0) { acc, i -> acc + i } / priorityFees.size
                max(((averagePriorityFee + multipleOf - 1) / multipleOf) * multipleOf, multipleOf)
            } * speedCoefficient
            val tokenAccountCreation = feeService.rentExemption(tokenAccountSize)
            val totalFee = staticBaseFee + (minerFee * gasLimit / 1_000_000)

            GasFee(
                feeAssetId = AssetId(Chain.Solana),
                speed = speed,
                minerFee = minerFee.toLong().toBigInteger(),
                maxGasPrice = staticBaseFee.toBigInteger(),
                limit = gasLimit.toBigInteger(),
                amount = totalFee.toLong().toBigInteger(),
                options = mapOf("tokenAccountCreation" to tokenAccountCreation)
            )
        }
    }

}