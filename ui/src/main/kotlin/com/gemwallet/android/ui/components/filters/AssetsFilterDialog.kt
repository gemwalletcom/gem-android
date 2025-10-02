package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.SearchBar
import com.gemwallet.android.ui.components.list_item.SwitchProperty
import com.wallet.core.primitives.Chain

@Composable
fun AssetsFilter(
    availableChains: List<Chain>,
    chainFilter: List<Chain>,
    balanceFilter: Boolean,
    onDismissRequest: () -> Unit,
    onChainFilter: (Chain) -> Unit,
    onBalanceFilter: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    val query = rememberTextFieldState()

    FilterDialog(
        onDismissRequest = onDismissRequest,
        onClearFilters = onClearFilters,
    ) {
        SearchBar(query)
        HasBalances(isActive = balanceFilter, onBalanceFilter)
        LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
            selectFilterChain(availableChains, chainFilter, query.text.toString(), onChainFilter)
        }
    }
}

@Composable
private fun ColumnScope.HasBalances(
    isActive: Boolean,
    onBalanceFilter: (Boolean) -> Unit,
) {
    SwitchProperty(
        text = stringResource(R.string.filter_has_balance),
        checked = isActive,
        onCheckedChange = onBalanceFilter,
    )
}