package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.PolkadotEstimateFee
import com.wallet.core.blockchain.polkadot.PolkadotTransactionPayload
import retrofit2.http.Body
import retrofit2.http.POST

interface PolkadotFeeService {
    @POST("/transaction/fee-estimate")
    suspend fun fee(@Body data: PolkadotTransactionPayload): Result<PolkadotEstimateFee>
}