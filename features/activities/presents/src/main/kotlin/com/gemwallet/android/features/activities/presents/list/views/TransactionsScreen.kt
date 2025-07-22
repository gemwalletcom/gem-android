@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.android.features.activities.presents.list.views

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.activities.presents.components.transactionsList
import com.gemwallet.android.features.activities.viewmodels.TxListScreenState
import com.gemwallet.android.features.activities.viewmodels.TransactionsViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun TransactionsScreen(
    onTransaction: (String) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    List(
        uiState = uiState,
        listState = listState,
        onRefresh = viewModel::refresh,
        onTransactionClick = onTransaction,
    )
}

@Composable
private fun List(
    uiState: TxListScreenState,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    Scene(
        title = stringResource(id = R.string.activity_title),
        mainActionPadding = PaddingValues(0.dp),
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
}