package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.generated.StellarFees
import retrofit2.http.GET

interface StellarFeeService {
    @GET("/fee_stats")
    suspend fun fee(): Result<StellarFees>
}