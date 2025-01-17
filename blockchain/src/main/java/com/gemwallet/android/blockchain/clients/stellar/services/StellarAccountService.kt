package com.gemwallet.android.blockchain.clients.stellar.services

import com.wallet.core.blockchain.stellar.StellarAccount
import retrofit2.http.GET
import retrofit2.http.Path

interface StellarAccountService {
    @GET("/accounts/{address}")
    suspend fun loadAccounts(@Path("address") address: String): Result<StellarAccount?>
}

suspend fun StellarAccountService.accounts(address: String): StellarAccount? {
    val result = loadAccounts(address)

    return result.getOrNull()
}