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

class SolanaFeeCalculator(
    private val feeService: SolanaFeeService
) {

    private val staticBaseFee = 5_000L
    private val tokenAccountSize = 165

    suspend fun calculate(params: ConfirmParams): GasFee = when (params) {
        is ConfirmParams.Stake -> calculate(params)
        is ConfirmParams.SwapParams -> calculate(params)
        is ConfirmParams.TokenApprovalParams -> throw IllegalArgumentException("Token approval doesn't support")
        is ConfirmParams.TransferParams -> calculate(params)
    }

    suspend fun calculate(params: ConfirmParams.Stake): GasFee {
        return calculate(
            gasLimit = 100_000L,
            multipleOf = 10_000L,
        )
    }

    suspend fun calculate(params: ConfirmParams.TransferParams): GasFee {
        return calculate(
            gasLimit = 100_000L,
            multipleOf = if (params.assetId.type() == AssetSubtype.NATIVE) 10_000L else 100_000L,
        )
    }

    suspend fun calculate(params: ConfirmParams.SwapParams): GasFee {
        return calculate(
            gasLimit = 1_400_000L,
            multipleOf = 250_000,

        )
    }

    private suspend fun calculate(gasLimit: Long, multipleOf: Long): GasFee {
        val priorityFees = feeService.getPriorityFees()
        val minerFee = if (priorityFees.isEmpty()) {
            multipleOf
        } else {
            val averagePriorityFee = priorityFees.map { it.prioritizationFee }.fold(0) { acc, i -> acc + i } / priorityFees.size
            max(((averagePriorityFee + multipleOf - 1) / multipleOf) * multipleOf, multipleOf)
        }
        val tokenAccountCreation = feeService.rentExemption(tokenAccountSize)
        val totalFee = staticBaseFee + (minerFee * gasLimit / 1_000_000)
        return GasFee(
            feeAssetId = AssetId(Chain.Solana),
            speed = TxSpeed.Normal,
            minerFee = minerFee.toBigInteger(),
            maxGasPrice = staticBaseFee.toBigInteger(),
            limit = gasLimit.toBigInteger(),
            amount = totalFee.toBigInteger(),
            options = mapOf("tokenAccountCreation" to tokenAccountCreation)
        )
    }

}