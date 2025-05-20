package com.gemwallet.android.model

import android.icu.text.CompactDecimalFormat
import com.wallet.core.primitives.Asset
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.min

fun cryptoFormat(
    value: BigDecimal,
    symbol: String,
    decimalPlace: Int,
    maxDecimals: Int = -1,
    showSign: SignMode = SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    val (value, _) = cutFraction(value, decimalPlace, maxDecimals, dynamicPlace)
    val formatter = NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
    val formatted = if (value.compareTo(BigDecimal.ZERO) == 0) {
        formatter.maximumFractionDigits = zeroFraction
        formatter.minimumFractionDigits = zeroFraction
        formatter.format(0.00)
    } else {
        formatter.maximumFractionDigits = Int.MAX_VALUE
        formatter.minimumFractionDigits = 2
        formatter.format(value.abs())
    }
    val zeroCompare = value.compareTo(BigDecimal.ZERO)
    return if (zeroCompare < 0) {
        "${if (showSign != SignMode.NoSign) "-" else ""}$formatted $symbol"
    } else if (zeroCompare == 0) {
        "$formatted $symbol"
    } else {
        "${if (showSign == SignMode.All) "+" else ""}$formatted $symbol"
    }.trimEnd()
}

fun fiatFormat(
    value: BigDecimal,
    symbol: String,
    decimalPlace: Int,
    maxDecimals: Int = -1,
    dynamicPlace: Boolean,
): String {
    val (value, place) = cutFraction(value, decimalPlace, maxDecimals, dynamicPlace)
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = place
    format.currency = Currency.getInstance(symbol)
    return format.format(value)
}

private fun cutFraction(value: BigDecimal, decimalPlace: Int, maxDecimals: Int, dynamicDecimal: Boolean = false): Pair<BigDecimal, Int> {
    if (value.compareTo(BigDecimal.ZERO) == 0) {
        return Pair(value, decimalPlace)
    }
    val whole = value.toBigInteger().abs().toBigDecimal()
    val fraction = value.abs().minus(whole).stripTrailingZeros().toPlainString()

    val result = if (decimalPlace == -1) {
        value
    } else {
        val minDecimalPlaces = min(decimalPlace, fraction.length - 2)
        val result = if (minDecimalPlaces > 0) {
            BigDecimal("${whole}.${fraction.substring(2 until minDecimalPlaces + 2)}")
        } else {
            whole
        }
        if (value > BigDecimal.ZERO) {
            result
        } else {
            result.multiply(BigDecimal(-1.0))
        }
    }
    return if (result <= BigDecimal.ZERO && dynamicDecimal && decimalPlace < fraction.length && (decimalPlace < maxDecimals || maxDecimals == -1)) {
        cutFraction(value, decimalPlace * 2, maxDecimals, true)
    } else {
        Pair(result, decimalPlace)
    }
}

enum class SignMode {
    NoSign,
    NoPLus,
    All,
}

abstract class CountingUnit<T : Number, C>(
    val atomicValue: T,
) {
    abstract fun convert(decimals: Int, price: Double): C

    abstract fun value(decimals: Int): BigDecimal

    abstract fun format(
        decimals: Int,
        symbol: String,
        decimalPlace: Int,
        maxDecimals: Int = -1,
        showSign: SignMode = SignMode.NoPLus,
        dynamicPlace: Boolean = false,
        zeroFraction: Int = 0,
    ): String
}

class Crypto(atomicValue: BigInteger) : CountingUnit<BigInteger, Fiat>(
    atomicValue = atomicValue,
) {
    constructor(value: String, decimals: Int) : this(value.toBigDecimal(), decimals)

    constructor(value: String) : this(value.toBigInteger())

    constructor(value: BigDecimal, decimals: Int) : this(
        value.multiply(BigDecimal.TEN.pow(decimals)).toBigInteger()
    )

    override fun convert(decimals: Int, price: Double): Fiat {
        val result = atomicValue.toBigDecimal()
            .divide(BigDecimal.TEN.pow(decimals), MathContext.DECIMAL128)
            .multiply(price.toBigDecimal())
        return Fiat(result)
    }

    override fun value(decimals: Int): BigDecimal =
        atomicValue.toBigDecimal().divide(BigDecimal.TEN.pow(decimals), MathContext.DECIMAL128)

    override fun format(
        decimals: Int,
        symbol: String,
        decimalPlace: Int,
        maxDecimals: Int,
        showSign: SignMode,
        dynamicPlace: Boolean,
        zeroFraction: Int,
    ): String = cryptoFormat(value(decimals), symbol, decimalPlace, maxDecimals, showSign, dynamicPlace, zeroFraction)
}

