package com.gemwallet.android.blockchain.clients.bitcoin

import uniffi.gemstone.Chain
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput

class BitcoinGatewayEstimateFee : GemGatewayEstimateFee {
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