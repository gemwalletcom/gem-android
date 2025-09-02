package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.StellarAccount
import com.wallet.core.blockchain.stellar.StellarFees
import retrofit2.http.GET
import retrofit2.http.Path

interface StellarService {
        @GET("/accounts/{address}")
        suspend fun loadAccounts(@Path("address") address: String): Result<StellarAccount?>

        @GET("/fee_stats")
        suspend fun fee(): Result<StellarFees>
}

suspend fun StellarService.accounts(address: String): StellarAccount? {
        val result = loadAccounts(address)

        return result.getOrNull()
}