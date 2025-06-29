package com.gemwallet.android.blockchain.clients.cosmos.services

import com.wallet.core.blockchain.cosmos.CosmosAccount
import com.wallet.core.blockchain.cosmos.CosmosAccountResponse
import com.wallet.core.blockchain.cosmos.CosmosBlockResponse
import com.wallet.core.blockchain.cosmos.CosmosInjectiveAccount
import retrofit2.http.GET
import retrofit2.http.Path

interface CosmosAccountsService {
    @GET("/cosmos/auth/v1beta1/accounts/{owner}")
    suspend fun getAccountData(@Path("owner") owner: String): Result<CosmosAccountResponse<CosmosAccount>>

    @GET("/cosmos/auth/v1beta1/accounts/{owner}")
    suspend fun getInjectiveAccountData(@Path("owner") owner: String): Result<CosmosAccountResponse<CosmosInjectiveAccount>>

    @GET("/cosmos/base/tendermint/v1beta1/blocks/latest")
    suspend fun getNodeInfo(): Result<CosmosBlockResponse>
}