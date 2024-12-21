package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.StellarTransactionStatus
import retrofit2.http.GET
import retrofit2.http.Path

interface StellarTxStatusService {
    @GET("/transactions/{id}")
    suspend fun transaction(@Path("id") id: String): Result<StellarTransactionStatus>
}