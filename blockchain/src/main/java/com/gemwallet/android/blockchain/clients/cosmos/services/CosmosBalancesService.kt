package com.gemwallet.android.blockchain.clients.cosmos.services

import com.wallet.core.blockchain.cosmos.models.CosmosBalances
import retrofit2.http.GET
import retrofit2.http.Path

interface CosmosBalancesService {
    @GET("/cosmos/bank/v1beta1/balances/{owner}")
    suspend fun getBalance(@Path("owner") owner: String): Result<CosmosBalances>
}