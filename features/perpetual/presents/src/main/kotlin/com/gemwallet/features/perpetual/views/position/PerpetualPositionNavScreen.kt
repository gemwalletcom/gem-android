package com.gemwallet.features.perpetual.views.position

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.AmountParams
import com.gemwallet.features.perpetual.viewmodels.PerpetualDetailsViewModel

@Composable
fun PerpetualPositionNavScreen(
    onOpenPosition: (AmountParams) -> Unit,
    onClose: () -> Unit,
    viewModel: PerpetualDetailsViewModel = hiltViewModel()
) {
    val perpetual by viewModel.perpetual.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()
    val chart by viewModel.chart.collectAsStateWithLifecycle()
    val period by viewModel.period.collectAsStateWithLifecycle()

    PerpetualPositionScene(
        perpetual = perpetual ?: return,
        position = position,
        chartData = chart,
        period = period,
        onClose = onClose,
        onChartPeriodSelect = viewModel::period,
        onOpenPosition = { direction ->
            perpetual?.let {
                onOpenPosition(AmountParams.buildPerpetualOpenPosition(it.asset.id, it.id, direction))
            }
        }
    )
}