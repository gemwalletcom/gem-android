package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.PolkadotBlock
import com.wallet.core.blockchain.polkadot.PolkadotNodeVersion
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface PolkadotNodeStatusService {
    @GET//(/node/version)
    suspend fun nodeVersion(@Url url: String): Response<PolkadotNodeVersion>

    @GET//("/blocks/head")
    suspend fun blockHead(@Url url: String): Response<PolkadotBlock>
}