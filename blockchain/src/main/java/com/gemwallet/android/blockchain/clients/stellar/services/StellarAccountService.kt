package com.gemwallet.android.blockchain.clients.stellar.services

import com.gemwallet.android.blockchain.rpc.handleError
import com.wallet.core.blockchain.stellar.StellarAccount
import com.wallet.core.blockchain.stellar.StellarAccountEmpty
import retrofit2.http.GET
import retrofit2.http.Path

interface StellarAccountService {
    @GET("/accounts/{address}")
    suspend fun loadAccounts(@Path("address") address: String): Result<StellarAccount?>
}

suspend fun StellarAccountService.accounts(address: String): StellarAccount? {
    val result = loadAccounts(address)

    return result.getOrNull() ?: result.handleError<StellarAccountEmpty>()?.status?.let {
        if (it == 404) throw StellarEmptyAccountError()
        null
    }
}

class StellarEmptyAccountError() : Exception()