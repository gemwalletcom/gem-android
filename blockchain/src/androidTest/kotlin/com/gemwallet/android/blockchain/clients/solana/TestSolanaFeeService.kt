package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.services.SolanaFeeService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaPrioritizationFee

internal class TestSolanaFeeService(
    private val fees: List<Int> = listOf(100000000, 2000000000),
    private val rentExemption: Int = 30,
) : SolanaFeeService {

    override suspend fun rentExemption(request: JSONRpcRequest<List<Int>>): Result<JSONRpcResponse<Int>> {
        return Result.success(JSONRpcResponse(rentExemption))
    }

    override suspend fun getPriorityFees(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SolanaPrioritizationFee>>> {
        return Result.success(JSONRpcResponse(fees.map { SolanaPrioritizationFee(it) }))
    }
}