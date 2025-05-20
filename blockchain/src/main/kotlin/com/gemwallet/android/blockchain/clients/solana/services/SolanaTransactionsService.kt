package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaTransaction
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaTransactionsService {
    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaTransaction>>
}

suspend fun SolanaTransactionsService.transaction(hash: String): Result<JSONRpcResponse<SolanaTransaction>> {
    val request = JSONRpcRequest.create(
        SolanaMethod.GetTransaction,
        listOf(
            hash,
            mapOf(
                "encoding" to "jsonParsed",
                SolanaRpcClient.commitmentKey to SolanaRpcClient.commitmentValue,
                "maxSupportedTransactionVersion" to 0,
            ),
        )
    )
    return transaction(request)
}