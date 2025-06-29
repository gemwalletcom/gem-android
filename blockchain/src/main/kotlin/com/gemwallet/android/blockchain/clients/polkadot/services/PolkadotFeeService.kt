package com.gemwallet.android.blockchain.clients.polkadot.services

import com.wallet.core.blockchain.polkadot.generated.PolkadotEstimateFee
import com.wallet.core.blockchain.polkadot.generated.PolkadotTransactionPayload
import retrofit2.http.Body
import retrofit2.http.POST

interface PolkadotFeeService {
    @POST("/transaction/fee-estimate")
    suspend fun fee(@Body data: PolkadotTransactionPayload): Result<PolkadotEstimateFee>
}