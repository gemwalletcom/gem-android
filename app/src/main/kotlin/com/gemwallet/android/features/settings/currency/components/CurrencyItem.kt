package com.gemwallet.android.features.settings.currency.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.theme.padding8
import com.wallet.core.primitives.Currency

@Composable
fun CurrencyItem(
    currency: Currency,
    selectedCurrency: Currency,
    onSelect: (Currency) -> Unit,
) {
    val title = android.icu.util.Currency.getInstance(currency.string).displayName

    ListItem(
        modifier = Modifier.clickable { onSelect(currency) },
        title = { ListItemTitleText("${emojiFlags[currency.string] ?: ""}  ${currency.string} - $title") },
        trailing = if (currency == selectedCurrency) {
            @Composable {
                Icon(
                    modifier = Modifier.Companion.padding(end = padding8).size(20.dp),
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "selected_currency",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            null
        },
    )
}

internal val emojiFlags = mapOf(
    "MXN" to "🇲🇽",
    "CHF" to "🇨🇭",
    "CNY" to "🇨🇳",
    "THB" to "🇹🇭",
    "HUF" to "🇭🇺",
    "AUD" to "🇦🇺",
    "IDR" to "🇮🇩",
    "RUB" to "🇷🇺",
    "ZAR" to "🇿🇦",
    "EUR" to "🇪🇺",
    "NZD" to "🇳🇿",
    "SAR" to "🇸🇦",
    "SGD" to "🇸🇬",
    "BMD" to "🇧🇲",
    "KWD" to "🇰🇼",
    "HKD" to "🇭🇰",
    "JPY" to "🇯🇵",
    "GBP" to "🇬🇧",
    "DKK" to "🇩🇰",
    "KRW" to "🇰🇷",
    "PHP" to "🇵🇭",
    "CLP" to "🇨🇱",
    "TWD" to "🇹🇼",
    "PKR" to "🇵🇰",
    "BRL" to "🇧🇷",
    "CAD" to "🇨🇦",
    "BHD" to "🇧🇭",
    "MMK" to "🇲🇲",
    "VEF" to "🇻🇪",
    "VND" to "🇻🇳",
    "CZK" to "🇨🇿",
    "TRY" to "🇹🇷",
    "INR" to "🇮🇳",
    "ARS" to "🇦🇷",
    "BDT" to "🇧🇩",
    "NOK" to "🇳🇴",
    "USD" to "🇺🇸",
    "LKR" to "🇱🇰",
    "ILS" to "🇮🇱",
    "PLN" to "🇵🇱",
    "NGN" to "🇳🇬",
    "UAH" to "🇺🇦",
    "XDR" to "🏳️",
    "MYR" to "🇲🇾",
    "AED" to "🇦🇪",
    "SEK" to "🇸🇪",
    "BTC" to "₿"
)