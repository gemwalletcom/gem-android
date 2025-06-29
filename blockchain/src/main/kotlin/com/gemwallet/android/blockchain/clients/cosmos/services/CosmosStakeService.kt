package com.gemwallet.android.blockchain.clients.cosmos.services

import com.wallet.core.blockchain.cosmos.CosmosDelegations
import com.wallet.core.blockchain.cosmos.CosmosRewards
import com.wallet.core.blockchain.cosmos.CosmosUnboundingDelegations
import com.wallet.core.blockchain.cosmos.CosmosValidators
import retrofit2.http.GET
import retrofit2.http.Path

interface CosmosStakeService {

    @GET("/cosmos/staking/v1beta1/validators?pagination.limit=1000")
    suspend fun validators(): Result<CosmosValidators>

    @GET("/cosmos/staking/v1beta1/delegations/{address}")
    suspend fun delegations(@Path("address") address: String): Result<CosmosDelegations>

    @GET("/cosmos/staking/v1beta1/delegators/{address}/unbonding_delegations")
    suspend fun undelegations(@Path("address") address: String): Result<CosmosUnboundingDelegations>

    @GET("/cosmos/distribution/v1beta1/delegators/{address}/rewards")
    suspend fun rewards(@Path("address") address: String): Result<CosmosRewards>
}