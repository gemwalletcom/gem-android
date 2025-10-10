package com.gemwallet.features.asset.presents.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.math.getRelativeDate
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.models.PriceState
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.space8
import com.gemwallet.features.asset.viewmodels.chart.models.PricePoint
import com.gemwallet.features.asset.viewmodels.chart.viewmodels.ChartViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

@Composable
fun Chart(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiModel by viewModel.chartUIModel.collectAsStateWithLifecycle()
    val state by viewModel.chartUIState.collectAsStateWithLifecycle()

    val chartPoints = uiModel.chartPoints
    val points = chartPoints.map { it.y }
    val min = points.minOrNull() ?: 0f
    val max = points.maxOrNull() ?: 0f
    val modelProducer = remember { CartesianChartModelProducer() }
    var price by remember { mutableStateOf<PricePoint?>(null) }

    val persistentLabel = TextComponent(
        color = MaterialTheme.colorScheme.secondary.toArgb(),
        margins = Insets(4f, 4f)
    )
    val minIndex = if (uiModel.chartPoints.isNotEmpty()) uiModel.chartPoints.indexOfFirst { it.y == min } else 0
    val maxIndex = if (uiModel.chartPoints.isNotEmpty()) uiModel.chartPoints.indexOfFirst { it.y == max } else 0
    
    val persistentMarkers = if (points.isNotEmpty() && chartPoints.isNotEmpty()) {
        mapOf(
            minIndex to rememberDefaultCartesianMarker(
                persistentLabel,
                labelPosition = DefaultCartesianMarker.LabelPosition.Bottom,
                valueFormatter = { _, targets ->
                    chartPoints[minIndex].yLabel ?: "~"
                }
            ),
            maxIndex to rememberDefaultCartesianMarker(
                persistentLabel,
                valueFormatter = { _, targets ->
                    chartPoints[maxIndex].yLabel ?: "~"
                }
            )
        )
    } else {
        emptyMap()
    }

    Column {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val point = if (state.loading || state.period != uiModel.period) {
                null
            } else {
                if (price == null) uiModel.currentPoint else price
            }
            PriceInfo(
                priceValue = point?.yLabel ?: "",
                changedPercentages = point?.percentage ?: "",
                state = point?.priceState ?: PriceState.None,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                internalPadding = space8
            )
            Text(
                text = getRelativeDate(price?.timestamp ?: 0L),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer16()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            when {
                state.loading || state.period != uiModel.period -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    strokeWidth = 1.dp
                )
                state.empty -> ChartError()
                points.isEmpty() -> Spacer16()
                else -> {
                    LaunchedEffect(uiModel.period, chartPoints) {
                        withContext(Dispatchers.Default) {
                            modelProducer.runTransaction {
                                lineSeries {
                                    series(
                                        x = List(points.size) { index -> index },
                                        y = points,
                                    )
                                }
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
                            rememberLineCartesianLayer(
                                pointSpacing = 0.1.dp,
                                rangeProvider = if (min.isFinite() && max.isFinite() && min != max) {
                                    CartesianLayerRangeProvider.fixed(minY = min.toDouble(), maxY = max.toDouble())
                                } else {
                                    CartesianLayerRangeProvider.auto()
                                }
                            ),
                            marker = rememberMarker(labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint),
                            persistentMarkers = { extraStore ->
                                persistentMarkers.forEach { index, marker ->
                                    marker at index
                                }
                            },
                            markerVisibilityListener = object : CartesianMarkerVisibilityListener {
                                override fun onHidden(marker: CartesianMarker) {
                                    price = null
                                }

                                override fun onShown(
                                    marker: CartesianMarker,
                                    targets: List<CartesianMarker.Target>
                                ) {
                                    val index = targets.first().x.toInt()
                                    if (index > 0 && index < chartPoints.size) {
                                        price = chartPoints[index]
                                    }
                                }

                                override fun onUpdated(
                                    marker: CartesianMarker,
                                    targets: List<CartesianMarker.Target>
                                ) {
                                    price = chartPoints[
                                        min(
                                            chartPoints.size - 1,
                                            targets.first().x.toInt()
                                        )
                                    ]
                                }
                            },
                        ),
                    )
                }
            }
        }
        Spacer16()
        PeriodsPanel(state.period, viewModel::setPeriod)
    }
}

@Composable
fun ChartError() {
    Box(modifier = Modifier
        .fillMaxSize()
        .defaultPadding()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.errors_error_occured),
        )
    }
}


@Composable
private fun rememberMarker(
    labelPosition: DefaultCartesianMarker.LabelPosition,
    guideline: LineComponent? = rememberMarketGuideLine(),
): DefaultCartesianMarker {
    val indicator = rememberShapeComponent(
        shape = CorneredShape.Pill,
        fill = Fill(Color.White.toArgb()),
        strokeThickness = 2.dp,
        strokeFill = Fill(MaterialTheme.colorScheme.primary.toArgb()),
    )
    return rememberDefaultCartesianMarker(
        valueFormatter = { _, _ -> ""},
        label = rememberTextComponent(
            color = Color.Black,
            background = null,
        ),
        indicator = { indicator },
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

