package com.gemwallet.android.blockchain.clients.algorand.services

import com.wallet.core.blockchain.algorand.AlgorandTransactionParams
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface AlgorandNodeStatusService {
    @GET("/v2/transactions/params")
    suspend fun transactionsParams(): Result<AlgorandTransactionParams>

    @GET//("/v2/transactions/params")
    suspend fun transactionsParams(@Url url: String): Response<AlgorandTransactionParams>
}