package com.gemwallet.android.features.swap.viewmodels.models

import com.wallet.core.primitives.AssetId

sealed class SwapPairSelect(val fromId: AssetId?, val toId: AssetId?) {

    class From(fromId: AssetId?, toId: AssetId?) : SwapPairSelect(fromId, toId) {

        override fun oppositeId(): AssetId? = toId

        override fun opposite(): SwapPairSelect = To(fromId, toId)

        override fun select(assetId: AssetId): SwapPairSelect = From(assetId, toId)

        override fun change(): AssetId? = fromId
    }

    class To(fromId: AssetId?, toId: AssetId?) : SwapPairSelect(fromId, toId) {
        override fun oppositeId(): AssetId? = fromId

        override fun opposite(): SwapPairSelect = From(fromId, toId)

        override fun select(assetId: AssetId): SwapPairSelect = To(fromId, assetId)

        override fun change(): AssetId? = toId
    }

    abstract fun oppositeId(): AssetId?

    abstract fun opposite(): SwapPairSelect

    abstract fun change(): AssetId?

    abstract fun select(assetId: AssetId): SwapPairSelect

    fun sameChain(): Boolean = fromId != null && toId != null

    companion object {
        fun request(fromId: AssetId?, toId: AssetId?) =
            if (fromId == null) From(fromId, toId) else To(fromId, toId)
    }
}