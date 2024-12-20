package com.gemwallet.android.blockchain.clients.stellar.services

import com.google.gson.Gson
import com.wallet.core.blockchain.stellar.StellarAccount
import com.wallet.core.blockchain.stellar.StellarAccountEmpty
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StellarAccountService {
    @GET("/accounts/{address}")
    suspend fun loadAccounts(@Path("address") address: String): Response<StellarAccount>
}

suspend fun StellarAccountService.accounts(address: String): StellarAccount? {
    val resp = loadAccounts(address)
    return if (resp.isSuccessful) {
        resp.body()
    } else {
        val error = Gson().fromJson(resp.errorBody()?.string(), StellarAccountEmpty::class.java)
        if (error.status == 404) {
            null
        } else {
            throw Exception()
        }
    }
}