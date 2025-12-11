package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.PeriodsPanel
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.candlestickSeries
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


fun LazyListScope.candyChart(
    data: List<ChartCandleStick>,
    period: ChartPeriod,
    onPeriodSelect: (ChartPeriod) -> Unit
) {
    item {
        CandyChart(period, data)
    }

    item {
        PeriodsPanel(period, onPeriodSelect)
    }
}

@Composable
private fun CandyChart(period: ChartPeriod, data: List<ChartCandleStick>) {
    if (data.isEmpty()) {
        return
    }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(period, data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                candlestickSeries(
                    opening = data.map { it.open },
                    closing = data.map { it.close },
                    low = data.map { it.low },
                    high = data.map { it.high },
                )
            }
        }
    }
    CartesianChartHost(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(end = paddingHalfSmall),
        animateIn = false,
        modelProducer = modelProducer,
        chart = rememberCartesianChart(
            rememberCandlestickCartesianLayer(
//
//                rangeProvider = if (min.isFinite() && max.isFinite() && min != max) {
//                    CartesianLayerRangeProvider.fixed(minY = min.toDouble(), maxY = max.toDouble())
//                } else {
//                    CartesianLayerRangeProvider.auto()
//                }
            ),
        ),
    )
}