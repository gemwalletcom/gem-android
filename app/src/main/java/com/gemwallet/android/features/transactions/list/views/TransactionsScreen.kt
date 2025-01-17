package com.gemwallet.android.features.transactions.list.views

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.transactions.components.transactionsList
import com.gemwallet.android.features.transactions.list.model.TxListScreenState
import com.gemwallet.android.features.transactions.list.viewmodels.TransactionsViewModel
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun List(
    uiState: TxListScreenState,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(uiState.loading, onRefresh)
    Scene(
        title = stringResource(id = R.string.activity_title),
        mainActionPadding = PaddingValues(0.dp),
    ) {
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
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
                            text = stringResource(id = R.string.activity_empty_state_message),
                            textAlign = TextAlign.Center,
                        )
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
            PullRefreshIndicator(uiState.loading, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }

    }
}