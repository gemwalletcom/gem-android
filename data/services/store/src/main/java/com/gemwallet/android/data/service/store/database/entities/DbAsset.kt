package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.isSwapSupport
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetBasic
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLink
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "assets", primaryKeys = ["owner_address", "id"])
data class DbAssetOld(
    @ColumnInfo("owner_address") val address: String,
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val chain: Chain,
    @ColumnInfo("is_buy_enabled") val isBuyEnabled: Boolean = false,
    @ColumnInfo("is_swap_enabled") val isSwapEnabled: Boolean = false,
    @ColumnInfo("is_stake_enabled") val isStakeEnabled: Boolean = false,
    @ColumnInfo("staking_apr") val stakingApr: Double? = null,
    @ColumnInfo("links") val links: String? = null,
    @ColumnInfo("market") val market: String? = null,
    @ColumnInfo("rank") val rank: Int = 0,
    @ColumnInfo("created_at") val createdAt: Long = 0,
    @ColumnInfo("updated_at") val updatedAt: Long = 0,
)

@Entity(tableName = "asset", primaryKeys = ["id"])
data class DbAsset(
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val chain: Chain,
    @ColumnInfo("is_enabled") val isEnabled: Boolean = true, // System flag
    @ColumnInfo("is_buy_enabled") val isBuyEnabled: Boolean = false,
    @ColumnInfo("is_sell_enabled") val isSellEnabled: Boolean = false,
    @ColumnInfo("is_swap_enabled") val isSwapEnabled: Boolean = false,
    @ColumnInfo("is_stake_enabled") val isStakeEnabled: Boolean = false,
    @ColumnInfo("staking_apr") val stakingApr: Double? = null,
    @ColumnInfo("rank") val rank: Int = 0,
    @ColumnInfo("updated_at") val updatedAt: Long = 0,
)

@Entity(
    tableName = "asset_links",
    primaryKeys = ["asset_id", "name"],
    foreignKeys = [ForeignKey(DbAsset::class, ["id"], ["asset_id"], onDelete = ForeignKey.Companion.CASCADE)],
)
data class DbAssetLink(
    @ColumnInfo("asset_id") val assetId: String,
    val name: String,
    val url: String,
)

@Entity(
    tableName = "asset_market",
    primaryKeys = ["asset_id"],
    foreignKeys = [ForeignKey(DbAsset::class, ["id"], ["asset_id"], onDelete = ForeignKey.CASCADE)],
)
data class DbAssetMarket(
    @ColumnInfo("asset_id") val assetId: String,
    val marketCap: Double? = null,
    val marketCapFdv: Double? = null,
    val marketCapRank: Int? = null,
    val totalVolume: Double? = null,
    val circulatingSupply: Double? = null,
    val totalSupply: Double? = null,
    val maxSupply: Double? = null
)

@Entity(
    tableName = "asset_wallet",
    primaryKeys = ["asset_id", "wallet_id", "account_address"],
    foreignKeys = [
        ForeignKey(DbAsset::class, ["id"], ["asset_id"], onDelete = ForeignKey.Companion.CASCADE),
        ForeignKey(DbWallet::class, ["id"], ["wallet_id"], onDelete = ForeignKey.Companion.CASCADE),
    ],
)
data class DbAssetWallet(
    @ColumnInfo("asset_id") val assetId: String,
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("account_address") val accountAddress: String,
)

fun List<DbAsset>.toModel() = mapNotNull { it.toModel() }

fun DbAsset.toModel(): Asset? {
    return Asset(
        id = id.toAssetId() ?: return null,
        name = name,
        symbol = symbol,
        decimals = decimals,
        type = type,
    )
}

fun Asset.toRecord(defaultScore: Int) = DbAsset(
    id = id.toIdentifier(),
    chain = chain(),
    name = name,
    symbol = symbol,
    decimals = decimals,
    type = type,
    isSwapEnabled = chain().isSwapSupport(),
    isBuyEnabled = defaultScore >= 40,
    updatedAt = System.currentTimeMillis(),
)

fun AssetFull.toRecord() = DbAsset(
    id = asset.id.toIdentifier(),
    chain = asset.chain(),
    name = asset.name,
    symbol = asset.symbol,
    decimals = asset.decimals,
    type = asset.type,
    isBuyEnabled = properties.isBuyable == true,
    isSellEnabled = properties.isSellable == true,
    isStakeEnabled = properties.isStakeable == true,
    isSwapEnabled = asset.chain().isSwapSupport(),
    stakingApr = properties.stakingApr,
    rank = score.rank,
)

fun AssetBasic.toRecord() = DbAsset(
    id = asset.id.toIdentifier(),
    chain = asset.chain(),
    name = asset.name,
    symbol = asset.symbol,
    decimals = asset.decimals,
    type = asset.type,
    isBuyEnabled = properties.isBuyable == true,
    isSellEnabled = properties.isSellable == true,
    isStakeEnabled = properties.isStakeable == true,
    isSwapEnabled = asset.chain().isSwapSupport(),
    stakingApr = properties.stakingApr,
    rank = score.rank,
)

fun List<AssetLink>.toAssetLinkRecord(assetId: AssetId) = map { it.toRecord(assetId) }

fun AssetLink.toRecord(assetId: AssetId) = DbAssetLink(
    assetId = assetId.toIdentifier(),
    name = name,
    url = url,
)

fun List<AssetFull>.toAssetFullRecord() = map { it.toRecord() }

fun List<DbAssetLink>.toAssetLinksModel() = map { it.toModel() }

fun Flow<List<DbAssetLink>>.toAssetLinksModel() = map { it.toAssetLinksModel() }

fun DbAssetLink.toModel() = AssetLink(name = name, url = url)

fun  AssetMarket.toRecord(assetId: AssetId) = DbAssetMarket(
    assetId = assetId.toIdentifier(),
    marketCap = marketCap,
    marketCapFdv = marketCapFdv,
    marketCapRank = marketCapRank,
    totalVolume = totalVolume,
    circulatingSupply = circulatingSupply,
    totalSupply = totalSupply,
    maxSupply = maxSupply,
)

fun  DbAssetMarket.toModel() = AssetMarket(
    marketCap = marketCap,
    marketCapFdv = marketCapFdv,
    marketCapRank = marketCapRank,
    totalVolume = totalVolume,
    circulatingSupply = circulatingSupply,
    totalSupply = totalSupply,
    maxSupply = maxSupply,
)