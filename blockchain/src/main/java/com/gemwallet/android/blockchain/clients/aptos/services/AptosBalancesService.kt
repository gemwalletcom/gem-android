package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosResource
import com.wallet.core.blockchain.aptos.models.AptosResourceBalance
import retrofit2.http.GET
import retrofit2.http.Path

interface AptosBalancesService {
    @GET("/v1/accounts/{address}/resource/0x1::coin::CoinStore<0x1::aptos_coin::AptosCoin>")
    suspend fun balance(@Path("address") address: String): Result<AptosResource<AptosResourceBalance>>
}