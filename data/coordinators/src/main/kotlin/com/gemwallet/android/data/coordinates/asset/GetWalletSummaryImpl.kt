package com.gemwallet.android.data.coordinates.asset

import com.gemwallet.android.application.assets.coordinators.GetWalletSummary
import com.gemwallet.android.cases.banners.HasMultiSign
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.domains.price.values.EquivalentValue
import com.gemwallet.android.domains.wallet.aggregates.WalletSummaryAggregate
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.isSwapSupport
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class GetWalletSummaryImpl(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val hasMultiSign: HasMultiSign,
    private val userConfig: UserConfig,
) : GetWalletSummary {

    override fun getWalletSummary(): Flow<WalletSummaryAggregate?> {
        return combine(
            sessionRepository.session(),
            assetsRepository.getAssetsInfo(),
            sessionRepository.session().filterNotNull().flatMapLatest { hasMultiSign.hasMultiSign(it.wallet) },
            userConfig.isHideBalances(),
        ) { session, assets, hasMultiSign, hideBalances ->
            val wallet = session?.wallet ?: return@combine null
            val currency = session.currency

            val (totalValue, changedValue) = assets.map {
                val current = it.balance.fiatTotalAmount.toBigDecimal()
                val changed = current * ((it.price?.price?.priceChangePercentage24h ?: 0.0) / 100).toBigDecimal()
                Pair(current, changed)
            }.fold(Pair(BigDecimal.ZERO, BigDecimal.ZERO)) { acc, pair ->
                Pair(acc.first + pair.first, acc.second + pair.second)
            }
            val changedPercentages = (changedValue.toDouble() / (totalValue.toDouble() / 100.0)).let {
                if (it.isNaN()) 0.0 else it
            }
            val icon = when (wallet.type) {
                WalletType.Multicoin -> null
                else -> wallet.accounts.firstOrNull()?.chain?.asset()
            }

            val isSwapEnabled = when (wallet.type) {
                WalletType.Multicoin -> true
                WalletType.Single,
                WalletType.PrivateKey -> wallet.accounts.firstOrNull()?.chain?.isSwapSupport() == true
                WalletType.View -> false
            }

            WalletSummaryAggregateImpl(
                walletType = wallet.type,
                walletName = wallet.name,
                walletIcon = icon,
                walletTotalValue = if (hideBalances) "✱✱✱✱✱✱" else currency.format(totalValue, dynamicPlace = true),
                changedValue = if (hideBalances) null else object : EquivalentValue {
                    override val currency: Currency = currency
                    override val value: Double = changedValue.toDouble()
                    override val changePercentage: Double = changedPercentages

                },
                isOperationsAvailable = !hasMultiSign,
                isSwapAvailable = isSwapEnabled,
            )
        }
    }
}

class WalletSummaryAggregateImpl(
    override val walletType: WalletType,
    override val walletName: String,
    override val walletIcon: Any?,
    override val walletTotalValue: String,
    override val changedValue: EquivalentValue?,
    override val isOperationsAvailable: Boolean,
    override val isSwapAvailable: Boolean,
) : WalletSummaryAggregate