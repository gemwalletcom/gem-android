package com.gemwallet.android.features.assets.model

import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Currency
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.absoluteValue

typealias IconUrl = String

data class AssetUIState(
    val id: AssetId,
    val name: String,
    val icon: IconUrl,
    val symbol: String,
    val type: AssetType,
    val value: String,
    val isZeroValue: Boolean,
    val price: PriceUIState? = null,
    val fiat: String = "",
    val owner: String = "",
    val metadata: AssetMetaData? = null,
)

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
            val dayChanges = formatPercentage(price?.priceChangePercentage24h ?: 0.0)
            val state = getState(price?.priceChangePercentage24h ?: 0.0)
            return PriceUIState(value = value, state = state, dayChanges = dayChanges)
        }

        fun formatPercentage(value: Double, showSign: Boolean = true, showZero: Boolean = false): String {
            return if (value == 0.0 && !showZero) {
                ""
            } else {
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.DOWN
                "${if (showSign) if (value > 0) "+" else "-" else ""}${df.format(value.absoluteValue)}%"
            }
        }

        fun getState(percentage: Double) = when {
            percentage > 0 -> PriceState.Up
            percentage < 0 -> PriceState.Down
            else -> PriceState.None
        }
    }
}

enum class PriceState {
    None,
    Up,
    Down,
}
