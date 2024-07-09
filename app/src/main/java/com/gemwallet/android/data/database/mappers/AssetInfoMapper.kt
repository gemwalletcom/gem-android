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
            val first = records.value.firstOrNull() ?: return@mapNotNull null
            val assetId = first.id.toAssetId() ?: return@mapNotNull null

            val balances = Balances(
                records.value.mapNotNull {
                    if (it.amount != null && it.balanceType != null) {
                        AssetBalance(assetId, Balance(it.balanceType, it.amount))
                    } else {
                        null
                    }
                }
            )

            val currency = Currency.entries.firstOrNull { it.string == first.priceCurrency }

            AssetInfo(
                owner = Account(
                    chain = first.chain,
                    address = first.address,
                    derivationPath = first.derivationPath,
                    extendedPublicKey = first.extendedPublicKey,
                ),
                asset = Asset(
                    id = assetId,
                    name = first.name,
                    symbol = first.symbol,
                    decimals = first.decimals,
                    type = first.type,
                ),
                balances = balances,
                price = if (first.priceValue != null && currency != null) {
                    AssetPriceInfo(
                        currency = currency,
                        price = AssetPrice(
                            assetId = assetId.toIdentifier(),
                            price = first.priceValue,
                            priceChangePercentage24h = first.priceDayChanges ?: 0.0,
                        )
                    )
                } else null,
                metadata = AssetMetaData(
                    isEnabled = first.isVisible,
                    isBuyEnabled = first.isBuyEnabled,
                    isSwapEnabled = first.isSwapEnabled,
                    isStakeEnabled = first.isStakeEnabled,
                ),
                links = if (first.links != null) gson.fromJson(
                    first.links,
                    AssetLinks::class.java
                ) else null,
                market = if (first.market != null) gson.fromJson(
                    first.market,
                    AssetMarket::class.java
                ) else null,
                rank = first.rank,
                walletName = first.walletName,
                walletType = first.walletType,
                stakeApr = first.stakingApr,
            )
        }
    }

    override fun asEntity(domain: List<AssetInfo>): List<DbAssetInfo> {
        throw IllegalAccessError()
    }
}