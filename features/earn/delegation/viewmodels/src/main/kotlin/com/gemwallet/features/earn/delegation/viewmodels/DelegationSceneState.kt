package com.gemwallet.features.earn.delegation.viewmodels

import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.WalletType

class DelegationSceneState(
    val walletType: WalletType,
    val state: DelegationState,
    val validator: DelegationValidator,
    val stakeBalance: String,
    val rewardsBalance: String,
    val availableIn: String,
    val stakeChain: StakeChain,
)