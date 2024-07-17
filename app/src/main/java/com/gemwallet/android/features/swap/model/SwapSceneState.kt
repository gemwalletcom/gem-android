package com.gemwallet.android.features.swap.model

import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain

class SwapScreenState(
    val isLoading: Boolean = false,
    val isFatal: Boolean = false,
    val details: SwapDetails = SwapDetails.None,
    val select: Select? = null,
) {
    class Select(
        val changeType: SwapItemType,
        val changeAssetId: AssetId?,
        val oppositeAssetId: AssetId?,
        val prevAssetId: AssetId?,
    ) : SwapDetails {
        fun predicate(other: AssetId): Boolean {
            val chain = other.chain
            val isEVMChain = EVMChain.entries.map { it.string }.contains(chain.string)
            return (isEVMChain || chain == Chain.Solana)
                    && (other.toIdentifier() != oppositeAssetId?.toIdentifier()
                    && other.toIdentifier() != changeAssetId?.toIdentifier()
                    && other.toIdentifier() != prevAssetId?.toIdentifier())
                    && (prevAssetId == null || prevAssetId.chain == other.chain)
        }
    }
}

sealed interface SwapDetails {
    data object None : SwapDetails
    class Quote(
        val pay: SwapItemState,
        val receive: SwapItemState,
        val error: SwapError,
        val swaping: Boolean,
        val allowance: Boolean,
    ) : SwapDetails
}