class Fiat(value: BigDecimal) : CountingUnit<BigDecimal, Crypto>(
    atomicValue = value,
) {
    override fun convert(decimals: Int, price: Double): Crypto {
        val result = atomicValue.divide(price.toBigDecimal(), MathContext.DECIMAL128)
            .multiply(BigDecimal.TEN.pow(decimals))
            .toBigInteger()
        return Crypto(result)
    }

    override fun value(decimals: Int): BigDecimal = atomicValue

    override fun format(
        decimals: Int,
        symbol: String,
        decimalPlace: Int,
        maxDecimals: Int,
        showSign: SignMode,
        dynamicPlace: Boolean,
        zeroFraction: Int,
    ): String = fiatFormat(value(0), symbol, decimalPlace, maxDecimals, dynamicPlace)
}

fun AssetBalance.format(
    value: Double,
    decimalPlace: Int = 6,
    showSign: SignMode = SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
    showSymbol: Boolean = true,
): String {
    return cryptoFormat(
        value = value.toBigDecimal(),
        symbol = if (showSymbol) asset.symbol else "",
        decimalPlace = decimalPlace,
        showSign = showSign,
        dynamicPlace = dynamicPlace,
        zeroFraction = zeroFraction,
    )
}

fun AssetBalance.availableFormatted(
    decimalPlace: Int = 6,
    dynamicPlace: Boolean = false,
): String {
    return format(balanceAmount.available, decimalPlace, SignMode.NoPLus, dynamicPlace, 0, true)
}

fun AssetBalance.reservedFormatted(
    decimalPlace: Int = 6,
    dynamicPlace: Boolean = false,
): String {
    return format(balanceAmount.reserved, decimalPlace, SignMode.NoPLus, dynamicPlace, 0, true)
}

fun AssetBalance.totalFormatted(
    decimalPlace: Int = 6,
    dynamicPlace: Boolean = false,
): String {
    return format(totalAmount, decimalPlace, SignMode.NoPLus, dynamicPlace, 0, true)
}

fun AssetBalance.totalStakeFormatted(
    decimalPlace: Int = 6,
    dynamicPlace: Boolean = false,
): String {
    return format(balanceAmount.getStackedAmount(), decimalPlace, SignMode.NoPLus, dynamicPlace, 0, true)
}

fun Asset.format(
    humanAmount: String,
    decimalPlace: Int = 6,
    maxDecimals: Int = -1,
    showSign: SignMode = SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(
        Crypto(humanAmount.toBigDecimal(), decimals),
        decimalPlace,
        maxDecimals,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun Asset.format(
    humanAmount: Double,
    decimalPlace: Int = 6,
    maxDecimals: Int = -1,
    showSign: SignMode = SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(
        Crypto(humanAmount.toBigDecimal(), decimals),
        decimalPlace,
        maxDecimals,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun Asset.format(
    humanAmount: BigDecimal,
    decimalPlace: Int = 6,
    maxDecimals: Int = -1,
    showSign: SignMode = SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(
        Crypto(humanAmount, decimals),
        decimalPlace,
        maxDecimals,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun Asset.format(
    crypto: Crypto,
    decimalPlace: Int = 6,
    maxDecimals: Int = -1,
    showSign: SignMode = SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
    showSymbol: Boolean = true
): String {
    return crypto.format(
        decimals,
        if (showSymbol) symbol else "",
        decimalPlace,
        maxDecimals,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun com.wallet.core.primitives.Currency.format(
    value: Double,
    decimalPlace: Int = 2,
    maxDecimals: Int = -1,
    dynamicPlace: Boolean = false,
): String = format(BigDecimal.valueOf(value), decimalPlace, maxDecimals, dynamicPlace)

fun com.wallet.core.primitives.Currency.format(
    value: Float,
    decimalPlace: Int = 2,
    maxDecimals: Int = -1,
    dynamicPlace: Boolean = false,
): String = format(BigDecimal.valueOf(value.toDouble()), decimalPlace, maxDecimals, dynamicPlace)

fun com.wallet.core.primitives.Currency.format(
    value: BigDecimal,
    decimalPlace: Int = 2,
    maxDecimals: Int = -1,
    dynamicPlace: Boolean = false,
): String = fiatFormat(value, this.string, decimalPlace, maxDecimals, dynamicPlace)

fun com.wallet.core.primitives.Currency.compactFormatter(
    value: Double,
    locale: Locale = Locale.getDefault()
): String {
    if (value <= 100_000.0) {
        return format(value)
    }
    val formatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
    formatter.currency = android.icu.util.Currency.getInstance(string)
    return formatter.format(value)
}

fun Asset.compactFormatter(
    value: Double,
    locale: Locale = Locale.getDefault()
): String {
    if (value <= 100_000.0) {
        return format(value)
    }
    val formatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT)
    return "${formatter.format(value)} $symbol"
}