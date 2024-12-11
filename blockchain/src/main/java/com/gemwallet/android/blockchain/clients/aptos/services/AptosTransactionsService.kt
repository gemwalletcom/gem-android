package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosTransaction
import retrofit2.http.GET
import retrofit2.http.Path

interface AptosTransactionsService {
    @GET("/v1/transactions/by_hash/{id}")
    suspend fun transactions(@Path("id") txId: String): Result<AptosTransaction>
}