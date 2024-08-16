package com.gemwallet.android.blockchain.clients.cosmos

import com.wallet.core.blockchain.cosmos.models.CosmosAccount
import com.wallet.core.blockchain.cosmos.models.CosmosAccountResponse
import com.wallet.core.blockchain.cosmos.models.CosmosBalances
import com.wallet.core.blockchain.cosmos.models.CosmosBlockResponse
import com.wallet.core.blockchain.cosmos.models.CosmosBroadcastResponse
import com.wallet.core.blockchain.cosmos.models.CosmosDelegations
import com.wallet.core.blockchain.cosmos.models.CosmosInjectiveAccount
import com.wallet.core.blockchain.cosmos.models.CosmosRewards
import com.wallet.core.blockchain.cosmos.models.CosmosSyncing
import com.wallet.core.blockchain.cosmos.models.CosmosTransactionResponse
import com.wallet.core.blockchain.cosmos.models.CosmosUnboundingDelegations
import com.wallet.core.blockchain.cosmos.models.CosmosValidators
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface CosmosRpcClient {
    @GET("/cosmos/bank/v1beta1/balances/{owner}")
    suspend fun getBalance(@Path("owner") owner: String): Result<CosmosBalances>

    @GET("/cosmos/auth/v1beta1/accounts/{owner}")
    suspend fun getAccountData(@Path("owner") owner: String): Result<CosmosAccountResponse<CosmosAccount>>

    @GET("/cosmos/auth/v1beta1/accounts/{owner}")
    suspend fun getInjectiveAccountData(@Path("owner") owner: String): Result<CosmosAccountResponse<CosmosInjectiveAccount>>

    @GET("/cosmos/base/tendermint/v1beta1/blocks/latest")
    suspend fun getNodeInfo(): Result<CosmosBlockResponse>

    @POST("/cosmos/tx/v1beta1/txs")
    suspend fun broadcast(@Body body: RequestBody): Result<CosmosBroadcastResponse>

    @GET("/cosmos/tx/v1beta1/txs/{txId}")
    suspend fun transaction(@Path("txId") txId: String): Result<CosmosTransactionResponse>

    @GET("/cosmos/staking/v1beta1/validators?pagination.limit=1000")
    suspend fun validators(): Result<CosmosValidators>

    @GET("/cosmos/staking/v1beta1/delegations/{address}")
    suspend fun delegations(@Path("address") address: String): Result<CosmosDelegations>

    @GET("/cosmos/staking/v1beta1/delegators/{address}/unbonding_delegations")
    suspend fun undelegations(@Path("address") address: String): Result<CosmosUnboundingDelegations>

    @GET("/cosmos/distribution/v1beta1/delegators/{address}/rewards")
    suspend fun rewards(@Path("address") address: String): Result<CosmosRewards>

    @GET//("/cosmos/base/tendermint/v1beta1/syncing")
    suspend fun syncing(@Url url: String): Response<CosmosSyncing>

    @GET//("/cosmos/base/tendermint/v1beta1/blocks/latest")
    suspend fun getNodeInfo(@Url url: String): Result<CosmosBlockResponse>

}