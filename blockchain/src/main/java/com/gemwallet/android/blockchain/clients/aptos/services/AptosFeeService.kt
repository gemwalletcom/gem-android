package com.gemwallet.android.blockchain.clients.aptos.services

import com.wallet.core.blockchain.aptos.models.AptosGasFee
import retrofit2.http.GET

interface AptosFeeService {
    @GET("/v1/estimate_gas_price")
    suspend fun feePrice(): Result<AptosGasFee>
}