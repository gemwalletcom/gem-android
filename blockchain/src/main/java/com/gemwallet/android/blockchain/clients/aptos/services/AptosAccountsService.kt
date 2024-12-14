package com.gemwallet.android.blockchain.clients.aptos.services

import com.gemwallet.android.blockchain.clients.aptos.model.AptosAccount
import retrofit2.http.GET
import retrofit2.http.Path

interface AptosAccountsService {
    @GET("/v1/accounts/{address}")
    suspend fun accounts(@Path("address") address: String): Result<AptosAccount>
}