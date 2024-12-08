package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.models.BitcoinFeeResult
import retrofit2.http.GET
import retrofit2.http.Path

interface BitcoinFeeService {
    @GET("/api/v2/estimatefee/{priority}")
    suspend fun estimateFee(@Path("priority") priority: String): Result<BitcoinFeeResult>
}