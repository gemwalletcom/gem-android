package com.gemwallet.android.blockchain.clients.bitcoin.services

import com.wallet.core.blockchain.bitcoin.BitcoinBlock
import com.wallet.core.blockchain.bitcoin.BitcoinNodeInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface BitcoinNodeStatusService {
    @GET//("/api/v2/")
    suspend fun nodeInfo(@Url url: String): Result<BitcoinNodeInfo>

    @GET//("/api/v2/block/{block}")
    suspend fun block(@Url url: String): Response<BitcoinBlock>
}