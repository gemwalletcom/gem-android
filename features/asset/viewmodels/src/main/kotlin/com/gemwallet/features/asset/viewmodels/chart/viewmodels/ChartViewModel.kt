package com.gemwallet.features.asset.viewmodels.chart.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.features.asset.viewmodels.chart.models.ChartUIModel
import com.gemwallet.features.asset.viewmodels.chart.models.PricePoint
import com.gemwallet.features.asset.viewmodels.assetIdArg
import com.gemwallet.android.ui.models.PriceUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.format
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChartViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val gemApiClient: GemApiClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val assetIdStr = savedStateHandle.getStateFlow<String?>(assetIdArg, null)
    private val assetInfo = assetIdStr
        .flatMapLatest {
            val assetId = it?.toAssetId() ?: return@flatMapLatest emptyFlow()
            assetsRepository.getTokenInfo(assetId).filterNotNull()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val chartState = MutableStateFlow(ChartState())
    val chartUIState = chartState.map { ChartUIModel.State(it.loading, it.period, it.empty) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ChartUIModel.State())

    val chartUIModel = assetInfo.combine(chartState) { assetInfo, chartState -> Pair(assetInfo, chartState) }
        .mapLatest { state ->
            val assetInfo = state.first ?: return@mapLatest ChartUIModel()
            val chartState = state.second
            try {
                request(assetInfo, chartState.period)
            } catch (_: Throwable) {
                ChartUIModel()
            }
        }
        .flowOn(Dispatchers.IO)
    .flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, ChartUIModel())


    suspend fun request(assetInfo: AssetInfo, period: ChartPeriod): ChartUIModel {
        val currency = assetInfo.price?.currency ?: Currency.USD

        val prices = try {
            gemApiClient.getChart(
                assetId = assetInfo.asset.id.toIdentifier(),
                currency = currency.string,
                period = period.string
            ).prices.sortedBy { it.timestamp }
        } catch (_: Throwable) {
            emptyList()
        }

        val periodStartPrice = prices.firstOrNull()?.value ?: 0.0f
        val currentPoint = if (assetInfo.price == null) null else {
            val percentage = if (period == ChartPeriod.Day) {
                assetInfo.price!!.price.priceChangePercentage24h
            } else {
                (assetInfo.price!!.price.price - periodStartPrice) / periodStartPrice * 100
            }
            PricePoint(
                y = assetInfo.price!!.price.price.toFloat(),
                yLabel = currency.format(assetInfo.price!!.price.price),
                timestamp = System.currentTimeMillis(),
                percentage = PriceUIState.formatPercentage(percentage, showZero = true),
                priceState = PriceUIState.getState(percentage),
            )
        }
        val chartPoints = prices.map {
            val percent = (it.value - periodStartPrice) / periodStartPrice * 100.0
            PricePoint(
                y = it.value,
                yLabel = currency.format(it.value, 2, dynamicPlace = true),
                timestamp = it.timestamp * 1000L,
                percentage = PriceUIState.formatPercentage(percent, showZero = true),
                priceState = PriceUIState.getState(percent),
            )
        } + if (currentPoint != null) listOf(currentPoint) else emptyList()

        chartState.update { it.copy(loading = false, empty = currentPoint == null && chartPoints.isEmpty()) }

        return ChartUIModel(
            period = period,
            currentPoint = currentPoint,
            chartPoints = chartPoints,
        )
    }

    fun setPeriod(period: ChartPeriod) {
        if (period == chartUIState.value.period) {
            return
        }
        chartState.update { ChartState(period) }
    }

    private data class ChartState(
        val period: ChartPeriod = ChartPeriod.Day,
        val loading: Boolean = true,
        val empty: Boolean = false,
    )
}