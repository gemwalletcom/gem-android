package com.gemwallet.features.earn.delegation.viewmodels

import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.WalletType

class DelegationSceneState(
    val walletType: WalletType,
    val state: DelegationState,
)