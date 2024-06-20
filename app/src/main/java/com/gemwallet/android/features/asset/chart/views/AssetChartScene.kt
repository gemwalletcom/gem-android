package com.gemwallet.android.features.asset.chart.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.asset.chart.components.PeriodButton
import com.gemwallet.android.features.asset.chart.components.rememberBottomAxis
import com.gemwallet.android.features.asset.chart.components.rememberTopAxis
import com.gemwallet.android.features.asset.chart.viewmodels.AssetChartSceneState
import com.gemwallet.android.features.asset.chart.viewmodels.AssetChartViewModel
import com.gemwallet.android.features.asset.chart.viewmodels.PricePoint
import com.gemwallet.android.features.assets.model.PriceState
import com.gemwallet.android.interactors.getDrawableUri
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.PriceInfo
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.getRelativeDate
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding4
import com.gemwallet.android.ui.theme.space8
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
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.ChartPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AssetChartScene(
    assetId: AssetId,
    onCancel: () -> Unit,
) {
    val viewModel: AssetChartViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = assetId) {
        viewModel.request(assetId, ChartPeriod.Day)

        onDispose { viewModel.reset() }
    }

    when (uiState) {
        is AssetChartSceneState.Chart -> {
            val state = uiState as AssetChartSceneState.Chart
            Success(
                loading = state.loading,
                assetTitle = state.assetTitle,
                assetLinkTitle = state.assetLinkTitle,
                assetLink = state.assetLink,
                assetLinks = state.assetLinks,
                marketCap = state.marketCap,
                circulatingSupply = state.circulatingSupply,
                totalSupply = state.totalSupply,
                period = state.period,
                currentPoint = state.currentPoint,
                chartPoints = state.chartPoints,
                onPeriodClick = { viewModel.request(assetId, it) },
                onCancel = onCancel,
            )
        }
        AssetChartSceneState.Loading -> LoadingScene(assetId.chain.string, onCancel)
    }
}

@Composable
private fun Success(
    loading: Boolean,
    assetTitle: String,
    assetLinkTitle: String,
    assetLink: String,
    assetLinks: AssetLinks?,
    marketCap: String,
    circulatingSupply: String,
    totalSupply: String,
    period: ChartPeriod,
    currentPoint: PricePoint? = null,
    chartPoints: List<PricePoint>,
    onPeriodClick: (ChartPeriod) -> Unit,
    onCancel: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Scene(
        title = assetTitle,
        backHandle = true,
        onClose = onCancel,
    ) {
        LazyColumn {
            item {
                ChartHead(
                    loading = loading,
                    period = period,
                    chartPoints = chartPoints,
                    currentPoint = currentPoint,
                    onPeriodClick = onPeriodClick,
                )
            }
            item {
                Table(
                    items = listOf(
                        CellEntity(
                            label = stringResource(id = R.string.asset_market_cap),
                            data = marketCap,
                        ),
                        CellEntity(
                            label = stringResource(id = R.string.asset_circulating_supply),
                            data = circulatingSupply,
                        ),
                        CellEntity(
                            label = stringResource(id = R.string.asset_total_supply),
                            data = totalSupply,
                        ),
                        if (assetLink.isNotEmpty()) {
                            CellEntity(
                                label = stringResource(id = R.string.transaction_view_on, assetLinkTitle),
                                data = "",
                                action = { uriHandler.openUri(assetLink) }
                            )
                        } else {
                            null
                        },
                    ),
                )
            }
            
            item {
                SubheaderItem(title = "LINKS")
                Table(
                    items = listOf(
                        if (assetLinks?.twitter.isNullOrEmpty()) {
                            null
                        } else {
                            CellEntity(
                                label = stringResource(id = R.string.social_x),
                                icon = "twitter".getDrawableUri(),
                                data = "",
                                action = { uriHandler.openUri(assetLinks?.twitter ?: return@CellEntity) }
                            )
                        },
                        if (assetLinks?.telegram.isNullOrEmpty()) {
                            null
                        } else {
                            CellEntity(
                                label = stringResource(id = R.string.social_telegram),
                                icon = "telegram".getDrawableUri(),
                                data = "",
                                action = { uriHandler.openUri(assetLinks?.telegram ?: return@CellEntity) }
                            )
                        },
                        if (assetLinks?.github.isNullOrEmpty()) {
                            null
                        } else {
                            CellEntity(
                                label = stringResource(id = R.string.social_github),
                                icon = "github".getDrawableUri(),
                                data = "",
                                action = { uriHandler.openUri(assetLinks?.github ?: return@CellEntity) }
                            )
                        },
                    ),
                )
            }
        }
    }
}

@Composable
fun ChartHead(
    loading: Boolean,
    period: ChartPeriod,
    currentPoint: PricePoint?,
    chartPoints: List<PricePoint>,
    onPeriodClick: (ChartPeriod) -> Unit,
) {
    val points = chartPoints.map { it.y }
    val min = points.minOrNull() ?: 0f
    val max = points.maxOrNull() ?: 0f
    val minIndex = points.indexOf(min)
    val maxIndex = points.indexOf(max)
    val modelProducer = remember { CartesianChartModelProducer.build() }
    var price by remember {
        mutableStateOf<PricePoint?>(null)
    }
    Container {
        Column {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val point = if (price == null) currentPoint else price
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
            if (loading) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LaunchedEffect(period) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(end = padding4),
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
                            if (index > 0 && index < chartPoints.size) {
                                price = chartPoints[index]
                            }
                        }

                        override fun onUpdated(
                            marker: CartesianMarker,
                            targets: List<CartesianMarker.Target>
                        ) {
                            price = chartPoints[targets.first().x.toInt()]
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
                            axisValueOverrider = AxisValueOverrider.fixed(minY = min, maxY = max),
                        ),
                        topAxis = rememberTopAxis(
                            valueFormatter = { value, _, _ -> if (value == maxIndex.toFloat()) chartPoints[maxIndex].yLabel ?: "" else "" },
                        ),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _, _ -> if (value == minIndex.toFloat()) chartPoints[minIndex].yLabel ?: "" else "" }
                        ),
                    ),
                    marker = rememberMarker(labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint),
                    modelProducer = modelProducer,
                )
            }
            Spacer16()
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                ChartPeriod.entries.forEach {
                    when (it) {
                        ChartPeriod.Hour -> PeriodButton(stringResource(id = R.string.charts_hour), it == period) {
                            onPeriodClick(ChartPeriod.Hour)
                        }

                        ChartPeriod.Day -> PeriodButton(stringResource(id = R.string.charts_day), it == period) {
                            onPeriodClick(ChartPeriod.Day)
                        }

                        ChartPeriod.Week -> PeriodButton(stringResource(id = R.string.charts_week), it == period) {
                            onPeriodClick(ChartPeriod.Week)
                        }

                        ChartPeriod.Month -> PeriodButton(stringResource(id = R.string.charts_month), it == period) {
                            onPeriodClick(ChartPeriod.Month)
                        }

                        ChartPeriod.Year -> PeriodButton(stringResource(id = R.string.charts_year), it == period) {
                            onPeriodClick(ChartPeriod.Year)
                        }

                        ChartPeriod.All -> PeriodButton(stringResource(id = R.string.charts_all), it == period) {
                            onPeriodClick(ChartPeriod.All)
                        }

                        ChartPeriod.Quarter -> {}
                    }
                }
            }
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
fun rememberMarketGuideLine(): LineComponent {
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