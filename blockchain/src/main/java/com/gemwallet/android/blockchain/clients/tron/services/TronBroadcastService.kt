package com.gemwallet.android.blockchain.clients.tron.services

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface TronBroadcastService {
    @POST("/wallet/broadcasttransaction")
    suspend fun broadcast(@Body body: RequestBody): Result<TronTransactionBroadcast>
}

data class TronTransactionBroadcast(
    val result: Boolean,
    val txid: String,
    val message: String,
)