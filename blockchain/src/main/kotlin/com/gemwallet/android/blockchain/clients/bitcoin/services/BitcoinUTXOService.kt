package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.BitcoinUTXO
import retrofit2.http.GET
import retrofit2.http.Path

interface BitcoinUTXOService {
    @GET("/api/v2/utxo/{address}")
    suspend fun getUTXO(@Path("address") address: String): Result<List<BitcoinUTXO>>
}