package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

class HyperCoreChainData(
    val order: Order?
) : ChainSignData {
    class Order(
        val approveAgentRequired: Boolean,
        val approveReferralRequired: Boolean,
        val approveBuilderRequired: Boolean,
        val builderFeeBps: UInt,
        val agentAddress: String,
        val agentPrivateKey: String
    )
}

fun GemTransactionLoadMetadata.Hyperliquid.toChainData(): HyperCoreChainData {
    val order = order?.let {
        HyperCoreChainData.Order(
            approveAgentRequired = it.approveAgentRequired,
            approveReferralRequired = it.approveReferralRequired,
            approveBuilderRequired = it.approveBuilderRequired,
            builderFeeBps = it.builderFeeBps,
            agentAddress = it.agentAddress,
            agentPrivateKey = it.agentPrivateKey,
        )
    }
    return HyperCoreChainData(order)
}