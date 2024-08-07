package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.near.models.NearAccount
import com.wallet.core.blockchain.near.models.NearAccountAccessKey
import com.wallet.core.blockchain.near.models.NearBlock
import com.wallet.core.blockchain.near.models.NearBroadcastResult
import com.wallet.core.blockchain.near.models.NearGasPrice
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface NearRpcClient {
    @POST("/")
    suspend fun getGasPrice(@Body params: JSONRpcRequest<List<String?>>): Result<JSONRpcResponse<NearGasPrice>>

    @POST("/")
    suspend fun account(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearAccount>>

    @POST("/")
    suspend fun accountAccessKey(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearAccountAccessKey>>

    @POST("/")
    suspend fun latestBlock(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearBlock>>

    @POST("/")
    suspend fun transaction(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearBroadcastResult>>

    @POST("/")
    suspend fun broadcast(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearBroadcastResult>>

    @POST
    suspend fun latestBlock(@Url url: String, @Body params: JSONRpcRequest<Any>): Response<JSONRpcResponse<NearBlock>>
}