package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.AptosResource
import com.wallet.core.blockchain.aptos.AptosResourceBalance
import com.wallet.core.blockchain.aptos.AptosResourceBalanceOptional
import retrofit2.http.GET
import retrofit2.http.Path

interface AptosBalancesService {
    @GET("/v1/accounts/{address}/resource/0x1::coin::CoinStore<0x1::aptos_coin::AptosCoin>")
    suspend fun balance(@Path("address") address: String): Result<AptosResource<AptosResourceBalance>>

    @GET("/v1/accounts/{address}/resources")
    suspend fun resources(@Path("address") address: String): Result<List<AptosResource<AptosResourceBalanceOptional>>>
}