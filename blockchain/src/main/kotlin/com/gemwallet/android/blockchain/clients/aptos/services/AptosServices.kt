package com.gemwallet.android.blockchain.clients.aptos.services

import com.gemwallet.android.blockchain.clients.aptos.models.AptosAccount
import com.wallet.core.blockchain.aptos.AptosGasFee
import com.wallet.core.blockchain.aptos.AptosTransaction
import com.wallet.core.blockchain.aptos.AptosTransactionSimulation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AptosServices {
        @GET("/v1/accounts/{address}")
        suspend fun accounts(@Path("address") address: String): Result<AptosAccount>

        @GET("/v1/estimate_gas_price")
        suspend fun feePrice(): Result<AptosGasFee>

        @Headers("Content-type: application/json")
        @POST("/v1/transactions/simulate?estimate_max_gas_amount=true")
        suspend fun simulate(@Body data: AptosTransactionSimulation): Result<List<AptosTransaction>>
}