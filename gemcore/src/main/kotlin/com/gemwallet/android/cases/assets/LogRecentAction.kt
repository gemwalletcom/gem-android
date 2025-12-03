package com.gemwallet.android.cases.assets

import com.gemwallet.android.model.RecentType
import com.wallet.core.primitives.AssetId

interface LogRecentAction {
    suspend fun logRecentAction(
        assetId: AssetId,
        walletId: String,
        type: RecentType,
        toAssetId: AssetId? = null,
    )

    suspend fun logRecentReceive(
        assetId: AssetId,
        walletId: String,
    ) = logRecentAction(assetId, walletId, RecentType.Receive)

    suspend fun logRecentSend(
        assetId: AssetId,
        walletId: String,
    ) = logRecentAction(assetId, walletId, RecentType.Send)

    suspend fun logRecentBuy(
        assetId: AssetId,
        walletId: String,
    ) = logRecentAction(assetId, walletId, RecentType.Buy)

    suspend fun logRecentSwap(
        assetId: AssetId,
        toAssetId: AssetId,
        walletId: String,
    ) = logRecentAction(assetId, walletId, RecentType.Swap, toAssetId)
}