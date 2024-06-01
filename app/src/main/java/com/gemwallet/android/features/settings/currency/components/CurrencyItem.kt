package com.gemwallet.android.features.settings.currency.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.ListItemTitle
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
        trailing = if (currency == selectedCurrency) {
            @Composable {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "selected_currency"
                )
            }
        } else {
            null
        },
    ) {
        ListItemTitle(title = "${currency.string} - $title", subtitle = "")
    }
}