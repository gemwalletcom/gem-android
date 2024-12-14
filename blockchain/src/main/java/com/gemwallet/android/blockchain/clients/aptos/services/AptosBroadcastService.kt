package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosTransactionBroacast
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AptosBroadcastService {
    @Headers("Content-type: application/json")
    @POST("/v1/transactions")
    suspend fun broadcast(@Body request: RequestBody): Result<AptosTransactionBroacast>
}