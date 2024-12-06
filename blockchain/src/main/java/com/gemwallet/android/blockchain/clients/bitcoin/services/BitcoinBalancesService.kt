package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.models.BitcoinAccount
import retrofit2.http.GET
import retrofit2.http.Path

interface BitcoinBalancesService {
    @GET("/api/v2/address/{address}")
    suspend fun balance(@Path("address") address: String): Result<BitcoinAccount>
}