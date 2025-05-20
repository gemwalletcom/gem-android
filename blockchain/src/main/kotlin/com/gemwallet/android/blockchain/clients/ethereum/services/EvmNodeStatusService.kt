package com.gemwallet.android.blockchain.clients.ethereum.services

import com.gemwallet.android.blockchain.clients.ethereum.EvmMethod
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmRpcClient.EvmNumber
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface EvmNodeStatusService {
    @POST
    suspend fun chainId(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Response<JSONRpcResponse<EvmNumber?>>

    @POST
    suspend fun sync(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<Boolean?>>

    @POST
    suspend fun latestBlock(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmNumber?>>
}

internal suspend fun EvmNodeStatusService.getChainId(url: String): Response<JSONRpcResponse<EvmNumber?>> {
    return chainId(url, JSONRpcRequest.create(EvmMethod.GetChainId, emptyList()))
}

internal suspend fun EvmNodeStatusService.latestBlock(url: String): Result<JSONRpcResponse<EvmNumber?>> {
    return latestBlock(url, JSONRpcRequest.create(EvmMethod.GetBlockNumber, emptyList()))
}

internal suspend fun EvmNodeStatusService.sync(url: String): Result<JSONRpcResponse<Boolean?>> {
    return sync(url, JSONRpcRequest.create(EvmMethod.Sync, emptyList()))
}