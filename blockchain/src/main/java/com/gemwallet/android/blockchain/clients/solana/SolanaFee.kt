package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class SolanaFee {

    private val staticBaseFee = 5_000L
    private val tokenAccountSize = 165

    suspend operator fun invoke(rpcClient: SolanaRpcClient, transactionType: TransactionType, assetType: AssetSubtype): Fee {
        val gasLimit = 100_000L
        val priorityFees = rpcClient.getPriorityFees()
        val averagePriorityFee = if (priorityFees.isEmpty()) {
            0
        } else {
            priorityFees.map { it.prioritizationFee }.fold(0) { acc, i -> acc + i } / priorityFees.size
        }
        val multipleOf = when (transactionType) {
            TransactionType.Transfer -> if (assetType == AssetSubtype.NATIVE) 10_000L else 100_000L
            TransactionType.TokenApproval,
            TransactionType.StakeDelegate,
            TransactionType.StakeUndelegate,
            TransactionType.StakeRewards,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw,
            TransactionType.Swap -> 100_000
        }
        val minerFee = ((averagePriorityFee + multipleOf - 1) / multipleOf) * multipleOf

        val tokenAccountCreation = rpcClient.rentExemption(JSONRpcRequest(id = 1, method = SolanaMethod.RentExemption.value, params = listOf(tokenAccountSize)))
            .getOrNull()?.result?.toBigInteger() ?: throw Exception("Can't get fee")

        val totalFee = staticBaseFee + (minerFee / (gasLimit / 10_000L))
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