package com.gemwallet.android.features.swap.model

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
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
        fun predicate(other: AssetInfo): Boolean {
            val chain = other.asset.id.chain
            val isEVMChain = EVMChain.entries.map { it.string }.contains(chain.string)
            return (isEVMChain || chain == Chain.Solana)
                    && (other.asset.id.toIdentifier() != oppositeAssetId?.toIdentifier()
                    && other.asset.id.toIdentifier() != changeAssetId?.toIdentifier()
                    && other.asset.id.toIdentifier() != prevAssetId?.toIdentifier())
                    && (prevAssetId == null || prevAssetId.chain == other.asset.id.chain)
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