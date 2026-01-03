@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.android.features.activities.presents.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.activities.viewmodels.TransactionsViewModel

@Composable
fun TransactionsNavScreen(
    onTransaction: (String) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chainFilter by viewModel.chainsFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()

    TransactionsScene(
        loading = uiState.loading,
        transactions = uiState.transactions,
        chainsFilter = chainFilter,
        typeFilter = typeFilter,
        listState = listState,
        onRefresh = viewModel::refresh,
        onChainFilter = viewModel::onChainFilter,
        onTypeFilter = viewModel::onTypeFilter,
        onTransactionClick = onTransaction,
        onClearChainsFilter = viewModel::clearChainsFilter,
        onClearTypesFilter = viewModel::clearTypeFilter,
    )
}