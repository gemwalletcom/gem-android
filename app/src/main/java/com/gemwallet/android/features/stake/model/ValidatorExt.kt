package com.gemwallet.android.features.stake.model

import android.text.format.DateUtils
import com.gemwallet.android.features.assets.model.PriceUIState
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator

fun DelegationValidator.getIconUrl(): String {
    return "https://assets.gemwallet.com/blockchains/${chain.string}/validators/${id}/logo.png"
}

fun DelegationValidator.formatApr(): String {
    return PriceUIState.formatPercentage(apr, showSign = false, showZero = true)
}

fun availableIn(delegation: Delegation?): String {
    val completionDate = (delegation?.base?.completionDate ?: return "") - System.currentTimeMillis()
    if (completionDate < 0) {
        return "0"
    }
    val days = completionDate / DateUtils.DAY_IN_MILLIS
    val hours = (completionDate % DateUtils.DAY_IN_MILLIS) / DateUtils.HOUR_IN_MILLIS
    val minutes = (completionDate % DateUtils.DAY_IN_MILLIS % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS
    val seconds = (completionDate % DateUtils.DAY_IN_MILLIS % DateUtils.HOUR_IN_MILLIS % DateUtils.MINUTE_IN_MILLIS) / DateUtils.SECOND_IN_MILLIS
    return when {
        days > 0 -> "$days days $hours hours"
        hours > 0 -> "$hours hours $minutes minutes"
        else -> "$minutes minutes $seconds seconds"
    }
}