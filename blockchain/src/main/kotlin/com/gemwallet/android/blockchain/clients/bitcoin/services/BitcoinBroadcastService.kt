package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.BitcoinTransactionBroacastResult
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface BitcoinBroadcastService {
    @POST("/api/v2/sendtx/")
    suspend fun broadcast(@Body body: RequestBody): Result<BitcoinTransactionBroacastResult>
}