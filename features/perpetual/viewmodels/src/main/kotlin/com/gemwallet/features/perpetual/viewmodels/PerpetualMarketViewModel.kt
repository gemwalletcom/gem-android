package com.gemwallet.features.perpetual.viewmodels

import androidx.lifecycle.ViewModel
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalance
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPositions
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetuals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PerpetualMarketViewModel @Inject constructor(
    private val getPerpetuals: GetPerpetuals,
    private val getPositions: GetPerpetualPositions,
    private val getBalance: GetPerpetualBalance,
) : ViewModel() {

    val query = MutableStateFlow<String?>(null)
    val perpetuals = getPerpetuals.getPerpetuals(query)
    val positions = getPositions.getPerpetualPositions(query)
    val balance = getBalance.getPerpetualBalance()
}