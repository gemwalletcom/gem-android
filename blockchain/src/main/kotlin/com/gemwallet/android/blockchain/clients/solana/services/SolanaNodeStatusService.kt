package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface SolanaNodeStatusService {

    @POST
    suspend fun health(@Url url: String,@Body request: JSONRpcRequest<List<String>>): Response<JSONRpcResponse<String>>

    @POST
    suspend fun slot(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<Long>>

    @POST
    suspend fun genesisHash(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>
}

suspend fun SolanaNodeStatusService.health(url: String): Response<JSONRpcResponse<String>> {
    return health(url, JSONRpcRequest.create(SolanaMethod.GetHealth, emptyList()))
}

suspend fun SolanaNodeStatusService.slot(url: String): Result<JSONRpcResponse<Long>> {
    return slot(url, JSONRpcRequest.create(SolanaMethod.GetSlot, emptyList()))
}

suspend fun SolanaNodeStatusService.genesisHash(url: String): Result<JSONRpcResponse<String>> {
    return genesisHash(url, JSONRpcRequest.create(SolanaMethod.GetGenesisHash, emptyList()))
}
