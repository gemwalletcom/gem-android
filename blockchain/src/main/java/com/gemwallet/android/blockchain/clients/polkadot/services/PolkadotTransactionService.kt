package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.PolkadotBlock
import com.wallet.core.blockchain.polkadot.PolkadotTransactionMaterial
import retrofit2.http.GET
import retrofit2.http.Query

interface PolkadotTransactionService {
    @GET("/transaction/material?noMeta=true")
    fun transactionMaterial(): Result<PolkadotTransactionMaterial>

    @GET("/blocks/head")
    fun blockHead(): Result<PolkadotBlock>

    @GET("/blocks?range={range}&noFees=true")
    fun blocks(@Query("range") range: String): Result<List<PolkadotBlock>>
}