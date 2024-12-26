package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.PolkadotAccountBalance
import retrofit2.http.GET
import retrofit2.http.Path

interface PolkadotBalancesService {
    @GET("/accounts/{address}/balance-info")
    suspend fun balance(@Path("address") address: String): Result<PolkadotAccountBalance>
}