package com.gemwallet.android.model

import com.wallet.core.primitives.BalanceType
import kotlinx.serialization.Serializable

@Serializable
data class Balance (
    val type: BalanceType,
    val value: String
)