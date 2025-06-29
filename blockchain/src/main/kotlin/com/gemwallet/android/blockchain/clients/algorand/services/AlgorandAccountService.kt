package com.gemwallet.android.blockchain.clients.algorand.services

import com.wallet.core.blockchain.algorand.AlgorandAccount
import retrofit2.http.GET
import retrofit2.http.Path

interface AlgorandAccountService {
    @GET("/v2/accounts/{address}")
    suspend fun accounts(@Path("address") address: String): Result<AlgorandAccount>
}