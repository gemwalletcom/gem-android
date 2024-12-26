package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosCoinInfo
import com.wallet.core.blockchain.aptos.models.AptosResource
import retrofit2.http.GET
import retrofit2.http.Path

interface AptosTokensService {

    @GET("/v1/accounts/{address}/resource/{resource}")
    suspend fun resource(@Path("address") address: String, @Path("resource") resource: String): Result<AptosResource<AptosCoinInfo>>
}