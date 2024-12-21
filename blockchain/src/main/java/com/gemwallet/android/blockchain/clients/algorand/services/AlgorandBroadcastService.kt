package com.gemwallet.android.blockchain.clients.algorand.services

import com.gemwallet.android.blockchain.clients.algorand.models.AlgorandTransactionBroadcast
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AlgorandBroadcastService {

    @POST("/v2/transactions")
    suspend fun broadcast(@Body body: RequestBody): Result<AlgorandTransactionBroadcast>

}