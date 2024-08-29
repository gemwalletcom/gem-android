package com.gemwallet.android.features.assets.model

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.absoluteValue

typealias IconUrl = String

data class AssetUIState(
    val asset: Asset,
    val value: String,
    val isZeroValue: Boolean,
    val price: PriceUIState? = null,
    val fiat: String = "",
    val owner: String = "",
    val metadata: AssetMetaData? = null,
)

fun AssetInfo.toUIModel(): AssetUIState {
    val balances = balances.calcTotal()
    val currency = price?.currency ?: Currency.USD

    return AssetUIState(
        asset = asset,
        isZeroValue = balances.atomicValue == BigInteger.ZERO,
        value = asset.format(balances, 4),
        price = PriceUIState.create(price?.price, currency),
        fiat = if (price?.price == null || price.price.price == 0.0) {
            ""
        } else {
            currency.format(balances.convert(asset.decimals, price.price.price), 2)
        },
        owner = owner.address,
        metadata = metadata,
    )
}

data class PriceUIState(
    val value: String,
    val state: PriceState,
    val dayChanges: String,
) {
    companion object {
        fun create(price: AssetPrice?, currency: Currency): PriceUIState {
            val value = if (price == null || price.price == 0.0) {
                ""
            } else {
                Fiat(price.price).format(0, currency.string, 2, dynamicPlace = true)
            }
            val dayChanges = formatPercentage(price?.priceChangePercentage24h ?: 0.0, showZero = true)
            val state = getState(price?.priceChangePercentage24h ?: 0.0)
            return PriceUIState(value = value, state = state, dayChanges = dayChanges)
        }

        fun formatPercentage(value: Double, showSign: Boolean = true, showZero: Boolean = false): String {
            return if (value == 0.0 && !showZero) {
                ""
            } else {
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.DOWN
                val formattedValue = df.format(value.absoluteValue)
                val afterFormat = df.parse(df.format(value))?.toDouble() ?: 0.0
                "${if (showSign) if (afterFormat > 0) "+" else if (afterFormat < 0) "-" else "" else ""}" +
                        "${if (afterFormat == 0.0) if (showZero) "0.00" else "" else formattedValue}%"
            }
        }

        fun getState(percentage: Double): PriceState {
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            val formattedValue = df.format(percentage)
            val afterFormat = df.parse(formattedValue)?.toDouble() ?: 0.0
            return when {
                afterFormat > 0 -> PriceState.Up
                afterFormat < 0 -> PriceState.Down
                else -> PriceState.None
            }
        }
    }
}

enum class PriceState {
    None,
    Up,
    Down,
}
