package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbAssetInfo
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.AssetPriceInfo
import com.gemwallet.android.model.Balance
import com.gemwallet.android.model.Balances
import com.google.gson.Gson
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

class AssetInfoMapper(private val gson: Gson = Gson()) : Mapper<List<DbAssetInfo>, List<AssetInfo>> {

    override fun asDomain(entity: List<DbAssetInfo>): List<AssetInfo> {
        return entity.groupBy { it.id + it.address }.mapNotNull { records ->
            val entity = records.value.firstOrNull() ?: return@mapNotNull null
            val assetId = entity.id.toAssetId() ?: return@mapNotNull null

            val balances = Balances(
                records.value.mapNotNull {
                    if (it.amount != null && it.balanceType != null) {
                        AssetBalance(assetId, Balance(it.balanceType, it.amount))
                    } else {
                        null
                    }
                }
            )

            val currency = Currency.entries.firstOrNull { it.string == entity.priceCurrency }

            AssetInfo(
                owner = Account(
                    chain = entity.chain,
                    address = entity.address,
                    derivationPath = entity.derivationPath,
                    extendedPublicKey = entity.extendedPublicKey,
                ),
                asset = Asset(
                    id = assetId,
                    name = entity.name,
                    symbol = entity.symbol,
                    decimals = entity.decimals,
                    type = entity.type,
                ),
                balances = balances,
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
                    isEnabled = entity.visible ?: true,
                    isBuyEnabled = entity.isBuyEnabled,
                    isSwapEnabled = entity.isSwapEnabled,
                    isStakeEnabled = entity.isStakeEnabled,
                    isPinned = entity.pinned ?: false,
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

    override fun asEntity(domain: List<AssetInfo>): List<DbAssetInfo> {
        throw IllegalAccessError()
    }
}