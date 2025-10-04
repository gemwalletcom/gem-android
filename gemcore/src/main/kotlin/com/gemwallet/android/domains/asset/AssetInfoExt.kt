package com.gemwallet.android.domains.asset

import android.text.format.DateUtils
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.availableFormatted
import com.gemwallet.android.model.format
import com.wallet.core.primitives.StakeChain
import uniffi.gemstone.Config
import java.math.BigDecimal
import java.math.BigInteger

val AssetInfo.symbol: String
    get() = asset.symbol

val AssetInfo.decimals: Int
    get() = asset.decimals

val AssetInfo.title: String
    get() = asset.title

val AssetInfo.stakeChain: StakeChain? // TODO: Out to StakeExt
    get() = asset.stakeChain

val AssetInfo.lockTime: Int?  // TODO: Out to StakeExt
    get() = owner?.chain?.string?.let {
        (Config().getStakeConfig(it).timeLock.toLong() / (DateUtils.DAY_IN_MILLIS / 1000)).toInt()
    }

val AssetInfo.availableBalance: String  // TODO: Out to BalanceExt
    get() = Crypto(balance.balance.available)
        .value(asset.decimals)
        .stripTrailingZeros().toPlainString()

val AssetInfo.availableBalanceFormatted: String // TODO: Out to BalanceExt
    get() = balance.availableFormatted(4, dynamicPlace = true)


fun AssetInfo.calculateFiat(rawInput: String): BigDecimal {
    val value = Crypto(rawInput.toBigIntegerOrNull() ?: BigInteger.ZERO)
        .value(asset.decimals)
    return calculateFiat(value)
}

fun AssetInfo.calculateFiat(value: BigDecimal): BigDecimal {
    return price?.takeIf { it.price.price > 0.0 }?.let {
        value * it.price.price.toBigDecimal()
    } ?: return BigDecimal.ZERO
}

fun AssetInfo.formatFiat(value: BigDecimal): String {
    if (value <= BigDecimal.ZERO) {
        return ""
    }

    return price?.currency?.format(value) ?: ""
}

fun AssetInfo.isMemoSupport() = asset.isMemoSupport()