@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.android.features.activities.presents.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.components.list_item.transactionsList
import com.gemwallet.android.features.activities.viewmodels.TransactionsViewModel
import com.gemwallet.android.features.activities.viewmodels.TxListScreenState
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.filters.ActivitiesFilter
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.TransactionTypeFilter
import com.wallet.core.primitives.Chain

@Composable
fun TransactionsScreen(
    onTransaction: (String) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chainFilter by viewModel.chainsFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()

    List(
        uiState = uiState,
        chainsFilter = chainFilter,
        typeFilter = typeFilter,
        listState = listState,
        onRefresh = viewModel::refresh,
        onChainFilter = viewModel::onChainFilter,
        onTypeFilter = viewModel::onTypeFilter,
        onTransactionClick = onTransaction,
        onClearFilters = viewModel::clearFilters
    )
}

@Composable
private fun List(
    uiState: TxListScreenState,
    chainsFilter: List<Chain>,
    typeFilter: List<TransactionTypeFilter>,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit,
    onChainFilter: (Chain) -> Unit,
    onTypeFilter: (TransactionTypeFilter) -> Unit,
    onTransactionClick: (String) -> Unit,
    onClearFilters: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    var showFilters by remember { mutableStateOf(false) }

    Scene(
        title = stringResource(id = R.string.activity_title),
        mainActionPadding = PaddingValues(0.dp),
        actions = {
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    tint = if (chainsFilter.isEmpty() && typeFilter.isEmpty())
                        LocalContentColor.current
                    else
                        MaterialTheme.colorScheme.primary,
                    contentDescription = "Filter by networks",
                )
            }
        }
    ) {
        PullToRefreshBox(
            modifier = Modifier,
            isRefreshing = uiState.loading,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = uiState.loading,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            when {
                uiState.transactions.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .wrapContentHeight(align = Alignment.CenterVertically),
                            text = stringResource(id = R.string.activity_state_empty_title),
                            textAlign = TextAlign.Center,
                        )
                        // TODO: Add empty description
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                    ) {
                        transactionsList(
                            items = uiState.transactions,
                            onTransactionClick = onTransactionClick
                        )
                    }
                }
            }
        }
    }
    if (showFilters) {
        ActivitiesFilter(
            availableChains = Chain.entries,
            chainsFilter = chainsFilter,
            typeFilter = typeFilter,
            onDismissRequest = { showFilters = false },
            onChainFilter = onChainFilter,
            onTypeFilter = onTypeFilter,
            onClearFilters = onClearFilters,
        )
    }
}