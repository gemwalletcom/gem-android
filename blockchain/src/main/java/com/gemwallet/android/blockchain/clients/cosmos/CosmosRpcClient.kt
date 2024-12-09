package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosAccountsService
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosBalancesService
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosStakeService
import com.wallet.core.blockchain.cosmos.models.CosmosBlockResponse
import com.wallet.core.blockchain.cosmos.models.CosmosBroadcastResponse
import com.wallet.core.blockchain.cosmos.models.CosmosSyncing
import com.wallet.core.blockchain.cosmos.models.CosmosTransactionResponse
import com.wallet.core.blockchain.cosmos.models.CosmosValidators
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface CosmosRpcClient :
    CosmosBalancesService,
    CosmosAccountsService,
    CosmosStakeService {

    @POST("/cosmos/tx/v1beta1/txs")
    suspend fun broadcast(@Body body: RequestBody): Result<CosmosBroadcastResponse>

    @GET("/cosmos/tx/v1beta1/txs/{txId}")
    suspend fun transaction(@Path("txId") txId: String): Result<CosmosTransactionResponse>

    @GET//("/cosmos/base/tendermint/v1beta1/syncing")
    suspend fun syncing(@Url url: String): Response<CosmosSyncing>

    @GET//("/cosmos/base/tendermint/v1beta1/blocks/latest")
    suspend fun getNodeInfo(@Url url: String): Result<CosmosBlockResponse>

}