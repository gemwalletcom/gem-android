package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.StellarTransactionStatus
import retrofit2.http.GET

interface StellarTxStatusService {
    @GET("/transactions/{id}")
    suspend fun transaction(): Result<StellarTransactionStatus>
}