package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.models.TransactionTypeFilter
import com.wallet.core.primitives.Chain

@Composable
fun ActivitiesFilter(
    availableChains: List<Chain>,
    chainsFilter: List<Chain>,
    typeFilter: List<TransactionTypeFilter>,
    onDismissRequest: () -> Unit,
    onChainFilter: (Chain) -> Unit,
    onTypeFilter: (TransactionTypeFilter) -> Unit,
    onClearFilters: () -> Unit,
) {
    FilterDialog(
        onDismissRequest,
        onClearFilters,
    ) { query ->
        LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
            selectFilterTransactionType(typeFilter, onTypeFilter)
            selectFilterChain(availableChains, chainsFilter, query, onChainFilter)
        }
    }
}