package com.gemwallet.android.features.asset.chart.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset.chart.models.ChartUIModel
import com.gemwallet.android.features.asset.chart.models.PricePoint
import com.gemwallet.android.features.asset.navigation.assetIdArg
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.format
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChartViewModel @Inject constructor(
    private val assetsRepository: AssetsRepository,
    private val gemApiClient: GemApiClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var requestChartData: Deferred<ChartUIModel>? = null
    private val assetIdStr = savedStateHandle.getStateFlow<String?>(assetIdArg, null)
    private val assetInfo = assetIdStr.flatMapLatest {
        assetsRepository.getAssetInfo(assetId = it?.toAssetId() ?: return@flatMapLatest emptyFlow())
    }
    private val chartState = MutableStateFlow(ChartState())
    val chartUIState = chartState.map { ChartUIModel.State(it.loading, it.period) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ChartUIModel.State())

    val chartUIModel = assetInfo.combine(chartState) { assetInfo, chartState ->
        if (requestChartData != null && requestChartData?.isActive == true) {
            requestChartData?.cancel()
        }
        withContext(Dispatchers.IO) {
            requestChartData = async {
                request(
                    assetInfo,
                    chartState.period
                )
            }
        }
        try {
            requestChartData?.await() ?: throw IllegalStateException()
        } catch (err: Throwable) {
            ChartUIModel()
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, ChartUIModel())


    suspend fun request(assetInfo: AssetInfo, period: ChartPeriod): ChartUIModel {
        val currency = assetInfo.price?.currency ?: Currency.USD

        val prices = gemApiClient.getChart(
            assetId = assetInfo.asset.id.toIdentifier(),
            currency = currency.string,
            period = period.string
        ).getOrNull()?.prices?.sortedBy { it.timestamp } ?: emptyList()

        val periodStartPrice = prices.firstOrNull()?.value ?: 0.0f
        val currentPoint = if (assetInfo.price == null) null else {
            PricePoint(
                y = assetInfo.price.price.price.toFloat(),
                yLabel = Fiat(assetInfo.price.price.price).format(0, currency.string, 2, dynamicPlace = true),
                timestamp = System.currentTimeMillis(),
                percentage = PriceUIState.formatPercentage(assetInfo.price.price.priceChangePercentage24h, showZero = true),
                priceState = PriceUIState.getState(assetInfo.price.price.priceChangePercentage24h),
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

        chartState.update { it.copy(loading = false) }

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
    )
}