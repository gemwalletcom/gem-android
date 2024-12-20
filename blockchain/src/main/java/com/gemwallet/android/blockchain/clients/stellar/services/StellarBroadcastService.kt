package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.StellarTransactionBroadcast
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface StellarBroadcastService {
    @FormUrlEncoded
    @POST("/transactions")
    suspend fun broadcast(@Field("tx") data: String): Response<StellarTransactionBroadcast>
}