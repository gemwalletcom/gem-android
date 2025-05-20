package com.gemwallet.android.blockchain.clients.cosmos.services

import com.wallet.core.blockchain.cosmos.models.CosmosTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CosmosTransactionsService {
    @GET("/cosmos/tx/v1beta1/txs/{txId}")
    suspend fun transaction(@Path("txId") txId: String): Result<CosmosTransactionResponse>
}