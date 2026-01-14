package com.gemwallet.android.data.coordinates.pricealerts

import androidx.compose.runtime.Stable
import com.gemwallet.android.application.pricealerts.coordinators.GetPriceAlerts
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.domains.pricealerts.aggregates.PriceAlertDataAggregate
import com.gemwallet.android.domains.pricealerts.aggregates.PriceAlertType
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetPriceInfo
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import com.wallet.core.primitives.PriceAlertDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class GetPriceAlertsImpl(
    private val priceAlertRepository: PriceAlertRepository,
    private val assetsRepository: AssetsRepository,
) : GetPriceAlerts {
    override fun getPriceAlerts(assetId: AssetId?): Flow<List<PriceAlertDataAggregate>> {
        return priceAlertRepository.getPriceAlerts(assetId)
            .flatMapLatest { items ->
                val index = items.groupBy { it.priceAlert.assetId.toIdentifier() }
                assetsRepository.getTokensInfo(index.keys.toList()).mapLatest { items ->
                    items.mapNotNull { assetInfo ->
                        index[assetInfo.id().toIdentifier()]?.mapNotNull { item ->
                            PriceAlertDataAggregateImpl(
                                id = item.id,
                                asset = assetInfo.asset,
                                assetPrice = assetInfo.price ?: return@mapNotNull null,
                                priceAlert = item.priceAlert,
                            )
                        }
                    }.flatten()
                }
            }
    }
}

@Stable
class PriceAlertDataAggregateImpl(
    override val id: Int,
    val asset: Asset,
    val assetPrice: AssetPriceInfo,
    val priceAlert: PriceAlert
) : PriceAlertDataAggregate {
    override val assetId: AssetId = asset.id
    override val icon: Any = asset
    override val title: String = asset.name
    override val titleBadge: String = asset.symbol.uppercase()

    override val priceState: PriceState get() {
        val alertPrice = priceAlert.price

        return when (priceAlert.priceDirection) {
            PriceAlertDirection.Up -> PriceState.Up
            PriceAlertDirection.Down -> PriceState.Down
            else -> if (alertPrice != null) {
                when {
                    alertPrice > assetPrice.price.price -> PriceState.Up
                    else -> PriceState.Down
                }
            } else {
                assetPrice.price.priceChangePercentage24h.let {
                    if (it > 0) {
                        PriceState.Up
                    } else {
                        PriceState.Up
                    }
                }
            }
        }
    }

    override val price: String
        get() = priceAlert.price?.let {
            Currency.entries.firstOrNull { it.string == priceAlert.currency }?.format(it) ?: ""
        } ?: assetPrice.currency.format(assetPrice.price.price)

    override val percentage: String = priceAlert.pricePercentChange?.formatAsPercentage(isShowSign = false)
        ?: assetPrice.price.priceChangePercentage24h.formatAsPercentage()

    override val type: PriceAlertType get() {
        val alertPrice = priceAlert.price

        return when {
            priceAlert.priceDirection == PriceAlertDirection.Up -> PriceAlertType.Increase
            priceAlert.priceDirection == PriceAlertDirection.Down -> PriceAlertType.Decrease
            alertPrice != null -> when {
                alertPrice > assetPrice.price.price -> PriceAlertType.Over
                else -> PriceAlertType.Under
            }
            else -> PriceAlertType.Auto
        }
    }
    override val hasTarget: Boolean
        get() = priceAlert.price != null || priceAlert.priceDirection != null

}