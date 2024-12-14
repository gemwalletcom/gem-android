package com.gemwallet.android.blockchain.clients.ethereum.services

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface EvmBroadcastService {
    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>
}