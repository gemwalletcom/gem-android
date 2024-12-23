package com.gemwallet.android.blockchain.clients.algorand.services

import com.wallet.core.blockchain.algorand.AlgorandTransactionStatus
import retrofit2.http.GET
import retrofit2.http.Path

interface AlgorandTxStatusService {
    @GET("/v2/transactions/pending/{id}")
    suspend fun transaction(@Path("id") id: String): Result<AlgorandTransactionStatus>
}