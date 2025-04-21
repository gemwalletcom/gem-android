package com.gemwallet.android.features.asset.chart.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.asset.chart.components.PeriodsPanel
import com.gemwallet.android.features.asset.chart.components.rememberBottomAxis
import com.gemwallet.android.features.asset.chart.components.rememberTopAxis
import com.gemwallet.android.features.asset.chart.models.PricePoint
import com.gemwallet.android.features.asset.chart.viewmodels.ChartViewModel
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.PriceInfo
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding4
import com.gemwallet.android.ui.components.designsystem.space8
import com.gemwallet.android.ui.components.getRelativeDate
import com.gemwallet.android.ui.models.PriceState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

@Composable
fun Chart(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val uiModel by viewModel.chartUIModel.collectAsStateWithLifecycle()
    val state by viewModel.chartUIState.collectAsStateWithLifecycle()

    val points = uiModel.chartPoints.map { it.y }
    val min = points.minOrNull() ?: 0f
    val max = points.maxOrNull() ?: 0f
    val minIndex = points.indexOf(min)
    val maxIndex = points.indexOf(max)
    val modelProducer = remember { CartesianChartModelProducer.build() }
    var price by remember { mutableStateOf<PricePoint?>(null) }
    Container {
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
                    priceValue =  point?.yLabel ?: "",
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
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                if (state.loading || state.period != uiModel.period) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        strokeWidth = 1.dp
                    )
                } else if (points.isEmpty()) {
                    Spacer16()
                } else {
                    LaunchedEffect(uiModel.period) {
                        withContext(Dispatchers.Default) {
                            modelProducer.tryRunTransaction {
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
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(end = padding4),
                        diffAnimationSpec = null,
                        markerVisibilityListener = object : CartesianMarkerVisibilityListener {
                            override fun onHidden(marker: CartesianMarker) {
                                price = null
                            }

                            override fun onShown(
                                marker: CartesianMarker,
                                targets: List<CartesianMarker.Target>
                            ) {
                                val index = targets.first().x.toInt()
                                if (index > 0 && index < uiModel.chartPoints.size) {
                                    price = uiModel.chartPoints[index]
                                }
                            }

                            override fun onUpdated(
                                marker: CartesianMarker,
                                targets: List<CartesianMarker.Target>
                            ) {
                                price = uiModel.chartPoints[min(uiModel.chartPoints.size - 1, targets.first().x.toInt())]
                            }
                        },
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(
                                spacing = 0.1.dp,
                                lines = listOf(
                                    rememberLineSpec(
                                        shader = DynamicShader.color(MaterialTheme.colorScheme.primary),
                                        backgroundShader = null,
                                    ),
                                ),
                                axisValueOverrider = AxisValueOverrider.fixed(
                                    minY = min,
                                    maxY = max
                                ),
                            ),
                            topAxis = rememberTopAxis(
                                valueFormatter = { value, _, _ ->
                                    if (value == maxIndex.toFloat()) uiModel.chartPoints[maxIndex].yLabel
                                        ?: "" else ""
                                },
                            ),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = { value, _, _ ->
                                    if (value == minIndex.toFloat()) uiModel.chartPoints[minIndex].yLabel
                                        ?: "" else ""
                                }
                            ),
                        ),
                        marker = rememberMarker(labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint),
                        modelProducer = modelProducer,
                    )
                }
            }
            Spacer16()
            PeriodsPanel(state.period, viewModel::setPeriod)
        }
    }
}


@Composable
private fun rememberMarker(
    labelPosition: DefaultCartesianMarker.LabelPosition,
    guideline: LineComponent? = rememberMarketGuideLine(),
): DefaultCartesianMarker {
    return rememberDefaultCartesianMarker(
        valueFormatter = { _, _ -> ""},
        label = rememberTextComponent(
            color = Color.Black,
            background = null,
        ),
        indicator = rememberShapeComponent(
            shape = Shape.Pill,
            color = Color.White,
            strokeWidth = 2.dp,
            strokeColor = MaterialTheme.colorScheme.primary,
        ),
        labelPosition = labelPosition,
        guideline = guideline,
    )
}

@Composable
private fun rememberMarketGuideLine(): LineComponent {
    return rememberLineComponent(
        color = MaterialTheme.colorScheme.outlineVariant,
        shape = remember {
            Shape.dashed(
                shape = Shape.Pill,
                dashLength = 4.dp,
                gapLength = 8.dp,
            )
        },
    )
}