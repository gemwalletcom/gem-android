package com.gemwallet.features.perpetual.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetual
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualChartData
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPosition
import com.wallet.core.primitives.ChartPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PerpetualDetailsViewModel @Inject constructor(
    private val getPerpetual: GetPerpetual,
    private val getPerpetualPosition: GetPerpetualPosition,
    private val getPerpetualChartData: GetPerpetualChartData,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    val perpetualId = savedStateHandle.getStateFlow("perpetualId", "")

    val perpetual = perpetualId.flatMapLatest { getPerpetual.getPerpetual(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val position = perpetualId.flatMapLatest { getPerpetualPosition.getPositionByPerpetual(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val period = MutableStateFlow(ChartPeriod.Day)
    val chart = combine(
        perpetual.filterNotNull(),
        period
    ) { perpetual, period ->
        getPerpetualChartData.getPerpetualChartData(perpetual.asset.id, period)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun period(period: ChartPeriod) {
        this.period.update { period }
    }

}