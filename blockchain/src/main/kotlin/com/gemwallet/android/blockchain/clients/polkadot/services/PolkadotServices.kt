package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.PolkadotAccountBalance
import com.wallet.core.blockchain.polkadot.PolkadotEstimateFee
import com.wallet.core.blockchain.polkadot.PolkadotTransactionMaterial
import com.wallet.core.blockchain.polkadot.PolkadotTransactionPayload
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PolkadotServices {
        @POST("/transaction/fee-estimate")
        suspend fun fee(@Body data: PolkadotTransactionPayload): Result<PolkadotEstimateFee>

        @GET("/accounts/{address}/balance-info")
        suspend fun balance(@Path("address") address: String): Result<PolkadotAccountBalance>

        @GET("/transaction/material?noMeta=true")
        suspend fun transactionMaterial(): Result<PolkadotTransactionMaterial>
}