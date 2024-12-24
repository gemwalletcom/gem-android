package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.PolkadotBlock
import com.wallet.core.blockchain.polkadot.PolkadotTransactionMaterial
import retrofit2.http.GET
import retrofit2.http.Query

interface PolkadotTransactionService {
    @GET("/transaction/material?noMeta=true")
    suspend fun transactionMaterial(): Result<PolkadotTransactionMaterial>

    @GET("/blocks/head")
    suspend fun blockHead(): Result<PolkadotBlock>

    @GET("/blocks?noFees=true")
    suspend fun blocks(@Query("range") range: String): Result<List<PolkadotBlock>>
}