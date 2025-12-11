package com.gemwallet.features.perpetual.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalance
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPositions
import com.gemwallet.android.application.perpetual.coordinators.GetPerpetuals
import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetualPositions
import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetuals
import com.gemwallet.android.application.perpetual.coordinators.TogglePerpetualPin
import com.gemwallet.features.perpetual.viewmodels.model.PerpetualMarketSceneState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PerpetualMarketViewModel @Inject constructor(
    private val getPerpetuals: GetPerpetuals,
    private val getPositions: GetPerpetualPositions,
    private val getBalance: GetPerpetualBalance,
    private val syncPerpetuals: SyncPerpetuals,
    private val syncPerpetualPositions: SyncPerpetualPositions,
    private val togglePin: TogglePerpetualPin
) : ViewModel() {

    val query = MutableStateFlow<String?>(null)
    val sceneState = MutableStateFlow<PerpetualMarketSceneState>(PerpetualMarketSceneState.Idle)
    private val perpetuals = getPerpetuals.getPerpetuals(query)
    val unpinnedPerpetuals = perpetuals.map { items -> items.filter { !it.isPinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val pinnedPerpetuals = perpetuals.map { items -> items.filter { it.isPinned } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val positions = getPositions.getPerpetualPositions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val balance = getBalance.getPerpetualBalance()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onRefresh() {
        sceneState.update { PerpetualMarketSceneState.Refreshing }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                syncPerpetuals.syncPerpetuals()
            }
            withContext(Dispatchers.IO) {
                syncPerpetualPositions.syncPerpetualPositions()
            }
            withContext(Dispatchers.IO) {
                delay(500)
                sceneState.update { PerpetualMarketSceneState.Idle }
            }
        }
    }

    fun onTogglePin(perpetualId: String) {
        togglePin.togglePin(perpetualId)
    }
}