package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet

@Entity(
    tableName = "banners",
    primaryKeys = ["wallet_id", "asset_id"],
    indices = [Index("event"), Index("wallet_id")],
)
data class DbBanner(
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("asset_id") val assetId: String,
    val chain: Chain?,
    val state: BannerState,
    val event: BannerEvent,
)

fun DbBanner.toModel(wallet: Wallet?, asset: Asset?): Banner {
    return Banner(
        wallet = wallet,
        asset = asset,
        chain = chain,
        state = state,
        event = event,
    )
}

fun Banner.toRecord(state: BannerState? = null): DbBanner {
    return DbBanner(
        walletId = wallet?.id ?: "",
        assetId = asset?.id?.toIdentifier() ?: "",
        chain = chain,
        event = event,
        state = state ?: this.state,
    )
}