package com.gemwallet.android.features.stake.stake.model

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.WalletType

class StakeUIState(
    val loading: Boolean,
    val walletType: WalletType,
    val assetId: AssetId,
    val assetIcon: String? = "",
    val stakeChain: StakeChain,
    val assetDecimals: Int,
    val assetSymbol: String,
    val ownerAddress: String,
    val title: String,
    val hasRewards: Boolean = false,
    val rewardsAmount: String = "",
    val apr: Double,
    val lockTime: Int,
    val delegations: List<Delegation> = emptyList(),
)