package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.math.decodeHex
import com.wallet.core.blockchain.near.NearAccount
import com.wallet.core.blockchain.near.NearAccountAccessKey
import com.wallet.core.blockchain.near.NearBlock
import retrofit2.http.Body
import retrofit2.http.POST
import wallet.core.jni.Base58

interface NearRpcClient {

    @POST("/")
    suspend fun account(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearAccount>>

    @POST("/")
    suspend fun accountAccessKey(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearAccountAccessKey>>

    @POST("/")
    suspend fun latestBlock(@Body params: JSONRpcRequest<Any>): Result<JSONRpcResponse<NearBlock>>

}

suspend fun NearRpcClient.accountAccessKey(from: String, ): NearAccountAccessKey {
    val publicKey = "ed25519:" + Base58.encodeNoCheck(from.decodeHex())
    return accountAccessKey(
        JSONRpcRequest(
            method = NearMethod.Query.value,
            params = mapOf(
                "request_type" to "view_access_key",
                "finality" to "final",
                "account_id" to from,
                "public_key" to publicKey,
            )
        )
    ).getOrNull()?.result ?: throw IllegalStateException("Can't get account")
}

suspend fun NearRpcClient.latestBlock(): NearBlock {
    return latestBlock(
        JSONRpcRequest(
            method = NearMethod.LatestBlock.value,
            params = mapOf(
                "finality" to "final",
            )
        )
    ).getOrNull()?.result  ?: throw IllegalStateException("Can't get block")
}