package com.gemwallet.features.nft.presents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gemwallet.android.cases.nft.NftError
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.NftAssetIdAction
import com.gemwallet.android.ui.models.actions.NftCollectionIdAction
import com.gemwallet.android.ui.theme.padding8
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.features.nft.viewmodels.NftListViewModels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NftListScene(
    cancelAction: CancelAction?,
    collectionAction: NftCollectionIdAction,
    assetAction: NftAssetIdAction,
    listState: LazyGridState = rememberLazyGridState(),
) {
    val viewModel: NftListViewModels = hiltViewModel()

    val items by viewModel.collections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()

    Scene(
        title = stringResource(R.string.nft_collections),
        onClose = if (cancelAction == null) null else { { cancelAction() } } // TODO: Replace to action in scene
    ) {
        val isRefreshing = isLoading && !items.isEmpty()
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullToRefreshState,
            indicator = {
                Indicator( // TODO: Out to view library
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            if (error != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = when (error) {
                                NftError.LoadError -> stringResource(R.string.errors_error_occured)
                                NftError.NotFoundAsset -> error?.message ?: ""
                                NftError.NotFoundCollection -> error?.message ?: ""
                                null -> ""
                            }
                        )
                        TextButton(onClick = viewModel::refresh) {
                            Text(stringResource(R.string.common_try_again))
                        }
                    }
                }
                return@PullToRefreshBox
            }

            if (isLoading && items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator20(modifier = Modifier.align(Alignment.Center))
                }
                return@PullToRefreshBox
            }

            if (!isLoading && items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.nft_state_empty_title)
                    )
                    // TODO: Add empty description
                }
                return@PullToRefreshBox
            }

            LazyVerticalGrid(
                modifier = Modifier.padding(/*horizontal = 8.dp*/),
                columns = GridCells.Adaptive(minSize = 150.dp),
                state = listState,
                contentPadding = PaddingValues(padding8, paddingDefault)
            ) {
                items(items) { item -> NFTItem(item, collectionAction, assetAction) }
            }
        }
    }
}