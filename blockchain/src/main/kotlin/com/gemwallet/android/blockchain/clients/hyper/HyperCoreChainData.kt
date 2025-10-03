package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

class HyperCoreChainData(
    val approveAgentRequired: Boolean,
    val approveReferralRequired: Boolean,
    val approveBuilderRequired: Boolean,
    val builderFeeBps: UInt,
    val agentAddress: String,
    val agentPrivateKey: String
) : ChainSignData

fun GemTransactionLoadMetadata.Hyperliquid.toChainData(): HyperCoreChainData {
    return HyperCoreChainData(
        approveAgentRequired = approveAgentRequired,
        approveReferralRequired = approveReferralRequired,
        approveBuilderRequired = approveBuilderRequired,
        builderFeeBps = builderFeeBps,
        agentAddress = agentAddress,
        agentPrivateKey = agentPrivateKey,
    )
}