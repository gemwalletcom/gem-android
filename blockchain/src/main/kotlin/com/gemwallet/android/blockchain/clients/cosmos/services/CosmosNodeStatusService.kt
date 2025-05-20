package com.gemwallet.android.blockchain.clients.cosmos.services

import com.wallet.core.blockchain.cosmos.models.CosmosBlockResponse
import com.wallet.core.blockchain.cosmos.models.CosmosSyncing
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface CosmosNodeStatusService {
    @GET//("/cosmos/base/tendermint/v1beta1/syncing")
    suspend fun syncing(@Url url: String): Response<CosmosSyncing>

    @GET//("/cosmos/base/tendermint/v1beta1/blocks/latest")
    suspend fun getNodeInfo(@Url url: String): Result<CosmosBlockResponse>
}