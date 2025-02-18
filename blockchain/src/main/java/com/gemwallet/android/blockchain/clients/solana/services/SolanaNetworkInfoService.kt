package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaBlockhashResult
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaNetworkInfoService {

    @POST("/")
    suspend fun getBlockhash(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaBlockhashResult>>
}

suspend fun SolanaNetworkInfoService.getBlockhash(): String {
    val blockhash = getBlockhash(JSONRpcRequest.create(SolanaMethod.GetLatestBlockhash, emptyList()))
        .getOrNull()?.result?.value?.blockhash
    if (blockhash.isNullOrEmpty()) {
        throw Exception("Can't get latest blockhash")
    }
    return blockhash
}