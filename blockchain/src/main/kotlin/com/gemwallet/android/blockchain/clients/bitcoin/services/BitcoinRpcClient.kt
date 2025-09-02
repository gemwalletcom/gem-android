package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.BitcoinBlock
import com.wallet.core.blockchain.bitcoin.BitcoinFeeResult
import com.wallet.core.blockchain.bitcoin.BitcoinNodeInfo
import com.wallet.core.blockchain.bitcoin.BitcoinUTXO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BitcoinRpcClient {
    @GET("/api/v2/estimatefee/{priority}")
    suspend fun estimateFee(@Path("priority") priority: String): Result<BitcoinFeeResult>

    @GET("/api/v2/utxo/{address}")
    suspend fun getUTXO(@Path("address") address: String): Result<List<BitcoinUTXO>>
}