package com.gemwallet.features.settings.currency.presents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.settings.currency.presents.components.CurrencyItem
import com.gemwallet.features.settings.currency.viewmodels.CurrenciesViewModel

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

            val defaultCurrencies = viewModel.getDefaultCurrencies()
            val defaultCurrenciesSize = defaultCurrencies.size
            itemsIndexed(defaultCurrencies) { index, item ->
                CurrencyItem(
                    currency = item,
                    selectedCurrency = currentCurrency,
                    listPosition = ListPosition.getPosition(index, defaultCurrenciesSize),
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
            val currencies = viewModel.getCurrencies()
            val currenciesSize = currencies.size
            itemsIndexed(currencies) { index, item ->
                CurrencyItem(
                    currency = item,
                    selectedCurrency = currentCurrency,
                    listPosition = ListPosition.getPosition(index, currenciesSize),
                    onSelect = {
                        viewModel.setCurrency(it)
                        onCancel()
                    }
                )
            }
        }
    }
}