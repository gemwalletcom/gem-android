@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.PeriodsPanel
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.theme.mainActionHeight
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingLarge
import com.gemwallet.android.ui.theme.pendingColor
import com.gemwallet.android.ui.theme.space8
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberTop
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.dashedShape
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.candlestickSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min


fun LazyListScope.candyChart(
    data: List<ChartCandleStick>,
    period: ChartPeriod,
    entry: Double?,
    liquidation: Double?,
    stopLoss: Double?,
    takeProfit: Double?,
    onPeriodSelect: (ChartPeriod) -> Unit
) {
    item {
        CandyChart(
            period = period,
            data = data,
            entry = entry,
            liquidation = liquidation,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
        )
    }

    item {
        PeriodsPanel(period, onPeriodSelect)
    }
}

@Composable
private fun CandyChart(
    period: ChartPeriod,
    data: List<ChartCandleStick>,
    entry: Double?,
    liquidation: Double?,
    stopLoss: Double?,
    takeProfit: Double?,
) {
    var price by remember { mutableStateOf<ChartCandleStick?>(null) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(paddingDefault),
    ) {
        val point = if (data.isEmpty()) {
            null
        } else {
            if (price == null) data.last() else price
        }
        PriceInfo(
            priceValue = Currency.USD.format(point?.close ?: 0.0), // TODO: Out to entity
            changedPercentages = data.firstOrNull()?.let { periodStart ->
                point?.let { it.close / (periodStart.open * 0.01) - 100.0}?.formatAsPercentage() ?: ""
            } ?: "",
            state = when {
                (point?.close ?: 0.0) - (point?.open ?: 0.0) < 0 -> PriceState.Down
                (point?.close ?: 0.0) - (point?.open ?: 0.0) > 0 -> PriceState.Up
                else -> PriceState.None

            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            internalPadding = space8
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp)
        ) {
            if (data.isEmpty()) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(paddingLarge).align(Alignment.Center),
                    stroke = Stroke(
                        width = with(LocalDensity.current) { 2.dp.toPx() },
                        cap = StrokeCap.Round,
                    ),
                    trackStroke = Stroke(
                        width = with(LocalDensity.current) { 2.dp.toPx() },
                        cap = StrokeCap.Round,
                    )
                )
                return
            }
            val min = data.minOfOrNull { it.low } ?: 0.0
            val max = data.maxOfOrNull { it.high } ?: 0.0
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
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
                chart = rememberCartesianChart(
                    rememberCandlestickCartesianLayer(
                        rangeProvider = if (min.isFinite() && max.isFinite() && min != max) {
                            CartesianLayerRangeProvider.fixed(minY = min, maxY = max)
                        } else {
                            CartesianLayerRangeProvider.auto()
                        }
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        line = null,
                        label = null,
                        guideline = rememberAxisGuidelineComponent(shape = dashedShape(dashLength = 10.dp, gapLength = 4.dp)),
                        itemPlacer = remember(min, max) { VerticalAxis.ItemPlacer.step({ (max - min) / 4 }) },
                    ),
                    topAxis = HorizontalAxis.rememberTop(
                        line = null,
                        label = null,
                        guideline = rememberAxisGuidelineComponent(shape = dashedShape(dashLength = 10.dp, gapLength = 4.dp)),
                        itemPlacer = remember(data.size) {
                            HorizontalAxis.ItemPlacer.aligned(
                                { data.size / 8 },
                                addExtremeLabelPadding = false
                            )
                        },
                    ),
                    marker = rememberMarker(labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint),
                    markerVisibilityListener = object : CartesianMarkerVisibilityListener {
                        override fun onHidden(marker: CartesianMarker) {
                            price = null
                        }

                        override fun onShown(
                            marker: CartesianMarker,
                            targets: List<CartesianMarker.Target>
                        ) {
                            val index = targets.first().x.toInt()
                            if (index > 0 && index < data.size) {
                                price = data[index]
                            }
                        }

                        override fun onUpdated(
                            marker: CartesianMarker,
                            targets: List<CartesianMarker.Target>
                        ) {
                            price = data[
                                min(
                                    data.size - 1,
                                    targets.first().x.toInt()
                                )
                            ]
                        }
                    },
                    decorations = listOfNotNull(
                        entry?.let {
                            rememberEntryLine(
                                value = it,
                                labelText = stringResource(R.string.charts_entry),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        liquidation?.let {
                            rememberEntryLine(
                                value = it,
                                labelText = stringResource(R.string.charts_liquidation),
                                color = pendingColor,
                            )
                        },
                        stopLoss?.let {
                            rememberEntryLine(
                                value = it,
                                labelText = stringResource(R.string.charts_stop_loss),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        takeProfit?.let {
                            rememberEntryLine(
                                value = it,
                                labelText = stringResource(R.string.charts_take_profit),
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    )
                ),
                placeholder = {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    ) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(mainActionHeight).align(Alignment.Center),
                            stroke = Stroke(
                                width = with(LocalDensity.current) { 2.dp.toPx() },
                                cap = StrokeCap.Round,
                            ),
                            trackStroke = Stroke(
                                width = with(LocalDensity.current) { 2.dp.toPx() },
                                cap = StrokeCap.Round,
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun rememberEntryLine(
    value: Double,
    labelText: String,
    shape: Shape = dashedShape(dashLength = 2.dp, gapLength = 2.dp),
    color: Color
): HorizontalLine {
    val fill = fill(color)
    val entryLine = rememberLineComponent(
        fill = fill,
        thickness = 1.dp,
        shape = shape,
    )
    val entryLabel = rememberTextComponent(
        margins = insets(start = 6.dp),
        padding = insets(start = 8.dp, end = 8.dp, bottom = 2.dp),
        background = shapeComponent(fill, CorneredShape.rounded(4.dp)),
        textSize = MaterialTheme.typography.labelSmall.fontSize,
        color = Color.White,
    )

    return remember(value, labelText) {
        HorizontalLine(
            y = { value },
            line = entryLine,
            labelComponent = entryLabel,
            label = { labelText },
            verticalLabelPosition = Position.Vertical.Center,
        )
    }
}

@Composable
private fun rememberMarker( // TODO: Fully duplicated (just copy-paste) com.gemwallet.features.asset.presents.chart.Chart.
    labelPosition: DefaultCartesianMarker.LabelPosition,
    guideline: LineComponent? = rememberMarketGuideLine(),
): DefaultCartesianMarker {
//    val indicator = rememberShapeComponent(
//        shape = CorneredShape.Pill,
//        fill = Fill(Color.White.toArgb()),
//        strokeThickness = 2.dp,
//        strokeFill = Fill(MaterialTheme.colorScheme.primary.toArgb()),
//    )
    return rememberDefaultCartesianMarker(
        valueFormatter = { _, _ -> ""},
        label = rememberTextComponent(
            color = Color.Black,
            background = null,
        ),
//        indicator = { indicator },
        labelPosition = labelPosition,
        guideline = guideline,
    )
}

@Composable
private fun rememberMarketGuideLine(): LineComponent {
    return rememberLineComponent(
        fill = Fill(MaterialTheme.colorScheme.outlineVariant.toArgb()),
        shape = remember {
            DashedShape(
                shape = CorneredShape.Pill,
                dashLengthDp = 4f,
                gapLengthDp = 8f,
            )
        },
    )
}