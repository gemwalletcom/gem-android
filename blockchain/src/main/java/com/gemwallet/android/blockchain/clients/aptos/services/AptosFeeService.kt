package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosGasFee
import com.wallet.core.blockchain.aptos.models.AptosTransaction
import com.wallet.core.blockchain.aptos.models.AptosTransactionSimulation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AptosFeeService {
    @GET("/v1/estimate_gas_price")
    suspend fun feePrice(): Result<AptosGasFee>

    @Headers("Content-type: application/json")
    @POST("/v1/transactions/simulate?estimate_max_gas_amount=true")
    suspend fun simulate(@Body data: AptosTransactionSimulation): Result<List<AptosTransaction>>
}