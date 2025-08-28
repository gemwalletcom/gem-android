package com.gemwallet.features.settings.networks.presents

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.components.screen.SelectChain
import com.gemwallet.features.settings.networks.viewmodels.NetworksViewModel

@Composable
fun NetworksScreen(
    onCancel: () -> Unit,
    viewModel: NetworksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nodes by viewModel.nodes.collectAsStateWithLifecycle()
    val nodeStates by viewModel.nodeStates.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val selectListState = rememberLazyListState()

    BackHandler(!state.selectChain) {
        viewModel.onSelectChain()
    }

    AnimatedContent(
        targetState = state.selectChain,
        transitionSpec = {
            if (!state.selectChain) {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                )
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                )
            }
        },
        label = "phrase"
    ) {
        when (it) {
            true -> SelectChain(
                chains = state.chains,
                listState = selectListState,
                chainFilter = viewModel.chainFilter,
                onSelect = viewModel::onSelectedChain,
                onCancel = onCancel
            )
            false -> NetworkScene(
                state = state,
                nodes = nodes,
                nodeStates = nodeStates,
                isRefreshing = isRefreshing != null,
                onRefresh = { viewModel.refresh() },
                onSelectNode = viewModel::onSelectNode,
                onSelectBlockExplorer = viewModel::onSelectBlockExplorer,
                onCancel = viewModel::onSelectChain
            )
        }
    }
}