package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbAssetInfo
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.AssetPriceInfo
import com.gemwallet.android.model.Balance
import com.google.gson.Gson
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

class AssetInfoMapper(private val gson: Gson = Gson()) : Mapper<List<DbAssetInfo>, List<AssetInfo>, Nothing, Nothing> {

    override fun asDomain(entity: List<DbAssetInfo>, options: (() -> Nothing)?): List<AssetInfo> {
        return entity.groupBy { it.id + it.address }.mapNotNull { records ->
            val entity = records.value.firstOrNull() ?: return@mapNotNull null
            val assetId = entity.id.toAssetId() ?: return@mapNotNull null
            val asset = Asset(
                id = assetId,
                name = entity.name,
                symbol = entity.symbol,
                decimals = entity.decimals,
                type = entity.type,
            )
            val balances = AssetBalance(
                asset = asset,
                balance = Balance(
                    available = entity.balanceAvailable ?: "0",
                    frozen = entity.balanceFrozen ?: "0",
                    locked = entity.balanceLocked ?: "0",
                    staked = entity.balanceStaked ?: "0",
                    pending = entity.balancePending ?: "0",
                    rewards = entity.balanceRewards ?: "0",
                    reserved = entity.balanceReserved ?: "0"
                ),
                balanceAmount = Balance(
                    available = entity.balanceAvailableAmount ?: 0.0,
                    frozen = entity.balanceFrozenAmount ?: 0.0,
                    locked = entity.balanceLockedAmount ?: 0.0,
                    staked = entity.balanceStakedAmount ?: 0.0,
                    pending = entity.balancePendingAmount ?: 0.0,
                    rewards = entity.balanceRewardsAmount ?: 0.0,
                    reserved = entity.balanceReservedAmount ?: 0.0,
                ),
                totalAmount = entity.balanceTotalAmount ?: 0.0,
                fiatTotalAmount = entity.balanceFiatTotalAmount ?: 0.0,
            )

            val currency = Currency.entries.firstOrNull { it.string == entity.priceCurrency }

            AssetInfo(
                owner = Account(
                    chain = entity.chain,
                    address = entity.address,
                    derivationPath = entity.derivationPath,
                    extendedPublicKey = entity.extendedPublicKey,
                ),
                asset = asset,
                balance = balances,
                price = if (entity.priceValue != null && currency != null) {
                    AssetPriceInfo(
                        currency = currency,
                        price = AssetPrice(
                            assetId = assetId.toIdentifier(),
                            price = entity.priceValue,
                            priceChangePercentage24h = entity.priceDayChanges ?: 0.0,
                        )
                    )
                } else null,
                metadata = AssetMetaData(
                    isEnabled = entity.visible != false,
                    isBuyEnabled = entity.isBuyEnabled,
                    isSwapEnabled = entity.isSwapEnabled,
                    isStakeEnabled = entity.isStakeEnabled,
                    isPinned = entity.pinned == true,
                    isSellEnabled = false
                ),
                links = if (entity.links != null) gson.fromJson(
                    entity.links,
                    AssetLinks::class.java
                ) else null,
                market = if (entity.market != null) gson.fromJson(
                    entity.market,
                    AssetMarket::class.java
                ) else null,
                rank = 0, //entity.assetRank,
                walletName = entity.walletName,
                walletType = entity.walletType,
                stakeApr = entity.stakingApr,
                position = entity.listPosition ?: 0,
            )
        }
    }

    override fun asEntity(domain: List<AssetInfo>, options: (() -> Nothing)?): List<DbAssetInfo> {
        throw IllegalAccessError()
    }
}