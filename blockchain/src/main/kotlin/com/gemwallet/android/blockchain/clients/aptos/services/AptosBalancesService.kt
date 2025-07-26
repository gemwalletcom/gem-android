package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.AptosResource
import com.wallet.core.blockchain.aptos.AptosResourceBalance
import com.wallet.core.blockchain.aptos.AptosResourceBalanceOptional
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AptosBalancesService {
    @GET("/v1/accounts/{address}/balance/{assetType}")
    suspend fun balance(@Path("address") address: String, @Path("assetType") assetType: String): ResponseBody

    @GET("/v1/accounts/{address}/resources")
    suspend fun resources(@Path("address") address: String): List<AptosResource<AptosResourceBalanceOptional>>
}