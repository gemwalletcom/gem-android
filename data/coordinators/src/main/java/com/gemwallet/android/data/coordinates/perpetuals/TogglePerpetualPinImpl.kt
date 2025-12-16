package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.TogglePerpetualPin
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.wallet.core.primitives.PerpetualMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class TogglePerpetualPinImpl @Inject constructor(
    private val perpetualRepository: PerpetualRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : TogglePerpetualPin {
    override fun togglePin(perpetualId: String) {
        scope.launch {
            val perpetual = perpetualRepository.getPerpetual(perpetualId).firstOrNull()
            perpetualRepository.setMetadata(
                perpetualId = perpetualId,
                metadata = PerpetualMetadata(isPinned = !(perpetual?.metadata?.isPinned ?: true)),
            )
        }
    }
}