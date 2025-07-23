package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.services.SolanaFeeService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.SolanaPrioritizationFee

internal class TestSolanaFeeService(
    private val fees: List<Long> = listOf(100000000, 2000000000),
) : SolanaFeeService {

    override suspend fun getPriorityFees(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SolanaPrioritizationFee>>> {
        return Result.success(JSONRpcResponse(fees.map { SolanaPrioritizationFee(it) }))
    }
}