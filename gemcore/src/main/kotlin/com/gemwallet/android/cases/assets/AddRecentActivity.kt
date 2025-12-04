package com.gemwallet.android.cases.assets

import com.gemwallet.android.model.RecentType
import com.wallet.core.primitives.AssetId

interface AddRecentActivity {
    suspend fun addRecentActivity(
        assetId: AssetId,
        walletId: String,
        type: RecentType,
        toAssetId: AssetId? = null,
    )

    suspend fun addRecentReceive(
        assetId: AssetId,
        walletId: String,
    ) = addRecentActivity(assetId, walletId, RecentType.Receive)

    suspend fun addRecentSend(
        assetId: AssetId,
        walletId: String,
    ) = addRecentActivity(assetId, walletId, RecentType.Send)

    suspend fun addRecentBuy(
        assetId: AssetId,
        walletId: String,
    ) = addRecentActivity(assetId, walletId, RecentType.Buy)

    suspend fun addRecentSwap(
        assetId: AssetId,
        toAssetId: AssetId,
        walletId: String,
    ) = addRecentActivity(assetId, walletId, RecentType.Swap, toAssetId)
}