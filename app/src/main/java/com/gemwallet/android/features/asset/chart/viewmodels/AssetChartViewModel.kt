package com.gemwallet.android.features.asset.chart.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset.chart.models.AssetChartSceneState
import com.gemwallet.android.features.asset.chart.models.PricePoint
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.ChartValue
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class AssetChartViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val gemApiClient: GemApiClient,
) : ViewModel() {
    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }.stateIn(viewModelScope, SharingStarted.Eagerly,
        AssetChartSceneState.Loading
    )

    fun request(assetId: AssetId, period: ChartPeriod) {
        state.update { it.copy(loading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession() ?: return@launch
            val assetInfo = assetsRepository.getById(session.wallet, assetId).getOrNull()?.firstOrNull()
            state.update {
                State(
                    assetInfo = assetInfo,
                    assetLinks = assetInfo?.links,
                    currency = session.currency,
                    period = period,
                )
            }

            val prices = gemApiClient.getChart(
                assetId.toIdentifier(),
                session.currency.string,
                period.string
            ).getOrNull()?.prices?.sortedBy { it.timestamp }
            state.update {
                it.copy(
                    loading = false,
                    prices = prices ?: emptyList(),
                )
            }
        }
    }

    fun reset() {
        state.update { State() }
    }

    private data class State(
        val loading: Boolean = true,
        val assetInfo: AssetInfo? = null,
        val currency: Currency? = null,
        val period: ChartPeriod = ChartPeriod.Day,
        val prices: List<ChartValue> = emptyList(),
        val assetLinks: AssetLinks? = null,
    ) {
        fun toUIState(): AssetChartSceneState {
            return when {
                assetInfo == null || currency == null -> AssetChartSceneState.Loading
                else -> {
                    val asset = assetInfo.asset
                    val periodStartPrice = prices.firstOrNull()?.value ?: 0.0f
                    val currentPoint = if (assetInfo.price == null) null else {
                        PricePoint(
                            y = assetInfo.price.price.price.toFloat(),
                            yLabel = Fiat(assetInfo.price.price.price).format(0, currency.string, 2, dynamicPlace = true),
                            timestamp = System.currentTimeMillis(),
                            percentage = PriceUIState.formatPercentage(assetInfo.price.price.priceChangePercentage24h),
                            priceState = PriceUIState.getState(assetInfo.price.price.priceChangePercentage24h),
                        )
                    }
                    AssetChartSceneState.Chart(
                        loading = loading,
                        assetId = asset.id,
                        assetTitle = asset.name,
                        assetLinkTitle = "CoinGecko",
                        assetLink = assetLinks?.coingecko ?: "",
                        assetLinks = assetLinks,
                        currency = currency,
                        marketCap = Fiat(assetInfo.market?.marketCap ?: 0.0).format(0, currency.string, 0),
                        circulatingSupply = Crypto(
                            BigInteger.valueOf(
                                assetInfo.market?.circulatingSupply?.toLong() ?: 0L
                            )
                        ).format(0, asset.symbol, 0),
                        totalSupply = Crypto(BigInteger.valueOf(assetInfo.market?.totalSupply?.toLong() ?: 0L)).format(
                            0,
                            asset.symbol,
                            0
                        ),
                        period = period,
                        currentPoint = currentPoint,
                        chartPoints = prices.map {
                            val percent = (it.value - periodStartPrice) / periodStartPrice * 100.0
                            PricePoint(
                                y = it.value,
                                yLabel = Fiat(it.value).format(0, currency.string, 2, dynamicPlace = true),
                                timestamp = it.timestamp * 1000L,
                                percentage = PriceUIState.formatPercentage(percent, showZero = true),
                                priceState = PriceUIState.getState(percent),
                            )
                        } + if (currentPoint != null) listOf(currentPoint) else emptyList()
                    )
                }
            }

        }
    }
}

