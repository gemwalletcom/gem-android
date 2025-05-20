package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaBroadcastService {
    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String>>
}