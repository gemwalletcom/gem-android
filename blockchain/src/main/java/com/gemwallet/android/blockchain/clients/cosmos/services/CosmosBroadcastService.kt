package com.gemwallet.android.blockchain.clients.cosmos.services

import com.wallet.core.blockchain.cosmos.models.CosmosBroadcastResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface CosmosBroadcastService {
    @POST("/cosmos/tx/v1beta1/txs")
    suspend fun broadcast(@Body body: RequestBody): Result<CosmosBroadcastResponse>
}