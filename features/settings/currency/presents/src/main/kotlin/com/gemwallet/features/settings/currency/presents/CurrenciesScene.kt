package com.gemwallet.features.settings.currency.presents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.features.settings.currency.presents.components.CurrencyItem
import com.gemwallet.features.settings.currency.viewmodels.CurrenciesViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun CurrenciesScene(
    onCancel: () -> Unit,
    viewModel: CurrenciesViewModel = hiltViewModel()
) {
    Scene(
        title = stringResource(id = R.string.settings_currency),
        onClose = onCancel,
    ) {
        val currentCurrency = viewModel.getCurrency()
        LazyColumn {
            item {
                SubheaderItem(title = stringResource(id = R.string.common_recommended))
            }

            items(viewModel.getDefaultCurrencies()) { currency ->
                CurrencyItem(
                    currency = currency,
                    selectedCurrency = currentCurrency,
                    onSelect = {
                        viewModel.setCurrency(it)
                        onCancel()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.size(20.dp))
                SubheaderItem(title = stringResource(id = R.string.common_all))
            }
            items(viewModel.getCurrencies()) { currency ->
                CurrencyItem(
                    currency = currency,
                    selectedCurrency = currentCurrency,
                    onSelect = {
                        viewModel.setCurrency(it)
                        onCancel()
                    }
                )
            }
        }
    }
}