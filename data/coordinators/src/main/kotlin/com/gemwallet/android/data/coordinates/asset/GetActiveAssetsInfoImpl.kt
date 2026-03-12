package com.gemwallet.android.data.coordinates.asset

import androidx.compose.runtime.Stable
import com.gemwallet.android.application.assets.coordinators.GetActiveAssetsInfo
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.domains.asset.aggregates.AssetInfoDataAggregate
import com.gemwallet.android.domains.price.values.PriceableValue
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.format
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetActiveAssetsInfoImpl(
    private val assetsRepository: AssetsRepository,
) : GetActiveAssetsInfo {

    override fun getAssetsInfo(hideBalance: Boolean): Flow<List<AssetInfoDataAggregate>> {
        return assetsRepository.getAssetsInfo().map { items -> items.map { AssetInfoDataAggregateImpl(it, hideBalance) } }
    }
}

@Stable
class AssetInfoDataAggregateImpl(
    private val assetInfo: AssetInfo,
    private val hideBalance: Boolean,
) : AssetInfoDataAggregate {
    override val id: AssetId = assetInfo.asset.id

    override val title: String = assetInfo.asset.name

    override val icon: Any = assetInfo.asset

    override val balance: String
        get() = if (hideBalance) "*****" else assetInfo.asset.format(
            humanAmount = assetInfo.balance.totalAmount,
            decimalPlace = 2,
            maxDecimals = 4,
            dynamicPlace = true
        )

    override val balanceEquivalent: String
        get() {
            val price = assetInfo.price?.price?.price ?: 0.0
            val fiat = if (price == 0.0) null else assetInfo.balance.totalAmount * price
            return if (hideBalance) "*****" else fiat?.let { currency.format(it, dynamicPlace = true) } ?: ""
        }

    override val isZeroBalance: Boolean
        get() = assetInfo.balance.totalAmount == 0.0

    override val price: PriceableValue?
        get() = assetInfo.price?.price?.let {
            PriceableValueImpl(
                currency = currency,
                priceValue = it.price,
                dayChangePercentage = it.priceChangePercentage24h,
            )
        }

    override val position: Int
        get() = assetInfo.position

    override val pinned: Boolean
        get() = assetInfo.metadata?.isPinned == true

    override val accountAddress: String
        get() = assetInfo.owner?.address ?: ""

    private val currency: Currency = assetInfo.price?.currency ?: Currency.USD

    private class PriceableValueImpl(
        override val currency: Currency,
        override val priceValue: Double?,
        override val dayChangePercentage: Double?
    ) : PriceableValue
}