package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.models.BitcoinTransaction
import retrofit2.http.GET
import retrofit2.http.Path

interface BitcoinTransactionsService {
    @GET("/api/v2/tx/{txId}")
    suspend fun transaction(@Path("txId") txId: String): Result<BitcoinTransaction>
}