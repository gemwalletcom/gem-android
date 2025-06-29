package com.gemwallet.android.ext

import com.wallet.core.blockchain.sui.SuiStake
import java.math.BigInteger

fun SuiStake.total(): BigInteger {
    return BigInteger(principal) + BigInteger(estimatedReward ?: "0")
}