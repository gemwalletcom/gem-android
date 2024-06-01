package com.gemwallet.android.features.stake.delegation.model

import com.gemwallet.android.ui.components.CellEntity
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.WalletType

sealed interface DelegationSceneState {
    data object Loading : DelegationSceneState

    class Loaded(
        val walletType: WalletType,
        val state: DelegationState,
        val validator: DelegationValidator,
        val balances: List<CellEntity<Int>>,
        val availableIn: String,
        val stakeChain: StakeChain,
    ) : DelegationSceneState
}