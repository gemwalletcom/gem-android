package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaBalanceValue
import com.wallet.core.blockchain.solana.models.SolanaTokenAccount
import com.wallet.core.blockchain.solana.models.SolanaValue

internal class TestSolanaAccountsService(
    private val tokenAddress: String? = null,
) : SolanaAccountsService {
    var tokenAccountRequest: JSONRpcRequest<List<Any>>? = null

    override suspend fun getTokenAccountByOwner(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>> {
        tokenAccountRequest = request
        return Result.success(
            JSONRpcResponse(
                SolanaValue(
                    listOf(
                        SolanaTokenAccount(
                            tokenAddress ?: (request.params[1] as Map<*, *>)["mint"].toString()
                        ),
                    )
                )
            )
        )
    }

    override suspend fun batchAccount(request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun batchBalances(request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<SolanaValue<SolanaBalanceValue>>>> {
        TODO("Not yet implemented")
    }
}