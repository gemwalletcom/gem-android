package com.gemwallet.android.model

import com.wallet.core.primitives.Asset
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.min

abstract class CountingUnit<T : Number, C>(
    val atomicValue: T,
) {
    abstract fun convert(decimals: Int, price: Double): C

    abstract fun value(decimals: Int): BigDecimal

    abstract fun format(
        decimals: Int,
        symbol: String,
        decimalPlace: Int,
        showSign: SignMode = SignMode.NoPLus,
        dynamicPlace: Boolean = false,
        zeroFraction: Int = 0,
    ): String

    fun cutFraction(value: BigDecimal, decimalPlace: Int, dynamicDecimal: Boolean = false): Pair<BigDecimal, Int> {
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
        return if (result <= BigDecimal.ZERO && dynamicDecimal && decimalPlace < fraction.length) {
                cutFraction(value, decimalPlace + 2, true)
            } else {
                Pair(result, decimalPlace)
            }
    }

    enum class SignMode {
        NoSign,
        NoPLus,
        All,
    }
}

class Crypto(atomicValue: BigInteger) : CountingUnit<BigInteger, Fiat>( // TODO: Move to BigInteger ext
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
        showSign: SignMode,
        dynamicPlace: Boolean,
        zeroFraction: Int,
    ): String {
        val (value, _) = cutFraction(value(decimals), decimalPlace, dynamicPlace)
        val formatter = NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat
        val formatted = if (value.compareTo(BigDecimal.ZERO) == 0) {
            formatter.maximumFractionDigits = zeroFraction
            formatter.minimumFractionDigits = zeroFraction
            formatter.format(0.00)
        } else {
            formatter.maximumFractionDigits = Int.MAX_VALUE
            formatter.minimumFractionDigits = 0
            formatter.format(value)
        }
        val zeroCompare = value.compareTo(BigDecimal.ZERO)
        return if (zeroCompare < 0) {
            "${if (showSign != SignMode.NoSign) "-" else ""}$formatted $symbol"
        } else if (zeroCompare == 0) {
            "$formatted $symbol"
        } else {
            "${if (showSign == SignMode.All) "+" else ""}$formatted $symbol"
        }
    }
}

class Fiat(value: BigDecimal) : CountingUnit<BigDecimal, Crypto>(
    atomicValue = value,
) {
    constructor(value: Double) : this(BigDecimal(value))

    constructor(value: Float) : this(BigDecimal(value.toDouble()))

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
        showSign: SignMode,
        dynamicPlace: Boolean,
        zeroFraction: Int,
    ): String {
        val (value, place) = cutFraction(value(0), decimalPlace, dynamicPlace)
        val format = NumberFormat.getCurrencyInstance()
        if (decimals > -1) {
            format.maximumFractionDigits = place
        }
        format.currency = Currency.getInstance(symbol)
        return format.format(value)
    }
}

fun Asset.format(
    humanAmount: String,
    decimalPlace: Int = 6,
    showSign: CountingUnit.SignMode = CountingUnit.SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(
        Crypto(humanAmount.toBigDecimal(), decimals),
        decimalPlace,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun Asset.format(
    humanAmount: Double,
    decimalPlace: Int = 6,
    showSign: CountingUnit.SignMode = CountingUnit.SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(
        Crypto(humanAmount.toBigDecimal(), decimals),
        decimalPlace,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun Asset.format(
    crypto: Crypto,
    decimalPlace: Int = 6,
    showSign: CountingUnit.SignMode = CountingUnit.SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
    showSymbol: Boolean = true
): String {
    return crypto.format(
        decimals,
        if (showSymbol) symbol else "",
        decimalPlace,
        showSign,
        dynamicPlace,
        zeroFraction,
    )
}

fun com.wallet.core.primitives.Currency.format(
    value: Double,
    decimalPlace: Int = 2,
    showSign: CountingUnit.SignMode = CountingUnit.SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(Fiat(value), decimalPlace, showSign, dynamicPlace, zeroFraction)
}

fun com.wallet.core.primitives.Currency.format(
    value: Float,
    decimalPlace: Int = 2,
    showSign: CountingUnit.SignMode = CountingUnit.SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return format(Fiat(value), decimalPlace, showSign, dynamicPlace, zeroFraction)
}

fun com.wallet.core.primitives.Currency.format(
    value: Fiat,
    decimalPlace: Int = 2,
    showSign: CountingUnit.SignMode = CountingUnit.SignMode.NoPLus,
    dynamicPlace: Boolean = false,
    zeroFraction: Int = 0,
): String {
    return value.format(
        decimals = 0,
        symbol = this.string,
        decimalPlace = decimalPlace,
        showSign = showSign,
        dynamicPlace = dynamicPlace,
        zeroFraction = zeroFraction,
    )
}