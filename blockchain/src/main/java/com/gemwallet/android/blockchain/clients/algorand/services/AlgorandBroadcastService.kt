package com.gemwallet.android.blockchain.clients.algorand.services

import com.wallet.core.blockchain.algorand.AlgorandTransactionBroadcast
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AlgorandBroadcastService {

    @POST("/v2/transactions")
    suspend fun broadcast(@Body body: RequestBody): Result<AlgorandTransactionBroadcast>

}