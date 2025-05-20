package com.gemwallet.android.blockchain.clients.polkadot.services

import com.gemwallet.android.blockchain.clients.polkadot.models.PolkadotTransactionBroadcast
import com.wallet.core.blockchain.polkadot.PolkadotTransactionPayload
import retrofit2.http.Body
import retrofit2.http.POST

interface PolkadotBroadcastService {
    @POST("/transaction")
    suspend fun broadcast(@Body data: PolkadotTransactionPayload): Result<PolkadotTransactionBroadcast>
}