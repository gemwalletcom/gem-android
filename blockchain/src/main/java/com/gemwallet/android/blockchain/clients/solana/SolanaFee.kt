package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class SolanaFee {

    private val baseFee = BigInteger.valueOf(50_000)
    private val priorityMultiplayer = 5L
    private val tokenAccountSize = 165

    suspend operator fun invoke(rpcClient: SolanaRpcClient): Fee {
        val request = JSONRpcRequest.create(SolanaMethod.GetPriorityFee, listOf<String>())
        val fees = rpcClient.getPriorityFees(request).getOrNull()?.result ?: throw Exception()
        val priorityFee = fees.map { it.prioritizationFee }.fold(0) { acc, i -> acc + i } / fees.size

        val tokenAccountCreation = rpcClient.rentExemption(JSONRpcRequest(id = 1, method = SolanaMethod.RentExemption.value, params = listOf(tokenAccountSize)))
            .getOrNull()?.result?.toBigInteger() ?: throw Exception("Can't get fee")
        val fee = baseFee + BigInteger.valueOf(priorityFee * priorityMultiplayer)
        return Fee(
            feeAssetId = AssetId(Chain.Solana),
            speed = TxSpeed.Normal,
            amount = fee,
            options = mapOf("tokenAccountCreation" to tokenAccountCreation)
        )
    }
}