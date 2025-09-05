package com.gemwallet.android.blockchain.clients.ethereum

import uniffi.gemstone.Chain
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput

class EvmGatewayEstimateFee : GemGatewayEstimateFee {
    override suspend fun getFee(
        chain: Chain,
        input: GemTransactionLoadInput
    ): GemTransactionLoadFee? {
        return null
    }

    override suspend fun getFeeData(
        chain: Chain,
        input: GemTransactionLoadInput
    ): String? {
        return null
    }
}